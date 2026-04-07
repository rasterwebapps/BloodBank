package com.bloodbank.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterConfigTest {

    private final RateLimiterConfig rateLimiterConfig = new RateLimiterConfig();

    @Test
    void userKeyResolverReturnsRemoteAddress() {
        KeyResolver resolver = rateLimiterConfig.userKeyResolver();
        assertThat(resolver).isNotNull();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/donors").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNextMatches(key -> key != null && !key.isEmpty())
                .verifyComplete();
    }
}
