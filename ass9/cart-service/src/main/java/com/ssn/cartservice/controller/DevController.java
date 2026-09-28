package com.ssn.cartservice.controller;

import com.ssn.cartservice.model.Metrics;
import com.ssn.cartservice.model.RequestLog;
import com.ssn.cartservice.repository.RequestLogRepository;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.List;

// Backs the Developer Interface's view of THIS microservice: its own
// request log and metrics, independent of Product Service's.
@RestController
@RequestMapping("/api/dev")
@CrossOrigin(origins = "*")
public class DevController {

    private final RequestLogRepository repository;

    public DevController(RequestLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/logs")
    public List<RequestLog> getLogs() {
        return repository.findTop100ByOrderByTimestampDesc();
    }

    @GetMapping("/metrics")
    public Metrics getMetrics() {
        List<RequestLog> all = repository.findAll();
        long total = all.size();
        long errors = all.stream().filter(l -> l.getStatus() >= 400).count();
        double avgLatency = all.stream().mapToLong(RequestLog::getDurationMs).average().orElse(0);
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        return new Metrics(total, errors, avgLatency, uptimeSeconds);
    }
}
