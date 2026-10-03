package com.ssn.cartbackend.controller;

import com.ssn.cartbackend.model.Order;
import com.ssn.cartbackend.service.AuthService;
import com.ssn.cartbackend.service.CheckoutService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final CheckoutService checkout;
    private final AuthService auth;

    public OrderController(CheckoutService checkout, AuthService auth) {
        this.checkout = checkout;
        this.auth = auth;
    }

    @PostMapping
    public Order placeOrder(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return checkout.checkout(auth.requireUser(authorization).getId());
    }

    @GetMapping
    public List<Order> myOrders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return checkout.history(auth.requireUser(authorization).getId());
    }
}
