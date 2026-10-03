package com.ssn.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
}
