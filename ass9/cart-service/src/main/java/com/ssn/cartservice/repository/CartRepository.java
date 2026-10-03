package com.ssn.cartservice.repository;

import com.ssn.cartservice.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CartRepository extends MongoRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);
    CartItem findByUserIdAndProductIdAndSizeAndColour(String userId, String productId, String size, String colour);
    void deleteByUserId(String userId);
    void deleteByIdAndUserId(String id, String userId);
}
