# Skill: Create Integration Test

Generate Spring Boot integration tests using Testcontainers following BloodBank patterns.

## Rules

1. `@SpringBootTest` with full application context
2. Testcontainers for PostgreSQL, Redis, RabbitMQ
3. `@Testcontainers` + `@Container` annotations
4. Use `@DynamicPropertySource` for container connection properties
5. Shared container instances (static) for speed
6. Test full request/response cycle through REST API

## Template

```java
package com.bloodbank.{servicename};

import com.bloodbank.{servicename}.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("{Entity} Integration Tests")
class {Entity}IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("bloodbank_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
        registry.add("spring.flyway.enabled", () -> "true"); // Enable for integration tests
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static UUID createdId;

    @Test
    @Order(1)
    @WithMockUser(roles = "BRANCH_ADMIN")
    @DisplayName("POST /api/v1/{entities} — should create {entity}")
    void shouldCreate{Entity}() throws Exception {
        {Entity}CreateRequest request = new {Entity}CreateRequest(
                "Test", "Value", UUID.randomUUID());

        mockMvc.perform(post("/api/v1/{entities}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    // Extract created ID for subsequent tests
                });
    }

    @Test
    @Order(2)
    @WithMockUser(roles = "BRANCH_ADMIN")
    @DisplayName("GET /api/v1/{entities} — should list {entities}")
    void shouldList{Entities}() throws Exception {
        mockMvc.perform(get("/api/v1/{entities}")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(3)
    @WithMockUser(roles = "DONOR")
    @DisplayName("POST /api/v1/{entities} — should return 403 for unauthorized role")
    void shouldReturn403_whenUnauthorizedRole() throws Exception {
        {Entity}CreateRequest request = new {Entity}CreateRequest(
                "Test", "Value", UUID.randomUUID());

        mockMvc.perform(post("/api/v1/{entities}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @WithMockUser(roles = "BRANCH_ADMIN")
    @DisplayName("POST /api/v1/{entities} — should return 400 for invalid request")
    void shouldReturn400_whenInvalidRequest() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/api/v1/{entities}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
```

## Repository Integration Test

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class {Entity}RepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("bloodbank_test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private {Entity}Repository repository;

    @Test
    void shouldSaveAndFindById() {
        // test implementation
    }
}
```

## Validation

- [ ] Uses Testcontainers (PostgreSQL 17, Redis 7, RabbitMQ 3.13)
- [ ] `@DynamicPropertySource` for container config
- [ ] Static containers (shared across tests)
- [ ] Tests full HTTP cycle (request → response)
- [ ] Tests security (authorized + unauthorized roles)
- [ ] Tests validation (400 Bad Request)
- [ ] No Lombok annotations
