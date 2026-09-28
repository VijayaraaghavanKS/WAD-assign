package com.ssn.cartbackend.service;

import com.ssn.cartbackend.model.CartItem;
import com.ssn.cartbackend.model.Product;
import com.ssn.cartbackend.repository.CartRepository;
import com.ssn.cartbackend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> getCart() {
        return cartRepository.findAll();
    }

    public double getTotal() {
        return cartRepository.findAll().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    // Adding a product that's already in the cart just bumps its quantity
    // instead of creating a duplicate cart row.
    public CartItem addToCart(String productId) {
        CartItem existing = cartRepository.findByProductId(productId);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            return cartRepository.save(existing);
        }

        Product product = productRepository.findById(productId).orElseThrow();
        CartItem item = new CartItem(null, product.getId(), product.getName(), product.getPrice(), 1);
        return cartRepository.save(item);
    }

    public CartItem updateQuantity(String cartItemId, int quantity) {
        CartItem item = cartRepository.findById(cartItemId).orElseThrow();
        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    public void removeItem(String cartItemId) {
        cartRepository.deleteById(cartItemId);
    }

    public void clearCart() {
        cartRepository.deleteAll();
    }
}
