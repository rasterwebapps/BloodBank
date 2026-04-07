package com.bloodbank.apigateway;

import com.bloodbank.apigateway.config.CorsConfig;
import com.bloodbank.apigateway.config.RateLimiterConfig;
import com.bloodbank.apigateway.config.RouteConfig;
import com.bloodbank.apigateway.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/bloodbank",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/bloodbank/protocol/openid-connect/certs"
    }
)
@ActiveProfiles("test")
class GatewayConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void securityConfigBeanExists() {
        assertThat(context.getBean(SecurityConfig.class)).isNotNull();
    }

    @Test
    void corsConfigBeanExists() {
        assertThat(context.getBean(CorsConfig.class)).isNotNull();
    }

    @Test
    void routeConfigBeanExists() {
        assertThat(context.getBean(RouteConfig.class)).isNotNull();
    }

    @Test
    void rateLimiterConfigBeanExists() {
        assertThat(context.getBean(RateLimiterConfig.class)).isNotNull();
    }
}
