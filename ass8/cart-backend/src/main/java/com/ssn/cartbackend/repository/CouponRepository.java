package com.ssn.cartbackend.repository;

import com.ssn.cartbackend.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CouponRepository extends MongoRepository<Coupon, String> {}
