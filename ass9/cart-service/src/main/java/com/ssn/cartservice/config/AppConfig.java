package com.ssn.cartservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // RestTemplate is how Cart Service makes HTTP calls to Product Service.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
