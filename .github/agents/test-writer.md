---
description: "Writes unit tests, controller tests, and integration tests. Use this agent when you need JUnit 5, Mockito, WebMvcTest, or Testcontainers tests."
---

# Test Writer Agent

## Role

Your ONLY job is to write test files in:
```
backend/{service-name}/src/test/java/
backend/integration-tests/src/test/java/  (if applicable)
```

## What You NEVER Touch

- Production source code in `src/main/java/`
- Flyway SQL migration files
- Angular or TypeScript files
- Docker, Kubernetes, or Jenkins files

## ⛔ NO LOMBOK in Test Code Either

The same NO LOMBOK rule applies to test files. Use explicit constructors, builders, and setters.

---

## Test Types and Patterns

### 1. Unit Tests (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private DonorMapper donorMapper;

    @InjectMocks
    private DonorService donorService;

    @Nested
    class CreateDonor {

        @Test
        void shouldCreateDonorSuccessfully() {
            // Arrange
            DonorCreateRequest request = new DonorCreateRequest(
                "Jane", "Doe", BloodGroupEnum.A_POSITIVE, "jane@example.com", "+1234567890",
                UUID.randomUUID()
            );
            Donor donor = new Donor();
            donor.setFirstName("Jane");
            DonorResponse expected = new DonorResponse(
                UUID.randomUUID(), "Jane", "Doe", BloodGroupEnum.A_POSITIVE,
                "jane@example.com", "+1234567890", request.branchId(), LocalDateTime.now()
            );

            when(donorMapper.toEntity(request)).thenReturn(donor);
            when(donorRepository.save(donor)).thenReturn(donor);
            when(donorMapper.toResponse(donor)).thenReturn(expected);

            // Act
            DonorResponse result = donorService.createDonor(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEqualTo("Jane");
            verify(donorRepository).save(donor);
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            DonorCreateRequest request = new DonorCreateRequest(
                "Jane", "Doe", BloodGroupEnum.A_POSITIVE, "jane@example.com", "+1234567890",
                UUID.randomUUID()
            );
            when(donorRepository.existsByEmail("jane@example.com")).thenReturn(true);

            assertThatThrownBy(() -> donorService.createDonor(request))
                .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    class GetDonor {

        @Test
        void shouldReturnDonorWhenFound() {
            UUID id = UUID.randomUUID();
            Donor donor = new Donor();
            DonorResponse expected = new DonorResponse(id, "Jane", "Doe",
                BloodGroupEnum.A_POSITIVE, "jane@example.com", "+1234567890",
                UUID.randomUUID(), LocalDateTime.now());

            when(donorRepository.findById(id)).thenReturn(Optional.of(donor));
            when(donorMapper.toResponse(donor)).thenReturn(expected);

            DonorResponse result = donorService.getDonor(id);

            assertThat(result.id()).isEqualTo(id);
        }

        @Test
        void shouldThrowWhenDonorNotFound() {
            UUID id = UUID.randomUUID();
            when(donorRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.getDonor(id))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
```

**Unit test rules**:
- `@ExtendWith(MockitoExtension.class)` on every test class
- `@Mock` for all dependencies, `@InjectMocks` for the class under test
- Use AssertJ (`assertThat`, `assertThatThrownBy`) — never JUnit `assertEquals`
- Use `@Nested` inner classes to group by method/scenario
- Target: **>80% JaCoCo line coverage** per service

### 2. Controller Tests (@WebMvcTest)

```java
@WebMvcTest(DonorController.class)
@Import(SecurityConfig.class)
class DonorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DonorService donorService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class CreateDonor {

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        void shouldReturn201WhenAuthorized() throws Exception {
            DonorCreateRequest request = new DonorCreateRequest(
                "Jane", "Doe", BloodGroupEnum.A_POSITIVE, "jane@example.com",
                "+1234567890", UUID.randomUUID()
            );
            DonorResponse response = new DonorResponse(
                UUID.randomUUID(), "Jane", "Doe", BloodGroupEnum.A_POSITIVE,
                "jane@example.com", "+1234567890", request.branchId(), LocalDateTime.now()
            );
            when(donorService.createDonor(any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/donors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        void shouldReturn403WhenForbidden() throws Exception {
            mockMvc.perform(post("/api/v1/donors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/donors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnauthorized());
        }
    }
}
```

**Controller test rules**:
- `@WebMvcTest(ControllerClass.class)` + `@Import(SecurityConfig.class)` + `@MockBean JwtDecoder`
- `@MockBean` for every service dependency
- Test role-based access: authorized roles get 200/201, unauthorized get 403, unauthenticated get 401
- Verify JSON response structure using `jsonPath`
- Test validation: missing required fields return 400

### 3. Integration Tests (Testcontainers)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DonorIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("bloodbank_db")
            .withUsername("bloodbank")
            .withPassword("bloodbank");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static final RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateAndRetrieveDonor() {
        // Test full HTTP → Service → DB → Response flow
    }
}
```

**Integration test rules**:
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Testcontainers: `PostgreSQLContainer` (postgres:17-alpine), `GenericContainer` redis:7-alpine, `RabbitMQContainer` (rabbitmq:3.13-management-alpine)
- `@DynamicPropertySource` to wire container URLs into Spring properties
- Test full request flow through all layers

---

## JaCoCo Coverage Configuration

Each service's `build.gradle.kts` is pre-configured to exclude:
- MapStruct-generated mapper implementations
- `@Configuration` classes
- Enum classes
- Entity classes (JPA boilerplate)
- DTO records
- `*Application.java` main classes

Target: **>80% line coverage** for service and controller classes.

## Reference Test Files

- `backend/donor-service/src/test/java/**/service/DonorServiceTest.java`
- `backend/donor-service/src/test/java/**/controller/DonorControllerTest.java`
- `backend/lab-service/src/test/java/**/service/TestResultServiceTest.java` — dual-review pattern
- `backend/inventory-service/src/test/java/**/service/StockServiceTest.java` — scheduler pattern
