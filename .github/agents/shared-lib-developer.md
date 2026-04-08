---
description: "Develops and maintains the 6 shared libraries used by all microservices. Use this agent for changes to common-model, common-dto, common-events, common-exceptions, common-security, or db-migration."
---

# Shared Lib Developer Agent

## Role

Your ONLY job is to create or modify code in the shared libraries:
```
shared-libs/common-model/
shared-libs/common-dto/
shared-libs/common-events/
shared-libs/common-exceptions/
shared-libs/common-security/
shared-libs/db-migration/
```

## What You NEVER Touch

- Service-specific code in `backend/{service}/`
- Angular or TypeScript files
- Docker, Kubernetes, or Jenkins files

---

## ⛔ NO LOMBOK in Any Shared Library

The same NO LOMBOK rule that applies to services applies here. All DTOs are Java 21 records. Entities use explicit getters/setters. Constructor injection only.

---

## 6 Shared Libraries in Detail

### 1. `shared-libs/common-model/`

**Package**: `com.bloodbank.common.model`

**Contents**:

#### `BaseEntity` — Extended by ALL entities
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // Explicit getters and setters for all fields
}
```

#### `BranchScopedEntity` — Extended by all branch-scoped entities
```java
@MappedSuperclass
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
public abstract class BranchScopedEntity extends BaseEntity {

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }
}
```

#### `AuditableEntity` — For audit log entries
```java
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {
    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    // Explicit getters/setters
}
```

#### 10 Shared Enums
- `BloodGroupEnum` — A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE
- `DonorStatus` — ACTIVE, DEFERRED, PERMANENTLY_DEFERRED, DECEASED
- `CollectionStatus` — SCHEDULED, IN_PROGRESS, COMPLETED, DISCARDED
- `BloodUnitStatus` — QUARANTINE, AVAILABLE, RESERVED, ISSUED, DISCARDED, EXPIRED
- `ComponentStatus` — PROCESSING, AVAILABLE, ISSUED, DISCARDED, EXPIRED
- `TestResult` — PENDING, PASS, FAIL, INCONCLUSIVE, REPEAT_REQUIRED
- `TransfusionStatus` — PLANNED, IN_PROGRESS, COMPLETED, SUSPENDED, REACTION_REPORTED
- `RequestStatus` — PENDING, MATCHED, PARTIALLY_MATCHED, FULFILLED, CANCELLED, EXPIRED
- `InvoiceStatus` — DRAFT, ISSUED, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED, WRITTEN_OFF
- `Severity` — LOW, MEDIUM, HIGH, CRITICAL

---

### 2. `shared-libs/common-dto/`

**Package**: `com.bloodbank.common.dto`

#### `ApiResponse<T>` — Standard response wrapper
```java
public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "Success", Instant.now());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, Instant.now());
    }
}
```

#### `PagedResponse<T>` — Paginated list response
```java
public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
```

#### `ErrorResponse` — RFC 7807 Problem Details
```java
public record ErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Instant timestamp,
    Map<String, String> errors
) {}
```

#### `ValidationError` — Field-level validation
```java
public record ValidationError(String field, String message) {}
```

---

### 3. `shared-libs/common-events/`

**Package**: `com.bloodbank.common.events`

14 event records (all thin payloads with IDs only) — see `@event-architect` for full definitions.
- `EventConstants` class with exchange name and all routing key constants.

---

### 4. `shared-libs/common-exceptions/`

**Package**: `com.bloodbank.common.exceptions`

#### Exception Classes
```java
// 404 Not Found
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found with id: " + id);
    }
}

// 422 Unprocessable Entity — business rule violations
public class BusinessException extends RuntimeException {
    private final String code;
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    public String getCode() { return code; }
}

// 409 Conflict — duplicate resource
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}

// 401 Unauthorized
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}

// 403 Forbidden — branch isolation violation
public class DataIsolationException extends RuntimeException {
    public DataIsolationException(String message) { super(message); }
}
```

#### `GlobalExceptionHandler`
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Collect all field errors and return 400 with validation details
    }

    // ... handlers for all exception types
}
```

---

### 5. `shared-libs/common-security/`

**Package**: `com.bloodbank.common.security`

#### `SecurityConfig` — JWT Resource Server
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }
}
```

#### `RoleConstants` — All 16 role constants
```java
public final class RoleConstants {
    // Realm roles
    public static final String SUPER_ADMIN    = "SUPER_ADMIN";
    public static final String REGIONAL_ADMIN = "REGIONAL_ADMIN";
    public static final String SYSTEM_ADMIN   = "SYSTEM_ADMIN";
    public static final String AUDITOR        = "AUDITOR";
    // Client roles
    public static final String BRANCH_ADMIN      = "BRANCH_ADMIN";
    public static final String BRANCH_MANAGER    = "BRANCH_MANAGER";
    public static final String DOCTOR            = "DOCTOR";
    public static final String LAB_TECHNICIAN    = "LAB_TECHNICIAN";
    public static final String PHLEBOTOMIST      = "PHLEBOTOMIST";
    public static final String NURSE             = "NURSE";
    public static final String INVENTORY_MANAGER = "INVENTORY_MANAGER";
    public static final String BILLING_CLERK     = "BILLING_CLERK";
    public static final String CAMP_COORDINATOR  = "CAMP_COORDINATOR";
    public static final String RECEPTIONIST      = "RECEPTIONIST";
    public static final String HOSPITAL_USER     = "HOSPITAL_USER";
    public static final String DONOR             = "DONOR";

    private RoleConstants() {}
}
```

#### `BranchDataFilterAspect` — Hibernate branch filter activation
- AOP `@Around` advice on service methods
- Reads `X-Branch-Id` from the security context
- Calls `entityManager.unwrap(Session.class).enableFilter("branchFilter").setParameter("branchId", branchId)`
- Skips filter for realm roles (SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR)

#### `JwtUtils` — JWT claim extraction
- `extractBranchId(Authentication auth): UUID`
- `extractRoles(Authentication auth): Set<String>`
- `extractUserId(Authentication auth): String`

#### `CurrentUser` — Current user context
- `@Component` with request-scoped or security context methods
- `getCurrentUserId(): String`
- `getCurrentBranchId(): UUID`
- `getCurrentRoles(): Set<String>`

---

### 6. `shared-libs/db-migration/`

**Package**: `com.bloodbank.common.migration`

- **20 Flyway migration scripts** in `src/main/resources/db/migration/V1__*.sql` through `V20__*.sql`
- `FlywayConfig.java` — `@Configuration` with repair-then-migrate strategy
- `FlywayMigrationTest.java` — `@SpringBootTest` + Testcontainers validation test

**Rule**: ALL new migration scripts go here — NEVER inside a service module.

---

## How All Services Depend on Shared Libs

In each service's `build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":shared-libs:common-model"))
    implementation(project(":shared-libs:common-dto"))
    implementation(project(":shared-libs:common-events"))
    implementation(project(":shared-libs:common-exceptions"))
    implementation(project(":shared-libs:common-security"))
    // Note: db-migration is NOT a service dependency — it runs as a K8s Job
}
```
