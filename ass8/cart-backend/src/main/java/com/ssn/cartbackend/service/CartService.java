package com.ssn.cartbackend.service;

import com.ssn.cartbackend.model.CartItem;
import com.ssn.cartbackend.model.Product;
import com.ssn.cartbackend.repository.CartRepository;
import com.ssn.cartbackend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CartService {

    private final CartRepository carts;
    private final ProductRepository products;

    public CartService(CartRepository carts, ProductRepository products) {
        this.carts = carts;
        this.products = products;
    }

    public List<CartItem> getCart(String userId) {
        return carts.findByUserId(userId);
    }

    // A new product copies name and sale price from the catalog. An existing one only gets +1.
    public CartItem addToCart(String userId, String productId) {
        CartItem existing = carts.findByUserIdAndProductId(userId, productId);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            return carts.save(existing);
        }

        Product product = products.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such product"));
        double salePrice = product.getPrice() * (100 - product.getDiscountPercent()) / 100.0;
        return carts.save(new CartItem(null, userId, product.getId(), product.getName(), salePrice, 1));
    }

    public CartItem updateQuantity(String userId, String cartItemId, int quantity) {
        CartItem item = carts.findById(cartItemId)
                .filter(i -> i.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such cart item"));
        item.setQuantity(quantity);
        return carts.save(item);
    }

    public void removeItem(String userId, String cartItemId) {
        carts.deleteByIdAndUserId(cartItemId, userId);
    }

    public void clearCart(String userId) {
        carts.deleteByUserId(userId);
    }
}
