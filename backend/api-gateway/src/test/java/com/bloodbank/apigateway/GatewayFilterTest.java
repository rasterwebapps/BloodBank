package com.bloodbank.apigateway;

import com.bloodbank.apigateway.filter.BranchIdExtractionFilter;
import com.bloodbank.apigateway.filter.RequestResponseLoggingFilter;
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
class GatewayFilterTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void branchIdExtractionFilterIsRegistered() {
        BranchIdExtractionFilter filter = applicationContext.getBean(BranchIdExtractionFilter.class);
        assertThat(filter).isNotNull();
    }

    @Test
    void requestResponseLoggingFilterIsRegistered() {
        RequestResponseLoggingFilter filter = applicationContext.getBean(RequestResponseLoggingFilter.class);
        assertThat(filter).isNotNull();
    }

    @Test
    void branchIdExtractionFilterOrder() {
        BranchIdExtractionFilter filter = applicationContext.getBean(BranchIdExtractionFilter.class);
        assertThat(filter.getOrder()).isEqualTo(-1);
    }

    @Test
    void requestResponseLoggingFilterOrder() {
        RequestResponseLoggingFilter filter = applicationContext.getBean(RequestResponseLoggingFilter.class);
        assertThat(filter.getOrder()).isEqualTo(-2);
    }
}
