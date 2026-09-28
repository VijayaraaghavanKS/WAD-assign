package com.ssn.cartbackend.controller;

import com.ssn.cartbackend.model.Product;
import com.ssn.cartbackend.repository.ProductRepository;
import com.ssn.cartbackend.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest loads only the web layer (this controller + Spring MVC), with
// the repository mocked out -- verifies the HTTP contract (status codes,
// JSON shape) without needing a real MongoDB connection.
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository repository;

    // RequestLoggingFilter (a @Component Filter) gets pulled into the
    // @WebMvcTest slice automatically; it needs this bean to construct even
    // though this test doesn't exercise logging behavior.
    @MockitoBean
    private RequestLogRepository requestLogRepository;

    @Test
    void getAllProducts_returnsJsonArray() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
                new Product("p1", "Laptop", 55000, 10),
                new Product("p2", "Mouse", 700, 25)
        ));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(55000.0))
                .andExpect(jsonPath("$[1].name").value("Mouse"))
                .andExpect(jsonPath("$[1].price").value(700.0));
    }

    @Test
    void getProductById_validId_returnsProduct() throws Exception {
        Product product = new Product("p1", "Laptop", 55000, 10);
        when(repository.findById("p1")).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(55000.0));
    }

    @Test
    void getProductById_invalidId_returnsNotFound() throws Exception {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_validData_savesAndReturnsIt() throws Exception {
        Product saved = new Product("p3", "Webcam", 3200, 15);
        when(repository.save(any(Product.class))).thenReturn(saved);

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"Webcam\",\"price\":3200,\"quantity\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p3"))
                .andExpect(jsonPath("$.name").value("Webcam"))
                .andExpect(jsonPath("$.price").value(3200.0))
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    void createProduct_missingOrEmptyName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"price\":3200,\"quantity\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_negativePrice_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"Invalid Item\",\"price\":-100,\"quantity\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_existingProduct_updatesAndReturnsIt() throws Exception {
        Product existing = new Product("p1", "Laptop", 55000, 10);
        when(repository.findById("p1")).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/products/p1")
                        .contentType("application/json")
                        .content("{\"name\":\"Laptop Pro\",\"price\":60000,\"quantity\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop Pro"))
                .andExpect(jsonPath("$.price").value(60000.0))
                .andExpect(jsonPath("$.quantity").value(12));
    }

    @Test
    void updateProduct_nonExistentProduct_returnsNotFound() throws Exception {
        when(repository.findById("invalid-id")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/products/invalid-id")
                        .contentType("application/json")
                        .content("{\"name\":\"NonExistent\",\"price\":1000,\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_existingProduct_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/products/p1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted"));

        verify(repository).deleteById(eq("p1"));
    }

    @Test
    void verifyDeletedProduct_cannotBeRetrieved() throws Exception {
        // Step 1: Delete product
        mockMvc.perform(delete("/api/products/p1"))
                .andExpect(status().isOk());

        // Step 2: Attempt retrieval returns 404 Not Found
        when(repository.findById("p1")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/products/p1"))
                .andExpect(status().isNotFound());
    }
}
