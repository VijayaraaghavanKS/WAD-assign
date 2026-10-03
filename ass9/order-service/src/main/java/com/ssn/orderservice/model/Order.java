package com.ssn.orderservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

// status moves PLACED -> PACKED -> SHIPPED -> DELIVERED. Admins change it.
// The money fields make the invoice: subtotal - discount + deliveryFee = total.
// GST (tax) is already inside the total, as Indian invoices show it.
@Data
@NoArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private String userId;
    private String username;
    private List<OrderLine> items;
    private double subtotal;
    private double discount;
    private String couponCode;
    private double deliveryFee;
    private double tax;
    private double total;
    private String status;
    private Instant placedAt;
    private Address shippingAddress;
    private String paymentMethod;
    private String invoiceNumber;
}
