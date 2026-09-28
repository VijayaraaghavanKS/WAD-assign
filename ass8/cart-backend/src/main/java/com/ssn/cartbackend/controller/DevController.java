package com.ssn.cartbackend.controller;

import com.ssn.cartbackend.model.Metrics;
import com.ssn.cartbackend.model.RequestLog;
import com.ssn.cartbackend.repository.RequestLogRepository;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.List;

// Backs the Developer Interface: a live request log tail and aggregate
// metrics, so a developer taking over this codebase can see exactly what
// the API is doing without attaching a debugger or an external APM tool.
@RestController
@RequestMapping("/api/dev")
@CrossOrigin(origins = "*")
public class DevController {

    private final RequestLogRepository repository;

    public DevController(RequestLogRepository repository) {
        this.repository = repository;
    }

    // Most recent 100 requests, newest first.
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
