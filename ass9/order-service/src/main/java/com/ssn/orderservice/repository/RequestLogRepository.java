package com.ssn.orderservice.repository;

import com.ssn.orderservice.model.RequestLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RequestLogRepository extends MongoRepository<RequestLog, String> {
    List<RequestLog> findTop100ByOrderByTimestampDesc();
}
