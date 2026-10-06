package com.ssn.cartbackend.controller;

import com.ssn.cartbackend.model.CheckoutRequest;
import com.ssn.cartbackend.model.Coupon;
import com.ssn.cartbackend.model.Order;
import com.ssn.cartbackend.model.User;
import com.ssn.cartbackend.service.AuthService;
import com.ssn.cartbackend.service.CheckoutService;
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
    private final AuthService auth;

    public OrderController(CheckoutService checkout, AuthService auth) {
        this.checkout = checkout;
        this.auth = auth;
    }

    // Body: {"coupon": "SAVE10", "address": {...}, "payment": "UPI"}. Coupon is optional.
    @PostMapping
    public Order placeOrder(@RequestHeader(value = "Authorization", required = false) String authorization,
                            @RequestBody(required = false) CheckoutRequest body) {
        User user = auth.requireUser(authorization);
        return checkout.checkout(user.getId(), user.getUsername(), body);
    }

    @GetMapping
    public List<Order> myOrders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return checkout.history(auth.requireUser(authorization).getId());
    }

    // Admin only: every order, the sales chart numbers, and moving an order along.
    @GetMapping("/all")
    public List<Order> allOrders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        auth.requireAdmin(authorization);
        return checkout.all();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(@RequestHeader(value = "Authorization", required = false) String authorization) {
        auth.requireAdmin(authorization);
        return checkout.stats();
    }

    @PutMapping("/{id}/status")
    public Order setStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
                           @PathVariable String id, @RequestBody Map<String, String> body) {
        auth.requireAdmin(authorization);
        return checkout.setStatus(id, body.get("status"));
    }

    // Public: the cart and the Offers page read the list.
    @GetMapping("/coupons")
    public List<Coupon> coupons() {
        return checkout.listCoupons();
    }

    @PostMapping("/coupons")
    public Coupon addCoupon(@RequestHeader(value = "Authorization", required = false) String authorization,
                            @RequestBody Coupon body) {
        auth.requireAdmin(authorization);
        return checkout.saveCoupon(body.getCode(), body.getPercent());
    }

    @DeleteMapping("/coupons/{code}")
    public void deleteCoupon(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @PathVariable String code) {
        auth.requireAdmin(authorization);
        checkout.deleteCoupon(code);
    }
}
