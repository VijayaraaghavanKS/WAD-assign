package com.ssn.apigateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

// Passes the request on unchanged (method, path, query, body, token) and returns the reply unchanged.
// Dev endpoints are not routed here: the Developer console asks each service directly.
@RestController
@CrossOrigin(origins = "*")
public class GatewayController {

    private final RestClient http = RestClient.create();
    private final String userUrl;
    private final String productUrl;
    private final String cartUrl;
    private final String orderUrl;

    public GatewayController(@Value("${user.service.url}") String userUrl,
                             @Value("${product.service.url}") String productUrl,
                             @Value("${cart.service.url}") String cartUrl,
                             @Value("${order.service.url}") String orderUrl) {
        this.userUrl = userUrl;
        this.productUrl = productUrl;
        this.cartUrl = cartUrl;
        this.orderUrl = orderUrl;
    }

    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> forward(HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        String base = baseFor(path);
        if (base == null) {
            return ResponseEntity.notFound().build();
        }
        String query = request.getQueryString();
        String target = base + path + (query == null ? "" : "?" + query);
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        String type = request.getContentType();

        return http.method(HttpMethod.valueOf(request.getMethod()))
                .uri(target)
                .headers(h -> {
                    if (token != null) h.set(HttpHeaders.AUTHORIZATION, token);
                    if (type != null) h.set(HttpHeaders.CONTENT_TYPE, type);
                })
                .body(body == null ? new byte[0] : body)
                .exchange((req, res) -> ResponseEntity.status(res.getStatusCode())
                        .headers(h -> h.setContentType(res.getHeaders().getContentType()))
                        .body(res.getBody().readAllBytes()));
    }

    // The service that owns a path, or null when no service does.
    String baseFor(String path) {
        if (path.startsWith("/api/auth")) return userUrl;
        if (path.startsWith("/api/products")) return productUrl;
        if (path.startsWith("/api/cart")) return cartUrl;
        if (path.startsWith("/api/orders")) return orderUrl;
        return null;
    }
}
