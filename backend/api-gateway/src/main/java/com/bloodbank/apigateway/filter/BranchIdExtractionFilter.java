package com.bloodbank.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Extracts branch_id from JWT claims and adds it as an X-Branch-Id header
 * to downstream service requests. This is the first layer of branch isolation
 * in the 4-layer branch data filtering architecture.
 */
@Component
public class BranchIdExtractionFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(BranchIdExtractionFilter.class);

    private static final String BRANCH_ID_CLAIM = "branch_id";
    private static final String BRANCH_ID_HEADER = "X-Branch-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtAuth -> {
                    Object branchId = jwtAuth.getToken().getClaim(BRANCH_ID_CLAIM);
                    if (branchId instanceof String branchIdStr && !branchIdStr.isBlank()) {
                        log.debug("Extracted branch_id from JWT: {}", branchIdStr);
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header(BRANCH_ID_HEADER, branchIdStr)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }
                    log.debug("No branch_id claim found in JWT");
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
