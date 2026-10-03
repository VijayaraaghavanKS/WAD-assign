package com.ssn.cartbackend.model;

// A snapshot of one cart line at checkout, so later price changes don't rewrite history.
public record OrderLine(String productId, String productName, double price, int quantity) {}
