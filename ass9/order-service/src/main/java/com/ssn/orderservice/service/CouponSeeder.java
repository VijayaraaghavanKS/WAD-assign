package com.ssn.orderservice.service;

import com.ssn.orderservice.model.Coupon;
import com.ssn.orderservice.repository.CouponRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// On first start, adds the two starter codes. After that the admin owns the list.
@Component
public class CouponSeeder implements ApplicationRunner {

    private final CouponRepository coupons;

    public CouponSeeder(CouponRepository coupons) {
        this.coupons = coupons;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (coupons.count() == 0) {
            coupons.save(new Coupon("SAVE10", 10));
            coupons.save(new Coupon("WELCOME20", 20));
        }
    }
}
