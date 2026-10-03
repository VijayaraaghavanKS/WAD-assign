package com.ssn.orderservice.controller;

import com.ssn.orderservice.model.CheckoutRequest;
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

    // Body: {"coupon": "SAVE10", "address": {...}, "payment": "UPI"}. Coupon is optional.
    @PostMapping
    public Order placeOrder(@RequestHeader(value = "Authorization", required = false) String authorization,
                            @RequestBody(required = false) CheckoutRequest body) {
        Map<String, String> user = loggedIn(authorization);
        return checkout.checkout(authorization, user, body);
    }

    @GetMapping
    public List<Order> myOrders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return checkout.history(loggedIn(authorization).get("id"));
    }

    // Admin only: every order, the sales chart numbers, and moving an order along.
    @GetMapping("/all")
    public List<Order> allOrders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        requireAdmin(authorization);
        return checkout.all();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(@RequestHeader(value = "Authorization", required = false) String authorization) {
        requireAdmin(authorization);
        return checkout.stats();
    }

    @PutMapping("/{id}/status")
    public Order setStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
                           @PathVariable String id, @RequestBody Map<String, String> body) {
        requireAdmin(authorization);
        return checkout.setStatus(id, body.get("status"));
    }

    private Map<String, String> loggedIn(String authorization) {
        Map<String, String> user = auth.currentUser(authorization);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first");
        }
        return user;
    }

    private void requireAdmin(String authorization) {
        if (!"ADMIN".equals(loggedIn(authorization).get("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins only");
        }
    }
}
