package com.ssn.orderservice.model;

// Aggregated numbers computed on demand from request_logs, for the
// Developer Interface's metrics panel. Not persisted -- just a response DTO.
public class Metrics {

    public long totalRequests;
    public long errorCount;
    public double errorRatePct;
    public double avgLatencyMs;
    public long uptimeSeconds;

    public Metrics(long totalRequests, long errorCount, double avgLatencyMs, long uptimeSeconds) {
        this.totalRequests = totalRequests;
        this.errorCount = errorCount;
        this.errorRatePct = totalRequests == 0 ? 0 : (errorCount * 100.0) / totalRequests;
        this.avgLatencyMs = avgLatencyMs;
        this.uptimeSeconds = uptimeSeconds;
    }
}
