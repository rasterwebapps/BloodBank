package com.bloodbank.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void corsWebFilterIsCreated() {
        CorsConfig corsConfig = new CorsConfig();
        try {
            var field = CorsConfig.class.getDeclaredField("allowedOrigins");
            field.setAccessible(true);
            field.set(corsConfig, "http://localhost:4200");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        CorsWebFilter filter = corsConfig.corsWebFilter();
        assertThat(filter).isNotNull();
    }

    @Test
    void corsWebFilterWithMultipleOrigins() {
        CorsConfig corsConfig = new CorsConfig();
        try {
            var field = CorsConfig.class.getDeclaredField("allowedOrigins");
            field.setAccessible(true);
            field.set(corsConfig, "http://localhost:4200,https://bloodbank.com");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        CorsWebFilter filter = corsConfig.corsWebFilter();
        assertThat(filter).isNotNull();
    }
}
