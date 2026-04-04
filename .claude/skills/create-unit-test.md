# Skill: Create Unit Test

Generate JUnit 5 + Mockito unit tests following BloodBank patterns.

## Rules

1. Use JUnit 5 (`@ExtendWith(MockitoExtension.class)`)
2. Use `@Mock` for dependencies, `@InjectMocks` for the class under test
3. No Lombok — use explicit setup
4. Follow Arrange-Act-Assert (AAA) pattern
5. Name format: `should{ExpectedBehavior}_when{Condition}`
6. Target >80% line coverage per class
7. Package mirrors main source: `com.bloodbank.{servicename}.service`

## Service Test Template

```java
package com.bloodbank.{servicename}.service;

import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.{servicename}.dto.*;
import com.bloodbank.{servicename}.entity.{Entity};
import com.bloodbank.{servicename}.mapper.{Entity}Mapper;
import com.bloodbank.{servicename}.repository.{Entity}Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("{Entity}Service Unit Tests")
class {Entity}ServiceTest {

    @Mock
    private {Entity}Repository {entity}Repository;

    @Mock
    private {Entity}Mapper {entity}Mapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private {Entity}Service {entity}Service;

    private {Entity} sample{Entity};
    private {Entity}CreateRequest createRequest;
    private {Entity}Response expectedResponse;

    @BeforeEach
    void setUp() {
        sample{Entity} = new {Entity}("field1", "field2");
        createRequest = new {Entity}CreateRequest("field1", "field2", UUID.randomUUID());
        expectedResponse = new {Entity}Response(UUID.randomUUID(), "field1", "field2", null, null);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create {entity} successfully")
        void shouldCreate{Entity}_whenValidRequest() {
            // Arrange
            when({entity}Mapper.toEntity(createRequest)).thenReturn(sample{Entity});
            when({entity}Repository.save(any({Entity}.class))).thenReturn(sample{Entity});
            when({entity}Mapper.toResponse(sample{Entity})).thenReturn(expectedResponse);

            // Act
            {Entity}Response result = {entity}Service.create(createRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResponse);
            verify({entity}Repository).save(any({Entity}.class));
            verify({entity}Mapper).toEntity(createRequest);
            verify({entity}Mapper).toResponse(sample{Entity});
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return {entity} when found")
        void shouldReturn{Entity}_whenExists() {
            UUID id = UUID.randomUUID();
            when({entity}Repository.findById(id)).thenReturn(Optional.of(sample{Entity}));
            when({entity}Mapper.toResponse(sample{Entity})).thenReturn(expectedResponse);

            {Entity}Response result = {entity}Service.getById(id);

            assertThat(result).isEqualTo(expectedResponse);
            verify({entity}Repository).findById(id);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowException_whenNotFound() {
            UUID id = UUID.randomUUID();
            when({entity}Repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> {entity}Service.getById(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("should update {entity} successfully")
        void shouldUpdate{Entity}_whenExists() {
            UUID id = UUID.randomUUID();
            {Entity}UpdateRequest updateRequest = new {Entity}UpdateRequest("updated", "updated");
            when({entity}Repository.findById(id)).thenReturn(Optional.of(sample{Entity}));
            when({entity}Repository.save(any({Entity}.class))).thenReturn(sample{Entity});
            when({entity}Mapper.toResponse(sample{Entity})).thenReturn(expectedResponse);

            {Entity}Response result = {entity}Service.update(id, updateRequest);

            assertThat(result).isNotNull();
            verify({entity}Mapper).updateEntity(eq(updateRequest), any({Entity}.class));
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete {entity} when exists")
        void shouldDelete{Entity}_whenExists() {
            UUID id = UUID.randomUUID();
            when({entity}Repository.existsById(id)).thenReturn(true);

            {entity}Service.delete(id);

            verify({entity}Repository).deleteById(id);
        }

        @Test
        @DisplayName("should throw exception when {entity} not found for delete")
        void shouldThrowException_whenDeleteNonExistent() {
            UUID id = UUID.randomUUID();
            when({entity}Repository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> {entity}Service.delete(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
```

## Controller Test Template

```java
@WebMvcTest({Entity}Controller.class)
@Import(SecurityConfig.class)
class {Entity}ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private {Entity}Service {entity}Service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "BRANCH_ADMIN")
    void shouldCreateEntity_whenAuthorized() throws Exception {
        // test implementation
    }

    @Test
    @WithMockUser(roles = "DONOR")
    void shouldReturn403_whenUnauthorized() throws Exception {
        // test 403 Forbidden
    }
}
```

## Validation

- [ ] JUnit 5 + Mockito (no JUnit 4 annotations)
- [ ] `@ExtendWith(MockitoExtension.class)` — not `@RunWith`
- [ ] AssertJ assertions — not JUnit `assertEquals`
- [ ] Nested test classes with `@Nested` and `@DisplayName`
- [ ] Both happy path and error cases covered
- [ ] No Lombok annotations
- [ ] Tests for all CRUD operations
- [ ] Controller tests verify security (`@PreAuthorize`)
