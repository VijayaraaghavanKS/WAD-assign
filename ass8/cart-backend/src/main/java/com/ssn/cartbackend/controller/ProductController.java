package com.ssn.cartbackend.controller;

import com.ssn.cartbackend.model.Product;
import com.ssn.cartbackend.model.Review;
import com.ssn.cartbackend.model.User;
import com.ssn.cartbackend.repository.ProductRepository;
import com.ssn.cartbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository products;
    private final AuthService auth;

    public ProductController(ProductRepository products, AuthService auth) {
        this.products = products;
        this.auth = auth;
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String category) {
        return category == null ? products.findAll() : products.findByCategory(category);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such product"));
    }

    @PostMapping
    public Product createProduct(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @RequestBody Product product) {
        auth.requireAdmin(authorization);
        return products.save(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @PathVariable String id, @RequestBody Product updated) {
        auth.requireAdmin(authorization);
        Product product = getProduct(id);
        product.setName(updated.getName());
        product.setPrice(updated.getPrice());
        product.setQuantity(updated.getQuantity());
        product.setCategory(updated.getCategory());
        product.setDiscountPercent(updated.getDiscountPercent());
        return products.save(product);
    }

    // Any logged-in shopper can review a product. One review is added per call.
    @PostMapping("/{id}/reviews")
    public Product addReview(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @PathVariable String id, @RequestBody Map<String, Object> body) {
        User user = auth.requireUser(authorization);
        int rating = ((Number) body.getOrDefault("rating", 0)).intValue();
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be 1 to 5");
        }
        Product product = getProduct(id);
        product.getReviews().add(0, new Review(user.getUsername(), rating,
                String.valueOf(body.getOrDefault("title", "")), String.valueOf(body.getOrDefault("body", "")), Instant.now()));
        return products.save(product);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @PathVariable String id) {
        auth.requireAdmin(authorization);
        products.deleteById(id);
        return Map.of("message", "Product deleted");
    }
}
