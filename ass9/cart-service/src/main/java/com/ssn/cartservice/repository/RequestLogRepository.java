package com.ssn.cartservice.repository;

import com.ssn.cartservice.model.RequestLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RequestLogRepository extends MongoRepository<RequestLog, String> {
    List<RequestLog> findTop100ByOrderByTimestampDesc();
}
