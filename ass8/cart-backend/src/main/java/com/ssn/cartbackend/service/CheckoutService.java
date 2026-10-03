package com.ssn.cartbackend.service;

import com.ssn.cartbackend.model.CartItem;
import com.ssn.cartbackend.model.Order;
import com.ssn.cartbackend.model.OrderLine;
import com.ssn.cartbackend.model.Product;
import com.ssn.cartbackend.repository.CartRepository;
import com.ssn.cartbackend.repository.OrderRepository;
import com.ssn.cartbackend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {

    private final CartRepository carts;
    private final ProductRepository products;
    private final OrderRepository orders;

    public CheckoutService(CartRepository carts, ProductRepository products, OrderRepository orders) {
        this.carts = carts;
        this.products = products;
        this.orders = orders;
    }

    // Turns the user's cart into an order. If one line is out of stock, the lines
    // already taken from stock are put back before the error is returned.
    public Order checkout(String userId) {
        List<CartItem> cart = carts.findByUserId(userId);
        if (cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        List<CartItem> reserved = new ArrayList<>();
        for (CartItem item : cart) {
            if (!reserve(item.getProductId(), item.getQuantity())) {
                reserved.forEach(r -> release(r.getProductId(), r.getQuantity()));
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Some items are out of stock. Please update your cart.");
            }
            reserved.add(item);
        }

        List<OrderLine> lines = cart.stream()
                .map(i -> new OrderLine(i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity()))
                .toList();
        double total = lines.stream().mapToDouble(l -> l.price() * l.quantity()).sum();
        Order order = orders.save(new Order(null, userId, lines, total, Instant.now()));
        carts.deleteByUserId(userId);
        return order;
    }

    public List<Order> history(String userId) {
        return orders.findByUserIdOrderByPlacedAtDesc(userId);
    }

    private boolean reserve(String productId, int quantity) {
        Product product = products.findById(productId).orElse(null);
        if (product == null || product.getQuantity() < quantity) return false;
        product.setQuantity(product.getQuantity() - quantity);
        products.save(product);
        return true;
    }

    private void release(String productId, int quantity) {
        products.findById(productId).ifPresent(p -> {
            p.setQuantity(p.getQuantity() + quantity);
            products.save(p);
        });
    }
}
