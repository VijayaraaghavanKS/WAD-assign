package com.ssn.cartbackend.repository;

import com.ssn.cartbackend.model.RequestLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RequestLogRepository extends MongoRepository<RequestLog, String> {

    // Most recent requests first, capped by Spring Data's Top/First keyword.
    List<RequestLog> findTop100ByOrderByTimestampDesc();
}
