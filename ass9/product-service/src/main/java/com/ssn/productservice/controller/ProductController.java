package com.ssn.productservice.controller;

import com.ssn.productservice.model.Product;
import com.ssn.productservice.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Product Service: owns the "products" collection. Cart Service calls
// GET /api/products/{id} over HTTP to look up a product's name/price.
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
    public Product getProduct(@PathVariable String id) {
        return repository.findById(id).orElse(null);
    }

    // Admin interface: add a new product to the catalog
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return repository.save(product);
    }

    // Admin interface: edit name/price/stock of an existing product
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id, @RequestBody Product updated) {
        return repository.findById(id).map(product -> {
            product.setName(updated.getName());
            product.setPrice(updated.getPrice());
            product.setQuantity(updated.getQuantity());
            return repository.save(product);
        }).orElse(null);
    }

    // Admin interface: remove a product from the catalog
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable String id) {
        repository.deleteById(id);
        return "Product deleted";
    }
}
