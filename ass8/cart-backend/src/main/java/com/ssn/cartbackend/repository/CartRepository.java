package com.ssn.cartbackend.repository;

import com.ssn.cartbackend.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<CartItem, String> {
    CartItem findByProductId(String productId);
}
