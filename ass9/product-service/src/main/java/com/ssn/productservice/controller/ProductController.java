package com.ssn.productservice.controller;

import com.ssn.productservice.model.Product;
import com.ssn.productservice.model.Review;
import com.ssn.productservice.repository.ProductRepository;
import com.ssn.productservice.security.AuthClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// Product Service: owns the "products" collection. Cart and Order services call
// it over REST. Writes need an ADMIN token; the reserve/release endpoints are
// only called by Order Service during checkout.
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository repository;
    private final AuthClient auth;

    public ProductController(ProductRepository repository, AuthClient auth) {
        this.repository = repository;
        this.auth = auth;
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String category) {
        return category == null ? repository.findAll() : repository.findByCategory(category);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such product"));
    }

    @PostMapping
    public Product createProduct(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @RequestBody Product product) {
        requireAdmin(authorization);
        return repository.save(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @PathVariable String id, @RequestBody Product updated) {
        requireAdmin(authorization);
        Product product = getProduct(id);
        product.setName(updated.getName());
        product.setPrice(updated.getPrice());
        product.setQuantity(updated.getQuantity());
        product.setCategory(updated.getCategory());
        product.setDiscountPercent(updated.getDiscountPercent());
        return repository.save(product);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @PathVariable String id) {
        requireAdmin(authorization);
        repository.deleteById(id);
        return Map.of("message", "Product deleted");
    }

    // Internal: Order Service calls this at checkout. Fails with 409 if stock ran out.
    // Any logged-in shopper can review a product. One review is added per call.
    @PostMapping("/{id}/reviews")
    public Product addReview(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @PathVariable String id, @RequestBody Map<String, Object> body) {
        Map<String, String> user = auth.currentUser(authorization);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in to review");
        }
        int rating = ((Number) body.getOrDefault("rating", 0)).intValue();
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be 1 to 5");
        }
        Product product = getProduct(id);
        product.getReviews().add(0, new Review(user.get("username"), rating,
                String.valueOf(body.getOrDefault("title", "")), String.valueOf(body.getOrDefault("body", "")), Instant.now()));
        return repository.save(product);
    }

    @PostMapping("/{id}/reserve")
    public Product reserve(@PathVariable String id, @RequestBody Map<String, Integer> body) {
        return changeStock(id, -body.get("quantity"));
    }

    // Internal: Order Service calls this to undo a reservation when checkout fails partway.
    @PostMapping("/{id}/release")
    public Product release(@PathVariable String id, @RequestBody Map<String, Integer> body) {
        return changeStock(id, body.get("quantity"));
    }

    private Product changeStock(String id, int delta) {
        Product product = getProduct(id);
        if (product.getQuantity() + delta < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, product.getName() + " is out of stock");
        }
        product.setQuantity(product.getQuantity() + delta);
        return repository.save(product);
    }

    private void requireAdmin(String authorization) {
        Map<String, String> user = auth.currentUser(authorization);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first");
        }
        if (!"ADMIN".equals(user.get("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins only");
        }
    }
}
