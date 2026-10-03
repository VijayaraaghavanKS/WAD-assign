package com.ssn.cartbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// One row per (user, product). Name and sale price are copied from the Product
// when the item is added, so later catalog edits don't change what's in the cart.
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
