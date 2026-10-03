package com.ssn.cartbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;
    private double price;
    private int quantity;

    // Shown as a category chip on the storefront, e.g. "Electronics".
    private String category;

    // 0 means no sale. Cart Service applies this when the item is added.
    private int discountPercent;

    // Product page details. Empty lists mean the product has no such option.
    private String description = "";
    private List<String> sizes = new ArrayList<>();
    private List<String> colors = new ArrayList<>();
    private Map<String, String> specs = new LinkedHashMap<>();
    private List<Review> reviews = new ArrayList<>();

    // Short constructor used by the seed data.
    public Product(String id, String name, double price, int quantity, String category, int discountPercent) {
        this(id, name, price, quantity, category, discountPercent, "", new ArrayList<>(), new ArrayList<>(),
                new LinkedHashMap<>(), new ArrayList<>());
    }
}
