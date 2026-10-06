package com.ssn.orderservice.repository;

import com.ssn.orderservice.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CouponRepository extends MongoRepository<Coupon, String> {}
