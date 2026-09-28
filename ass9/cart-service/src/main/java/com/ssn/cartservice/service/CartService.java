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

    public List<CartItem> getCart() {
        return cartRepository.findAll();
    }

    public double getTotal() {
        return cartRepository.findAll().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    // Microservice interaction: Cart Service calls Product Service over HTTP
    // (GET /api/products/{id}) to fetch the product's name and price before
    // storing a cart line for it. Cart Service never touches "products" directly.
    public CartItem addToCart(String productId) {
        CartItem existing = cartRepository.findByProductId(productId);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            return cartRepository.save(existing);
        }

        ProductDto product = restTemplate.getForObject(
                productServiceUrl + "/api/products/" + productId,
                ProductDto.class
        );

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
