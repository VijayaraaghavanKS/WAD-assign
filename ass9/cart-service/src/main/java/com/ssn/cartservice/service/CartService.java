package com.ssn.cartservice.service;

import com.ssn.cartservice.model.CartItem;
import com.ssn.cartservice.model.ProductDto;
import com.ssn.cartservice.repository.CartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    public CartService(CartRepository cartRepository, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.restTemplate = restTemplate;
    }

    public List<CartItem> getCart(String userId) {
        return cartRepository.findByUserId(userId);
    }

    public double getTotal(String userId) {
        return getCart(userId).stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    // Cart Service asks Product Service for the price (and any sale) before saving a line.
    public CartItem addToCart(String userId, String productId) {
        CartItem existing = cartRepository.findByUserIdAndProductId(userId, productId);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            return cartRepository.save(existing);
        }

        ProductDto product = restTemplate.getForObject(
                productServiceUrl + "/api/products/" + productId, ProductDto.class);
        double salePrice = product.getPrice() * (100 - product.getDiscountPercent()) / 100.0;

        return cartRepository.save(new CartItem(null, userId, product.getId(), product.getName(), salePrice, 1));
    }

    public CartItem updateQuantity(String userId, String cartItemId, int quantity) {
        CartItem item = cartRepository.findById(cartItemId).filter(i -> i.getUserId().equals(userId)).orElseThrow();
        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    public void removeItem(String userId, String cartItemId) {
        cartRepository.deleteByIdAndUserId(cartItemId, userId);
    }

    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
