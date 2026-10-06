package com.ssn.productservice.controller;

import com.ssn.productservice.model.Product;
import com.ssn.productservice.repository.ProductRepository;
import com.ssn.productservice.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.ssn.productservice.security.AuthClient;
import com.ssn.productservice.security.OrderClient;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository repository;

    @MockitoBean
    private OrderClient orders;

    @MockitoBean
    private AuthClient authClient;

    @MockitoBean
    private RequestLogRepository requestLogRepository;

    @Test
    void getAllProducts_returnsJsonArray() throws Exception {
        when(repository.findAll()).thenReturn(List.of(new Product("p1", "Laptop", 55000, 0, null, 0)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getProduct_whenFound_returnsIt() throws Exception {
        when(repository.findById("p1")).thenReturn(Optional.of(new Product("p1", "Laptop", 55000, 0, null, 0)));

        mockMvc.perform(get("/api/products/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void createProduct_savesAndReturnsIt() throws Exception {
        when(repository.save(any(Product.class))).thenReturn(new Product("p2", "Mouse", 700, 0, null, 0));

        when(authClient.currentUser("Bearer admin-token")).thenReturn(Map.of("role", "ADMIN"));

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer admin-token")
                        .contentType("application/json")
                        .content("{\"name\":\"Mouse\",\"price\":700,\"quantity\":5,\"discountPercent\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mouse"));
    }
}
