package com.ssn.productservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

// Asks Order Service whether this shopper has ever bought a product. Drives the "Verified purchase" badge.
@Component
public class OrderClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${order.service.url}")
    private String orderServiceUrl;

    public boolean hasBought(String authorization, String productId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        try {
            List<Map<String, Object>> orders = rest.exchange(orderServiceUrl + "/api/orders", HttpMethod.GET,
                    new HttpEntity<>(headers), new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
            if (orders == null) return false;
            return orders.stream().anyMatch(order -> ((List<Map<String, Object>>) order.getOrDefault("items", List.of())).stream()
                    .anyMatch(item -> productId.equals(item.get("productId"))));
        } catch (RestClientException e) {
            return false;
        }
    }
}
