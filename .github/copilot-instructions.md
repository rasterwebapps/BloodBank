# GitHub Copilot Instructions — BloodBank Project

## Project Overview

BloodBank is a **24-module, 14-microservice** blood bank management system. It manages the complete blood lifecycle from **Donor Registration → Collection → Testing → Processing → Inventory → Cross-Matching → Issuing → Transfusion → Hemovigilance**.

## ⛔ CRITICAL RULES

### NO LOMBOK — ANYWHERE
- **NEVER** use any Lombok annotation: `@Data`, `@Getter`, `@Setter`, `@Builder`, `@Value`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`, `@Slf4j`, `@ToString`, `@EqualsAndHashCode`
- **NEVER** add `lombok` to any `build.gradle.kts`
- **NEVER** import from `lombok.*`

### What to Use Instead
| Instead of | Use |
|---|---|
| `@Data` on DTOs | Java 21 `record` |
| `@Getter/@Setter` | Explicit `getXxx()` / `setXxx()` |
| `@NoArgsConstructor` | `protected ClassName() {}` |
| `@AllArgsConstructor` | Explicit constructor |
| `@RequiredArgsConstructor` | Explicit constructor with `final` fields |
| `@Builder` | Static factory method or constructor |
| `@Slf4j` | `private static final Logger log = LoggerFactory.getLogger(ClassName.class);` |

## Tech Stack

- **Java 21** (records, virtual threads, sealed classes, pattern matching)
- **Spring Boot 3.4.x** + Spring Cloud 2024.x
- **Gradle 8** (Kotlin DSL)
- **Angular 21** (standalone components, signals, zoneless)
- **PostgreSQL 17** (single shared database `bloodbank_db`)
- **Flyway** (centralized in `shared-libs/db-migration/`)
- **Redis 7** (caching)
- **RabbitMQ 3.13+** (async events — thin payloads, IDs only)
- **Keycloak 26+** (OAuth2/OIDC, 16 roles)
- **MapStruct 1.6+** (compile-time mapping)
- **Docker + Kubernetes 1.30+**

## Code Patterns

### DTOs — Always Java 21 Records
```java
public record DonorCreateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull BloodGroupEnum bloodGroup,
    @Email String email,
    @NotNull UUID branchId
) {}
```

### Entities — Explicit Getters/Setters, Extend Base Classes
```java
@Entity
@Table(name = "donors")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Donor extends BranchScopedEntity {
    @Column(name = "first_name", nullable = false)
    private String firstName;

    protected Donor() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
}
```

### Services — Constructor Injection
```java
@Service
@Transactional(readOnly = true)
public class DonorService {
    private static final Logger log = LoggerFactory.getLogger(DonorService.class);
    private final DonorRepository donorRepository;
    private final DonorMapper donorMapper;

    public DonorService(DonorRepository donorRepository, DonorMapper donorMapper) {
        this.donorRepository = donorRepository;
        this.donorMapper = donorMapper;
    }
}
```

### Controllers — @PreAuthorize on Every Method
```java
@RestController
@RequestMapping("/api/v1/donors")
public class DonorController {
    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DonorResponse>> create(@Valid @RequestBody DonorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(donorService.create(request)));
    }
}
```

### Events — Thin Payloads (IDs Only)
```java
public record DonationCompletedEvent(UUID donationId, UUID donorId, UUID branchId, Instant occurredAt) {}
```

## Architecture Rules

1. **Single Shared Database** — All services connect to ONE PostgreSQL 17 database
2. **RabbitMQ for Async Only** — Not for data sync, events carry IDs only
3. **4-Layer Branch Isolation** — Gateway → Security → JPA Filter → DB indexes
4. **API Prefix**: `/api/v1/`
5. **Response Wrapper**: `ApiResponse<T>` for all endpoints
6. **Flyway Disabled in Services**: `spring.flyway.enabled=false`
7. **Migrations in**: `shared-libs/db-migration/`

## 16 User Roles

**Realm**: SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR
**Client**: BRANCH_ADMIN, BRANCH_MANAGER, DOCTOR, LAB_TECHNICIAN, PHLEBOTOMIST, NURSE, INVENTORY_MANAGER, BILLING_CLERK, CAMP_COORDINATOR, RECEPTIONIST, HOSPITAL_USER, DONOR

## Testing Standards

- **Unit Tests**: JUnit 5 + Mockito, >80% coverage
- **Integration Tests**: Testcontainers (PostgreSQL 17, Redis 7, RabbitMQ 3.13)
- **Controller Tests**: `@WebMvcTest` with `@WithMockUser` per role

## Commit Convention

Use conventional commits: `feat:`, `fix:`, `docs:`, `chore:`, `test:`, `refactor:`
