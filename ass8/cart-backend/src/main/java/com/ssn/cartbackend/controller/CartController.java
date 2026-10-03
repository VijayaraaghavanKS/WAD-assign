package com.ssn.cartbackend.controller;

import com.ssn.cartbackend.model.CartItem;
import com.ssn.cartbackend.service.AuthService;
import com.ssn.cartbackend.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Every endpoint needs a logged-in token. The cart is always the caller's own.
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService service;
    private final AuthService auth;

    public CartController(CartService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    public List<CartItem> getCart(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.getCart(auth.requireUser(authorization).getId());
    }

    @PostMapping("/add/{productId}")
    public CartItem addToCart(@RequestHeader(value = "Authorization", required = false) String authorization,
                              @PathVariable String productId) {
        return service.addToCart(auth.requireUser(authorization).getId(), productId);
    }

    @PutMapping("/{cartItemId}")
    public CartItem updateQuantity(@RequestHeader(value = "Authorization", required = false) String authorization,
                                   @PathVariable String cartItemId, @RequestBody Map<String, Integer> body) {
        return service.updateQuantity(auth.requireUser(authorization).getId(), cartItemId, body.get("quantity"));
    }

    @DeleteMapping("/{cartItemId}")
    public Map<String, String> removeItem(@RequestHeader(value = "Authorization", required = false) String authorization,
                                          @PathVariable String cartItemId) {
        service.removeItem(auth.requireUser(authorization).getId(), cartItemId);
        return Map.of("message", "Item removed");
    }

    @DeleteMapping
    public Map<String, String> clearCart(@RequestHeader(value = "Authorization", required = false) String authorization) {
        service.clearCart(auth.requireUser(authorization).getId());
        return Map.of("message", "Cart cleared");
    }
}
