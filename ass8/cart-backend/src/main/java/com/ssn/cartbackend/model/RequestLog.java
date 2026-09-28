package com.ssn.cartbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// One row per HTTP request the backend served. This is what the Developer
// Interface reads to show a live request log and compute metrics.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "request_logs")
public class RequestLog {

    @Id
    private String id;

    private String method;      // GET / POST / PUT / DELETE
    private String path;        // e.g. /api/products
    private int status;         // HTTP status code returned
    private long durationMs;    // how long the request took
    private String error;       // exception message, or null if the request succeeded
    private Instant timestamp;
}
