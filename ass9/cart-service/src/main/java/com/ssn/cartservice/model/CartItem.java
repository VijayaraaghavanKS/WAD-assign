package com.ssn.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// productName/price are copied in from Product Service's response at add-time,
// since Cart Service does not own or query the products collection directly.
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
