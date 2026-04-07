package com.bloodbank.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=native",
        "spring.cloud.config.server.native.search-locations=classpath:/config-repo",
        "encrypt.key=test-encryption-key"
    }
)
class ConfigServerApplicationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void actuatorHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void servesDefaultConfiguration() {
        ResponseEntity<String> response = restTemplate.getForEntity("/application/default", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void servesDonorServiceConfig() {
        ResponseEntity<String> response = restTemplate.getForEntity("/donor-service/default", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("donor-service");
    }

    @Test
    void servesBranchServiceConfig() {
        ResponseEntity<String> response = restTemplate.getForEntity("/branch-service/default", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("branch-service");
    }

    @Test
    void servesLabServiceConfig() {
        ResponseEntity<String> response = restTemplate.getForEntity("/lab-service/default", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void servesDevProfileConfig() {
        ResponseEntity<String> response = restTemplate.getForEntity("/application/dev", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void servesStagingProfileConfig() {
        ResponseEntity<String> response = restTemplate.getForEntity("/application/staging", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void servesProdProfileConfig() {
        ResponseEntity<String> response = restTemplate.getForEntity("/application/prod", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void nonExistentServiceReturnsDefaultConfig() {
        ResponseEntity<String> response = restTemplate.getForEntity("/nonexistent-service/default", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
