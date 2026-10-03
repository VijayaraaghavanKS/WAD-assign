package com.ssn.orderservice.controller;

import com.ssn.orderservice.model.Order;
import com.ssn.orderservice.security.AuthClient;
import com.ssn.orderservice.service.CheckoutService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final CheckoutService checkout;
    private final AuthClient auth;

    public OrderController(CheckoutService checkout, AuthClient auth) {
        this.checkout = checkout;
        this.auth = auth;
    }

    @PostMapping
    public Order placeOrder(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return checkout.checkout(authorization, userId(authorization));
    }

    @GetMapping
    public List<Order> myOrders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return checkout.history(userId(authorization));
    }

    private String userId(String authorization) {
        Map<String, String> user = auth.currentUser(authorization);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first");
        }
        return user.get("id");
    }
}
