package com.ssn.cartservice.service;

import com.ssn.cartservice.model.CartItem;
import com.ssn.cartservice.model.ProductDto;
import com.ssn.cartservice.repository.CartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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

    public CartItem addToCart(String userId, String productId) {
        return addToCart(userId, productId, null, null);
    }

    // Cart Service asks Product Service for the price (and any sale) before saving a line.
    // Size and colour are part of the line: the same product in two sizes is two lines.
    public CartItem addToCart(String userId, String productId, String size, String colour) {
        size = blankToNull(size);
        colour = blankToNull(colour);
        ProductDto product = fetchProduct(productId);
        CartItem existing = cartRepository.findByUserIdAndProductIdAndSizeAndColour(userId, productId, size, colour);
        int wanted = existing == null ? 1 : existing.getQuantity() + 1;
        requireStock(product, wanted);
        if (existing != null) {
            existing.setQuantity(wanted);
            return cartRepository.save(existing);
        }

        double salePrice = product.getPrice() * (100 - product.getDiscountPercent()) / 100.0;
        return cartRepository.save(new CartItem(null, userId, product.getId(), product.getName(), salePrice, 1, size, colour));
    }

    private ProductDto fetchProduct(String productId) {
        return restTemplate.getForObject(productServiceUrl + "/api/products/" + productId, ProductDto.class);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    // Refuses a quantity the shop cannot supply, so the shopper hears about it at the cart.
    private static void requireStock(ProductDto product, int wanted) {
        if (wanted > product.getQuantity()) {
            String message = product.getQuantity() == 0 ? "Out of stock" : "Only " + product.getQuantity() + " left";
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
    }

    public CartItem updateQuantity(String userId, String cartItemId, int quantity) {
        CartItem item = cartRepository.findById(cartItemId).filter(i -> i.getUserId().equals(userId)).orElseThrow();
        if (quantity > item.getQuantity()) {
            requireStock(fetchProduct(item.getProductId()), quantity);
        }
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
