package com.ssn.cartservice.controller;

import com.ssn.cartservice.model.CartItem;
import com.ssn.cartservice.security.AuthClient;
import com.ssn.cartservice.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

// Every endpoint needs a logged-in token. The cart is always the caller's own.
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService service;
    private final AuthClient auth;

    public CartController(CartService service, AuthClient auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    public List<CartItem> getCart(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.getCart(userId(authorization));
    }

    @GetMapping("/total")
    public double getTotal(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.getTotal(userId(authorization));
    }

    @PostMapping("/add/{productId}")
    public CartItem addToCart(@RequestHeader(value = "Authorization", required = false) String authorization,
                              @PathVariable String productId) {
        return service.addToCart(userId(authorization), productId);
    }

    @PutMapping("/{cartItemId}")
    public CartItem updateQuantity(@RequestHeader(value = "Authorization", required = false) String authorization,
                                   @PathVariable String cartItemId, @RequestBody Map<String, Integer> body) {
        return service.updateQuantity(userId(authorization), cartItemId, body.get("quantity"));
    }

    @DeleteMapping("/{cartItemId}")
    public Map<String, String> removeItem(@RequestHeader(value = "Authorization", required = false) String authorization,
                                          @PathVariable String cartItemId) {
        service.removeItem(userId(authorization), cartItemId);
        return Map.of("message", "Item removed");
    }

    @DeleteMapping
    public Map<String, String> clearCart(@RequestHeader(value = "Authorization", required = false) String authorization) {
        service.clearCart(userId(authorization));
        return Map.of("message", "Cart cleared");
    }

    private String userId(String authorization) {
        Map<String, String> user = auth.currentUser(authorization);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first");
        }
        return user.get("id");
    }
}
