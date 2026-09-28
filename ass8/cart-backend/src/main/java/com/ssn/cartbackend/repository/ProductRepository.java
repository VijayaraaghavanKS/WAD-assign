package com.ssn.cartbackend.repository;

import com.ssn.cartbackend.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
