package com.ssn.cartservice.repository;

import com.ssn.cartservice.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<CartItem, String> {
    CartItem findByProductId(String productId);
}
