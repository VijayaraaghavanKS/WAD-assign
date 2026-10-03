package com.ssn.cartbackend.repository;

import com.ssn.cartbackend.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CartRepository extends MongoRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);
    CartItem findByUserIdAndProductId(String userId, String productId);
    void deleteByUserId(String userId);
    void deleteByIdAndUserId(String id, String userId);
}
