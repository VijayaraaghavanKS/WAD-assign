package com.ssn.cartservice.controller;

import com.ssn.cartservice.model.CartItem;
import com.ssn.cartservice.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Flow: Vue frontend -> Cart Service (this controller) -> Product Service (REST call)
//       -> Cart Service saves the cart line to its own MongoDB (CartServiceDB).
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public List<CartItem> getCart() {
        return service.getCart();
    }

    @GetMapping("/total")
    public double getTotal() {
        return service.getTotal();
    }

    @PostMapping("/add/{productId}")
    public CartItem addToCart(@PathVariable String productId) {
        return service.addToCart(productId);
    }

    @PutMapping("/{cartItemId}")
    public CartItem updateQuantity(@PathVariable String cartItemId, @RequestBody Map<String, Integer> body) {
        return service.updateQuantity(cartItemId, body.get("quantity"));
    }

    @DeleteMapping("/{cartItemId}")
    public String removeItem(@PathVariable String cartItemId) {
        service.removeItem(cartItemId);
        return "Item removed";
    }

    @DeleteMapping
    public String clearCart() {
        service.clearCart();
        return "Cart cleared";
    }
}
