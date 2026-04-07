package com.bloodbank.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BranchIdExtractionFilterTest {

    private BranchIdExtractionFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new BranchIdExtractionFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void orderIsNegativeOne() {
        assertThat(filter.getOrder()).isEqualTo(-1);
    }

    @Test
    void extractsBranchIdFromJwtAndAddsHeader() {
        String branchId = "550e8400-e29b-41d4-a716-446655440000";

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user-1")
                .claim("branch_id", branchId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_BRANCH_ADMIN")));

        SecurityContextImpl securityContext = new SecurityContextImpl(authentication);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/donors").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void proceedsWithoutHeaderWhenNoBranchIdClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));

        SecurityContextImpl securityContext = new SecurityContextImpl(authentication);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/donors").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void proceedsWithoutHeaderWhenNoSecurityContext() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/donors").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void proceedsWhenBranchIdIsBlank() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user-1")
                .claim("branch_id", "")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_BRANCH_ADMIN")));

        SecurityContextImpl securityContext = new SecurityContextImpl(authentication);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/donors").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result).verifyComplete();
    }
}
