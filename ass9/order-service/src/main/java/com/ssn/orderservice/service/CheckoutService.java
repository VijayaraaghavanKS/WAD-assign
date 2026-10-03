package com.ssn.orderservice.service;

import com.ssn.orderservice.model.Order;
import com.ssn.orderservice.model.OrderLine;
import com.ssn.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutService {

    private final OrderRepository orders;
    private final RestTemplate rest;

    @Value("${cart.service.url}")
    private String cartUrl;

    @Value("${product.service.url}")
    private String productUrl;

    public CheckoutService(OrderRepository orders, RestTemplate rest) {
        this.orders = orders;
        this.rest = rest;
    }

    // Turns the caller's cart into an order. Stock is reserved one line at a time;
    // if one line is out of stock, the lines already reserved are released again.
    public Order checkout(String authorization, String userId) {
        List<OrderLine> lines = fetchCart(authorization);
        if (lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        List<OrderLine> reserved = new ArrayList<>();
        try {
            for (OrderLine line : lines) {
                reserve(line);
                reserved.add(line);
            }
        } catch (HttpClientErrorException e) {
            reserved.forEach(this::release);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Some items are out of stock. Please update your cart.");
        }

        double total = lines.stream().mapToDouble(l -> l.price() * l.quantity()).sum();
        Order order = orders.save(new Order(null, userId, lines, total, Instant.now()));
        clearCart(authorization);
        return order;
    }

    public List<Order> history(String userId) {
        return orders.findByUserIdOrderByPlacedAtDesc(userId);
    }

    private List<OrderLine> fetchCart(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        List<Map<String, Object>> cart = rest.exchange(cartUrl + "/api/cart", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getBody();

        List<OrderLine> lines = new ArrayList<>();
        for (Map<String, Object> item : cart) {
            lines.add(new OrderLine(
                    (String) item.get("productId"),
                    (String) item.get("productName"),
                    ((Number) item.get("price")).doubleValue(),
                    ((Number) item.get("quantity")).intValue()));
        }
        return lines;
    }

    private void reserve(OrderLine line) {
        rest.postForObject(productUrl + "/api/products/" + line.productId() + "/reserve",
                Map.of("quantity", line.quantity()), Map.class);
    }

    private void release(OrderLine line) {
        rest.postForObject(productUrl + "/api/products/" + line.productId() + "/release",
                Map.of("quantity", line.quantity()), Map.class);
    }

    private void clearCart(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        rest.exchange(cartUrl + "/api/cart", HttpMethod.DELETE, new HttpEntity<>(headers), Map.class);
    }
}
