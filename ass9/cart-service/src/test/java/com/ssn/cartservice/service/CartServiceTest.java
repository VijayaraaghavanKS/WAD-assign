package com.ssn.cartservice.service;

import com.ssn.cartservice.model.CartItem;
import com.ssn.cartservice.model.ProductDto;
import com.ssn.cartservice.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// A new product triggers one call to Product Service; a product already in
// the user's cart only bumps its quantity.
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestTemplate restTemplate;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, restTemplate);
        ReflectionTestUtils.setField(cartService, "productServiceUrl", "http://localhost:8083");
    }

    @Test
    void addToCart_newProduct_appliesDiscountAndSaves() {
        when(cartRepository.findByUserIdAndProductId("u1", "p1")).thenReturn(null);
        when(restTemplate.getForObject(eq("http://localhost:8083/api/products/p1"), eq(ProductDto.class)))
                .thenReturn(new ProductDto("p1", "Laptop", 50000, 10));
        when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem result = cartService.addToCart("u1", "p1");

        assertThat(result.getProductName()).isEqualTo("Laptop");
        assertThat(result.getPrice()).isEqualTo(45000);
        assertThat(result.getQuantity()).isEqualTo(1);
    }

    @Test
    void addToCart_existingProduct_skipsProductServiceCall() {
        CartItem existing = new CartItem("c1", "u1", "p1", "Laptop", 55000, 1);
        when(cartRepository.findByUserIdAndProductId("u1", "p1")).thenReturn(existing);
        when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem result = cartService.addToCart("u1", "p1");

        assertThat(result.getQuantity()).isEqualTo(2);
        verify(restTemplate, never()).getForObject(any(String.class), eq(ProductDto.class));
    }
}
