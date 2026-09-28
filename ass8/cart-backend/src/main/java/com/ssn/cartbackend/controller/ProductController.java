package com.ssn.cartbackend.controller;

import com.ssn.cartbackend.model.Product;
import com.ssn.cartbackend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin interface: add a new product to the catalog
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty() || product.getPrice() < 0) {
            return ResponseEntity.badRequest().body("Product name is required and price must be non-negative");
        }
        Product saved = repository.save(product);
        return ResponseEntity.ok(saved);
    }

    // Admin interface: edit name/price of an existing product
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product updated) {
        return repository.findById(id).map(product -> {
            if (updated.getName() != null) {
                product.setName(updated.getName());
            }
            if (updated.getPrice() >= 0) {
                product.setPrice(updated.getPrice());
            }
            if (updated.getQuantity() >= 0) {
                product.setQuantity(updated.getQuantity());
            }
            return ResponseEntity.ok(repository.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Admin interface: remove a product from the catalog
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok("Product deleted");
    }
}

