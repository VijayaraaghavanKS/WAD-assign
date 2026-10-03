package com.ssn.orderservice.logging;

import com.ssn.orderservice.model.RequestLog;
import com.ssn.orderservice.repository.RequestLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

// Runs once per HTTP request served by THIS microservice. Each service logs
// its own requests independently -- there is no shared/central log store,
// which is exactly the observability trade-off microservices introduce
// versus the ass8 monolith's single log stream.
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository repository;

    public RequestLoggingFilter(RequestLogRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

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
