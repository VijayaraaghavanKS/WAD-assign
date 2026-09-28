package com.ssn.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Shape of the JSON returned by Product Service's GET /api/products/{id}.
// Cart Service does not share Product Service's database, only its REST API.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String id;
    private String name;
    private double price;
}
