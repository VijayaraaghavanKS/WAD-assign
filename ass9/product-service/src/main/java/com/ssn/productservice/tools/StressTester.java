package com.ssn.productservice.tools;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// A load/stress-testing tool written in plain Java -- no JMeter/k6 install
// needed. It fires N concurrent HTTP requests at a target endpoint using a
// fixed-size thread pool (each thread simulating one "virtual user"),
// timing every request, and reports throughput/latency/error-rate stats
// exactly like a real load test would.
//
// Usage: java -cp target/classes com.ssn.productservice.tools.StressTester
//            <url> <totalRequests> <concurrency> [method]
//
// Example: java -cp target/classes com.ssn.productservice.tools.StressTester
//            http://localhost:8083/api/products 500 20 GET
public class StressTester {

    public static void main(String[] args) throws InterruptedException {
        String url = args.length > 0 ? args[0] : "http://localhost:8083/api/products";
        int totalRequests = args.length > 1 ? Integer.parseInt(args[1]) : 500;
        int concurrency = args.length > 2 ? Integer.parseInt(args[2]) : 20;
        String method = args.length > 3 ? args[3] : "GET";

        System.out.printf("Target: %s %s | requests=%d concurrency=%d%n", method, url, totalRequests, concurrency);

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        // HttpClient.newHttpClient() defaults to ForkJoinPool.commonPool() for
        // its internal async plumbing, which has limited parallelism
        // (usually availableProcessors() - 1). Under real concurrent load that
        // shared pool becomes a bottleneck and a handful of requests stall
        // until the request timeout -- giving a client a dedicated executor
        // sized to the concurrency level avoids that contention.
        ExecutorService clientExecutor = Executors.newFixedThreadPool(concurrency);
        HttpClient client = HttpClient.newBuilder()
                .executor(clientExecutor)
                .build();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicLong totalLatencyMs = new AtomicLong();
        List<Long> latencies = new CopyOnWriteArrayList<>();

        long startedAt = System.nanoTime();

        List<Callable<Void>> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < totalRequests; i++) {
            tasks.add(() -> {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .method(method, HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(10))
                        .build();

                long t0 = System.nanoTime();
                try {
                    HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                    long latency = (System.nanoTime() - t0) / 1_000_000;
                    latencies.add(latency);
                    totalLatencyMs.addAndGet(latency);
                    if (response.statusCode() < 400) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
                return null;
            });
        }

        pool.invokeAll(tasks);
        pool.shutdown();
        clientExecutor.shutdown();

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        List<Long> sorted = new java.util.ArrayList<>(latencies);
        sorted.sort(Long::compareTo);
        long min = sorted.isEmpty() ? 0 : sorted.get(0);
        long max = sorted.isEmpty() ? 0 : sorted.get(sorted.size() - 1);
        long p95 = sorted.isEmpty() ? 0 : sorted.get((int) (sorted.size() * 0.95));
        double avg = latencies.isEmpty() ? 0 : totalLatencyMs.get() / (double) latencies.size();
        double throughput = totalRequests / (elapsedMs / 1000.0);

        System.out.println();
        System.out.println("===== Stress Test Results =====");
        System.out.printf("Total requests   : %d%n", totalRequests);
        System.out.printf("Successful (2xx/3xx): %d%n", successCount.get());
        System.out.printf("Failed (4xx/5xx/err): %d%n", errorCount.get());
        System.out.printf("Total time        : %d ms%n", elapsedMs);
        System.out.printf("Throughput        : %.1f req/sec%n", throughput);
        System.out.printf("Latency avg/min/max/p95: %.1f / %d / %d / %d ms%n", avg, min, max, p95);
        System.out.println("================================");
    }
}
