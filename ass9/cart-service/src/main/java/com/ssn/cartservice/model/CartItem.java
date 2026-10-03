package com.ssn.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// One row per (user, product, size, colour). Name and price are copied from Product Service
// when the item is added, since this service never reads the products collection.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart")
public class CartItem {

    @Id
    private String id;

    private String userId;
    private String productId;
    private String productName;
    private double price; // already discounted
    private int quantity;
    private String size;    // null when the product has no sizes
    private String colour;  // null when the product has no colours
}
