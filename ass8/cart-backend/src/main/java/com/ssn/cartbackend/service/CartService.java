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

    public CartItem addToCart(String userId, String productId) {
        return addToCart(userId, productId, null, null);
    }

    // A new product copies name and sale price from the catalog. An existing one only gets +1.
    // Size and colour are part of the line: the same product in two sizes is two lines.
    public CartItem addToCart(String userId, String productId, String size, String colour) {
        size = blankToNull(size);
        colour = blankToNull(colour);
        Product product = products.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such product"));
        CartItem existing = carts.findByUserIdAndProductIdAndSizeAndColour(userId, productId, size, colour);
        int wanted = existing == null ? 1 : existing.getQuantity() + 1;
        requireStock(product, wanted);
        if (existing != null) {
            existing.setQuantity(wanted);
            return carts.save(existing);
        }

        double salePrice = product.getPrice() * (100 - product.getDiscountPercent()) / 100.0;
        return carts.save(new CartItem(null, userId, product.getId(), product.getName(), salePrice, 1, size, colour));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    // Refuses a quantity the shop cannot supply, so the shopper hears about it at the cart.
    private static void requireStock(Product product, int wanted) {
        if (wanted > product.getQuantity()) {
            String message = product.getQuantity() == 0 ? "Out of stock" : "Only " + product.getQuantity() + " left";
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
    }

    public CartItem updateQuantity(String userId, String cartItemId, int quantity) {
        CartItem item = carts.findById(cartItemId)
                .filter(i -> i.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such cart item"));
        if (quantity > item.getQuantity()) {
            Product product = products.findById(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such product"));
            requireStock(product, quantity);
        }
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
