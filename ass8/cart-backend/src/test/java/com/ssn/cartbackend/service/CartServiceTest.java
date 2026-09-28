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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for CartService with the repositories mocked out -- no real
// MongoDB needed. This is what proves the cart's business rules (merge on
// duplicate add, total calculation) independent of the database.
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, productRepository);
    }

    @Test
    void addToCart_newProduct_createsCartItemFromProduct() {
        Product product = new Product("p1", "Laptop", 55000);
        when(cartRepository.findByProductId("p1")).thenReturn(null);
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem result = cartService.addToCart("p1");

        assertThat(result.getProductId()).isEqualTo("p1");
        assertThat(result.getProductName()).isEqualTo("Laptop");
        assertThat(result.getPrice()).isEqualTo(55000);
        assertThat(result.getQuantity()).isEqualTo(1);
    }

    @Test
    void addToCart_existingProduct_incrementsQuantityInsteadOfDuplicating() {
        CartItem existing = new CartItem("c1", "p1", "Laptop", 55000, 2);
        when(cartRepository.findByProductId("p1")).thenReturn(existing);
        when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem result = cartService.addToCart("p1");

        assertThat(result.getQuantity()).isEqualTo(3);
        verify(productRepository, never()).findById(any());
    }

    @Test
    void getTotal_sumsPriceTimesQuantityAcrossAllItems() {
        when(cartRepository.findAll()).thenReturn(List.of(
                new CartItem("c1", "p1", "Laptop", 55000, 1),
                new CartItem("c2", "p2", "Mouse", 700, 2)
        ));

        double total = cartService.getTotal();

        assertThat(total).isEqualTo(55000 + 700 * 2);
    }

    @Test
    void updateQuantity_setsNewQuantityAndSaves() {
        CartItem item = new CartItem("c1", "p1", "Laptop", 55000, 1);
        when(cartRepository.findById("c1")).thenReturn(Optional.of(item));
        when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem result = cartService.updateQuantity("c1", 5);

        assertThat(result.getQuantity()).isEqualTo(5);
    }

    @Test
    void removeItem_delegatesToRepositoryDeleteById() {
        cartService.removeItem("c1");
        verify(cartRepository).deleteById("c1");
    }

    @Test
    void clearCart_delegatesToRepositoryDeleteAll() {
        cartService.clearCart();
        verify(cartRepository).deleteAll();
    }
}
