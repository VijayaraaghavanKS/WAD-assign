package com.ssn.cartbackend.service;

import com.ssn.cartbackend.model.CartItem;
import com.ssn.cartbackend.model.Product;
import com.ssn.cartbackend.repository.CartRepository;
import com.ssn.cartbackend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// A new product copies its name and sale price into the cart. Adding a product
// that's already in the cart only bumps its quantity.
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository carts;

    @Mock
    private ProductRepository products;

    private CartService service;

    @BeforeEach
    void setUp() {
        service = new CartService(carts, products);
    }

    @Test
    void addToCart_newProduct_appliesSalePrice() {
        when(carts.findByUserIdAndProductId("u1", "p1")).thenReturn(null);
        when(products.findById("p1")).thenReturn(Optional.of(new Product("p1", "Laptop", 50000, 5, "Electronics", 10)));
        when(carts.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem result = service.addToCart("u1", "p1");

        assertThat(result.getProductName()).isEqualTo("Laptop");
        assertThat(result.getPrice()).isEqualTo(45000);
        assertThat(result.getQuantity()).isEqualTo(1);
    }

    @Test
    void addToCart_existingProduct_incrementsQuantity() {
        CartItem existing = new CartItem("c1", "u1", "p1", "Laptop", 55000, 2);
        when(carts.findByUserIdAndProductId("u1", "p1")).thenReturn(existing);
        when(carts.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.addToCart("u1", "p1").getQuantity()).isEqualTo(3);
        verify(products, never()).findById(any());
    }
}
