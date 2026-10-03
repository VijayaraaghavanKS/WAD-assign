package com.ssn.cartbackend.repository;

import com.ssn.cartbackend.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserIdOrderByPlacedAtDesc(String userId);
}
