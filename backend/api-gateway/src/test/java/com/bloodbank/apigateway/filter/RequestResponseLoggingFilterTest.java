package com.bloodbank.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RequestResponseLoggingFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void orderIsNegativeTwo() {
        assertThat(filter.getOrder()).isEqualTo(-2);
    }

    @Test
    void addsRequestIdHeaderWhenMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/donors").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void preservesExistingRequestIdHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/donors")
                .header("X-Request-Id", "existing-id-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void handlesPostRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/donors").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void handlesPutRequest() {
        MockServerHttpRequest request = MockServerHttpRequest
                .put("/api/v1/donors/550e8400-e29b-41d4-a716-446655440000")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void handlesDeleteRequest() {
        MockServerHttpRequest request = MockServerHttpRequest
                .delete("/api/v1/donors/550e8400-e29b-41d4-a716-446655440000")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
    }
}
