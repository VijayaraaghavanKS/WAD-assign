package com.ssn.cartbackend.logging;

import com.ssn.cartbackend.model.RequestLog;
import com.ssn.cartbackend.repository.RequestLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

// Runs once per HTTP request. Wraps the rest of the filter chain (which
// eventually reaches the @RestController methods) in a timer, then writes
// one RequestLog row per request to Mongo -- this is how the Developer
// Interface gets a live feed of every call the backend has served, without
// needing an external logging/observability tool.
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository repository;

    public RequestLoggingFilter(RequestLogRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        // Don't log the dev dashboard polling itself, or every request would
        // just be noise about the developer interface reading its own logs.
        if (request.getRequestURI().startsWith("/api/dev")) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        String error = null;

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            error = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            RequestLog log = new RequestLog(
                    null,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    error,
                    Instant.now());
            repository.save(log);
        }
    }
}
