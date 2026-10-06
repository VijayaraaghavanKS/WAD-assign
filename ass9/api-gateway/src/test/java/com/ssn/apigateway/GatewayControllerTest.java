package com.ssn.apigateway;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Each path must reach the service that owns it. Unknown paths, dev paths included, are not forwarded.
class GatewayControllerTest {

    private final GatewayController gateway = new GatewayController(
            "http://u", "http://p", "http://c", "http://o");

    @Test
    void routesEachPathToItsService() {
        assertThat(gateway.baseFor("/api/auth/login")).isEqualTo("http://u");
        assertThat(gateway.baseFor("/api/products/p1/reviews")).isEqualTo("http://p");
        assertThat(gateway.baseFor("/api/cart/add/p1")).isEqualTo("http://c");
        assertThat(gateway.baseFor("/api/orders/coupons")).isEqualTo("http://o");
    }

    @Test
    void doesNotRouteDevOrUnknownPaths() {
        assertThat(gateway.baseFor("/api/dev/logs")).isNull();
        assertThat(gateway.baseFor("/api/other")).isNull();
    }
}
