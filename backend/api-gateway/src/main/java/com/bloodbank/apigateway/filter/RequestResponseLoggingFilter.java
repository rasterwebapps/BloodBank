package com.bloodbank.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Logs incoming requests and outgoing responses for observability.
 * Adds a unique X-Request-Id header to each request for distributed tracing.
 */
@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        log.info("Incoming request: {} {} [requestId={}] from {}",
                request.getMethod(),
                request.getURI().getPath(),
                requestId,
                request.getRemoteAddress());

        final String finalRequestId = requestId;

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add(REQUEST_ID_HEADER, finalRequestId);
                    log.info("Outgoing response: {} for {} {} [requestId={}]",
                            response.getStatusCode(),
                            request.getMethod(),
                            request.getURI().getPath(),
                            finalRequestId);
                }));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
