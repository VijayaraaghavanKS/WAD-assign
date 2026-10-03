package com.ssn.orderservice.repository;

import com.ssn.orderservice.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserIdOrderByPlacedAtDesc(String userId);
}
