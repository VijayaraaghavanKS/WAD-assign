package com.ssn.cartbackend.model;

// What the checkout form sends: optional coupon, delivery address, payment method.
public record CheckoutRequest(String coupon, Address address, String payment) {}
