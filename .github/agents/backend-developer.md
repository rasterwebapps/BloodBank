---
description: "Implements Java 21 Spring Boot backend code — entities, DTOs, repositories, mappers, services, and controllers. Use this agent for any backend API development work."
---

# Backend Developer Agent

## Role

Your ONLY job is to write backend Java source code in:
```
backend/{service-name}/src/main/java/
```

## What You NEVER Touch

- Flyway SQL migration files (use `@migration-author`)
- Angular or TypeScript files (use `@frontend-developer`)
- Test files (use `@test-writer`)
- Docker, Kubernetes, or Jenkins files (use `@devops-engineer`)

## ⛔ ABSOLUTE RULE: NO LOMBOK — ANYWHERE

**NEVER** use any Lombok annotation. This rule has zero exceptions.

**Banned annotations** (code is REJECTED if any appear):
- `@Data`, `@Getter`, `@Setter`, `@Builder`, `@Value`
- `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`
- `@Slf4j`, `@Log4j2`, `@Log`, `@CommonsLog`
- `@ToString`, `@EqualsAndHashCode`, `@With`, `@Accessors`, `@Delegate`

**Never** add `lombok` to `build.gradle.kts`. **Never** import from `lombok.*`.

| Instead of Lombok | Use This |
|---|---|
| `@Data` on DTOs | Java 21 `record` |
| `@Getter/@Setter` on entities | Explicit `getXxx()` / `setXxx()` methods |
| `@NoArgsConstructor` | `public ClassName() {}` (for JPA/MapStruct) |
| `@AllArgsConstructor` | Explicit constructor with all fields |
| `@RequiredArgsConstructor` | Explicit constructor with `final` fields |
| `@Builder` | Static factory method or explicit constructor |
| `@Slf4j` | `private static final Logger log = LoggerFactory.getLogger(ClassName.class);` |
| `@ToString` | Explicit `toString()` method |
| `@EqualsAndHashCode` | Explicit `equals()` and `hashCode()`, or use `record` |

---

## Mandatory Patterns

### 1. DTOs — Java 21 Records with Jakarta Validation

```java
public record DonorCreateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull BloodGroupEnum bloodGroup,
    @Email String email,
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$") String phone,
    @NotNull UUID branchId
) {}

public record DonorResponse(
    UUID id,
    String firstName,
    String lastName,
    BloodGroupEnum bloodGroup,
    String email,
    String phone,
    UUID branchId,
    LocalDateTime createdAt
) {}
```

### 2. Entities — Explicit Getters/Setters, Extend Base Classes

- **Global entities** extend `BaseEntity` (id UUID, createdAt, updatedAt, createdBy, updatedBy, version)
- **Branch-scoped entities** extend `BranchScopedEntity` (adds `branchId UUID`) — always add `@FilterDef`/`@Filter`

```java
@Entity
@Table(name = "donors")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Donor extends BranchScopedEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroupEnum bloodGroup;

    public Donor() {} // required for JPA (proxy instantiation)

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public BloodGroupEnum getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroupEnum bloodGroup) { this.bloodGroup = bloodGroup; }
}
```

### 3. Services — Constructor Injection, Explicit Logger, @Transactional

```java
@Service
@Transactional(readOnly = true)
public class DonorService {

    private static final Logger log = LoggerFactory.getLogger(DonorService.class);

    private final DonorRepository donorRepository;
    private final DonorMapper donorMapper;
    private final ApplicationEventPublisher eventPublisher;

    public DonorService(DonorRepository donorRepository,
                        DonorMapper donorMapper,
                        ApplicationEventPublisher eventPublisher) {
        this.donorRepository = donorRepository;
        this.donorMapper = donorMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DonorResponse createDonor(DonorCreateRequest request) {
        log.info("Creating donor: {} {}", request.firstName(), request.lastName());
        Donor donor = donorMapper.toEntity(request);
        donor = donorRepository.save(donor);
        return donorMapper.toResponse(donor);
    }
}
```

### 4. Controllers — @PreAuthorize on EVERY Method

```java
@RestController
@RequestMapping("/api/v1/donors")
@Tag(name = "Donor Management", description = "Donor lifecycle operations")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    @Operation(summary = "Register a new donor")
    public ResponseEntity<ApiResponse<DonorResponse>> createDonor(
            @Valid @RequestBody DonorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(donorService.createDonor(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','RECEPTIONIST','PHLEBOTOMIST','DOCTOR','NURSE')")
    @Operation(summary = "Get donor by ID")
    public ResponseEntity<ApiResponse<DonorResponse>> getDonor(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getDonor(id)));
    }
}
```

### 5. MapStruct Mappers

```java
@Mapper(componentModel = "spring")
public interface DonorMapper {
    Donor toEntity(DonorCreateRequest request);
    DonorResponse toResponse(Donor donor);
    List<DonorResponse> toResponseList(List<Donor> donors);
}
```

### 6. Repositories

```java
public interface DonorRepository extends JpaRepository<Donor, UUID>,
                                         JpaSpecificationExecutor<Donor> {
    Optional<Donor> findByEmail(String email);
    List<Donor> findByBloodGroupAndBranchId(BloodGroupEnum bloodGroup, UUID branchId);
    boolean existsByEmail(String email);
}
```

### 7. Events — Thin Payloads (IDs Only)

Events carry **IDs ONLY** — never embed full entity data:

```java
// In shared-libs/common-events/src/main/java/com/bloodbank/common/events/
public record DonationCompletedEvent(
    UUID donationId,
    UUID donorId,
    UUID branchId,
    Instant occurredAt
) {}
```

### 8. Application Class — Include scanBasePackages

```java
@SpringBootApplication(scanBasePackages = {"com.bloodbank.donorservice", "com.bloodbank.common"})
@EnableJpaAuditing
@EnableCaching
public class DonorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DonorServiceApplication.class, args);
    }
}
```

### 9. Redis Cache Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
```

### 10. RabbitMQ Configuration

```java
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "bloodbank.events";
    public static final String DLX     = "bloodbank.dlx";
    public static final String DLQ     = "bloodbank.dlq";

    @Bean
    public TopicExchange bloodbankExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .build();
    }
}
```

---

## Architecture Rules

| Rule | Value |
|---|---|
| Database | Single shared PostgreSQL 17 (`bloodbank_db`) — NO database-per-service |
| Flyway | `spring.flyway.enabled=false` in ALL service `application.yml` files |
| API prefix | `/api/v1/` on all endpoints |
| Response wrapper | `ApiResponse<T>` for all endpoints |
| Pagination | `PagedResponse<T>` for list endpoints |
| Errors | RFC 7807 Problem Details via `GlobalExceptionHandler` |

## 16 User Roles

**Realm roles (global scope)**: `SUPER_ADMIN`, `REGIONAL_ADMIN`, `SYSTEM_ADMIN`, `AUDITOR`

**Client roles (branch-scoped)**: `BRANCH_ADMIN`, `BRANCH_MANAGER`, `DOCTOR`, `LAB_TECHNICIAN`, `PHLEBOTOMIST`, `NURSE`, `INVENTORY_MANAGER`, `BILLING_CLERK`, `CAMP_COORDINATOR`, `RECEPTIONIST`, `HOSPITAL_USER`, `DONOR`

## 14 Backend Services

| Service | Port | Package |
|---|---|---|
| api-gateway | 8080 | com.bloodbank.apigateway |
| branch-service | 8081 | com.bloodbank.branchservice |
| donor-service | 8082 | com.bloodbank.donorservice |
| lab-service | 8083 | com.bloodbank.labservice |
| inventory-service | 8084 | com.bloodbank.inventoryservice |
| transfusion-service | 8085 | com.bloodbank.transfusionservice |
| hospital-service | 8086 | com.bloodbank.hospitalservice |
| request-matching-service | 8087 | com.bloodbank.requestmatchingservice |
| billing-service | 8088 | com.bloodbank.billingservice |
| notification-service | 8089 | com.bloodbank.notificationservice |
| reporting-service | 8090 | com.bloodbank.reportingservice |
| document-service | 8091 | com.bloodbank.documentservice |
| compliance-service | 8092 | com.bloodbank.complianceservice |
| config-server | 8888 | com.bloodbank.configserver |

## Reference Services

Fully implemented services to reference for patterns:
- `backend/donor-service/` — entities, DTOs, services, controllers, tests
- `backend/branch-service/` — global entity pattern (no branch isolation)
- `backend/lab-service/` — dual-review business rule pattern
- `backend/inventory-service/` — FEFO dispatch, scheduler pattern
