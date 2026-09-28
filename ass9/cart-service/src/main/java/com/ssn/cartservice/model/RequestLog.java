package com.ssn.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// One row per HTTP request this service served. Read by the Developer
// Interface to show a live per-service request log and metrics.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "request_logs")
public class RequestLog {

    @Id
    private String id;

    private String method;
    private String path;
    private int status;
    private long durationMs;
    private String error;
    private Instant timestamp;
}
