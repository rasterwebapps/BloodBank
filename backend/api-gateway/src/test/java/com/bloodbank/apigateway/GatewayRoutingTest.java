package com.bloodbank.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/bloodbank",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/bloodbank/protocol/openid-connect/certs"
    }
)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayRoutingTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void actuatorHealthEndpointIsAccessible() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void unauthenticatedRequestsToApiAreRejected() {
        webTestClient.get()
                .uri("/api/v1/donors")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void unauthenticatedRequestToBranchServiceIsRejected() {
        webTestClient.get()
                .uri("/api/v1/branches")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void unauthenticatedRequestToLabServiceIsRejected() {
        webTestClient.get()
                .uri("/api/v1/test-orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void unauthenticatedRequestToInventoryServiceIsRejected() {
        webTestClient.get()
                .uri("/api/v1/blood-units")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void actuatorInfoEndpointIsAccessible() {
        webTestClient.get()
                .uri("/actuator/info")
                .exchange()
                .expectStatus().isOk();
    }
}
