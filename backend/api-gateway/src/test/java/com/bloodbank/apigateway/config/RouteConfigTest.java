package com.bloodbank.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class RouteConfigTest {

    @Test
    void defaultCircuitBreakerCustomizerIsNotNull() {
        RouteConfig routeConfig = new RouteConfig();
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer =
                routeConfig.defaultCircuitBreakerCustomizer();
        assertThat(customizer).isNotNull();
    }
}
