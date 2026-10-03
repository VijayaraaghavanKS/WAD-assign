package com.ssn.orderservice.model;

// Where the order is delivered. Also used as the billing address.
public record Address(String name, String phone, String line1, String city, String state, String pin) {}
