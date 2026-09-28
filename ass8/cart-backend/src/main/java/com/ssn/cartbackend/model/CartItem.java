package com.ssn.cartbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// One document per product currently in the cart. Price/name are copied from
// the Product at add-time so the cart still shows correct totals.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart")
public class CartItem {

    @Id
    private String id;

    private String productId;
    private String productName;
    private double price;
    private int quantity;
}
