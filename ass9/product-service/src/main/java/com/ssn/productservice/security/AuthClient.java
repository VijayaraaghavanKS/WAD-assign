package com.ssn.productservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// Asks User Service who owns a token. Returns null when the token is not valid.
@Component
public class AuthClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${user.service.url}")
    private String userServiceUrl;

    public Map<String, String> currentUser(String authorization) {
        if (authorization == null) return null;
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        try {
            return rest.exchange(userServiceUrl + "/api/auth/me", HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class).getBody();
        } catch (HttpClientErrorException e) {
            return null;
        }
    }
}
