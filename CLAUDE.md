# CLAUDE.md — BloodBank Project Instructions

## Project Overview

BloodBank is a worldwide, multi-branch, containerized microservices blood bank management system. It manages the complete blood lifecycle from Donor Registration through Transfusion and Hemovigilance. The system is built with 24 functional modules deployed across 14 microservices, with a single shared PostgreSQL 17 database (~87 tables), 16 user roles managed by Keycloak, and a 41-agent AI orchestration system for development.

## Tech Stack (Mandatory Versions)

- **Java 21** (LTS) — use virtual threads, records, sealed classes, pattern matching
- **Spring Boot 3.4.x** + Spring Cloud 2024.x
- **Gradle 8** (Kotlin DSL) — multi-module build
- **Angular 21** — standalone components, signals, zoneless change detection
- **PostgreSQL 17** — single shared database `bloodbank_db`
- **Flyway** — centralized migrations in `shared-libs/db-migration/`
- **Redis 7** — caching (branch data, stock levels, master data)
- **RabbitMQ 3.13+** — async events ONLY (thin payloads, IDs only)
- **Keycloak 26+** — OAuth2/OIDC, LDAP federation, MFA
- **Docker** — multi-stage builds (Gradle build → Temurin JRE 21 Alpine)
- **Kubernetes 1.30+** — deployments, HPA, StatefulSets, Jobs
- **Jenkins** — 11-stage declarative pipeline
- **MapStruct 1.6+** — compile-time object mapping
- **Resilience4j** — circuit breaker, retry, rate limiter
- **OpenTelemetry + Micrometer** — distributed tracing and metrics
- **MinIO** — S3-compatible object storage for documents

## ⛔ ABSOLUTE RULES — NEVER VIOLATE

### NO LOMBOK — ANYWHERE

This is the single most critical rule. **Never use any Lombok annotation in any file.**

Banned annotations (if you generate ANY of these, the code is REJECTED):
- `@Data`, `@Getter`, `@Setter`, `@Builder`, `@Value`
- `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`
- `@Slf4j`, `@Log4j2`, `@Log`, `@CommonsLog`
- `@ToString`, `@EqualsAndHashCode`
- `@With`, `@Wither`
- `@Accessors`, `@Delegate`
- Any annotation from `lombok.*` package

Never add `lombok` to any `build.gradle.kts` dependency block. Never import from `lombok.*`.

### What to Use Instead

| Instead of Lombok | Use This |
|---|---|
| `@Data` on DTOs | Java 21 `record` |
| `@Getter/@Setter` on entities | Explicit `getXxx()` / `setXxx()` methods |
| `@NoArgsConstructor` | `protected ClassName() {}` (for JPA) |
| `@AllArgsConstructor` | Explicit constructor with all fields |
| `@RequiredArgsConstructor` | Explicit constructor with `final` fields |
| `@Builder` | Static factory method or constructor |
| `@Slf4j` | `private static final Logger log = LoggerFactory.getLogger(ClassName.class);` |
| `@ToString` | Explicit `toString()` method |
| `@EqualsAndHashCode` | Explicit `equals()` and `hashCode()` or use `record` |

## Code Patterns

### DTOs — Always Java 21 Records

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

### Events — Always Java 21 Records (Thin Payloads)

```java
public record DonationCompletedEvent(
    UUID donationId,
    UUID donorId,
    UUID branchId,
    Instant occurredAt
) {}
```

Events carry IDs ONLY. Never embed full entity data in events.

### Entities — Explicit Getters/Setters, Extend Base Classes

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

    protected Donor() {} // JPA requires no-arg constructor

    public Donor(String firstName, String lastName, BloodGroupEnum bloodGroup) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.bloodGroup = bloodGroup;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public BloodGroupEnum getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroupEnum bloodGroup) { this.bloodGroup = bloodGroup; }
}
```

### Services — Constructor Injection, Explicit Logger

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

### Controllers — @PreAuthorize on Every Method

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
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','RECEPTIONIST','PHLEBOTOMIST')")
    @Operation(summary = "Register a new donor")
    public ResponseEntity<ApiResponse<DonorResponse>> createDonor(
            @Valid @RequestBody DonorCreateRequest request) {
        DonorResponse response = donorService.createDonor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Donor registered successfully"));
    }
}
```

### MapStruct Mappers

```java
@Mapper(componentModel = "spring")
public interface DonorMapper {
    Donor toEntity(DonorCreateRequest request);
    DonorResponse toResponse(Donor donor);
    List<DonorResponse> toResponseList(List<Donor> donors);
}
```

### Repositories

```java
public interface DonorRepository extends JpaRepository<Donor, UUID>,
                                         JpaSpecificationExecutor<Donor> {
    Optional<Donor> findByEmail(String email);
    List<Donor> findByBloodGroupAndBranchId(BloodGroupEnum bloodGroup, UUID branchId);
    boolean existsByEmail(String email);
}
```

## Architecture Rules

### Single Shared Database

All 14 services connect to ONE PostgreSQL 17 database (`bloodbank_db`). There is NO database-per-service pattern.
- Flyway migrations centralized in `shared-libs/db-migration/`
- Kubernetes Job runs Flyway BEFORE services start
- Each service sets `spring.flyway.enabled=false` in its application.yml
- Services access ONLY their own domain tables (enforced by convention)

### RabbitMQ — Async Actions Only

RabbitMQ is NOT for data synchronization. All services query the shared DB directly.
- Topic Exchange: `bloodbank.events`
- Dead Letter: `bloodbank.dlx` → `bloodbank.dlq`
- Retry: 3 attempts, 1s backoff, 2x multiplier
- Payloads: **Thin events — IDs only, no entity data**

Use cases: notifications, audit logging, workflow triggers, emergency broadcasts.

### Branch Data Isolation (4 Layers)

1. **API Gateway** — `BranchIdExtractionFilter` extracts `branch_id` from JWT
2. **Spring Security** — `@PreAuthorize` on every controller method
3. **JPA Filtering** — `BranchDataFilterAspect` (AOP) enables Hibernate `@Filter`
4. **Database** — `branch_id` column + composite indexes on all branch-scoped tables

### Base Entity Classes

All entities extend `BaseEntity` (global) or `BranchScopedEntity` (branch-scoped):
- `BaseEntity`: id (UUID), createdAt, updatedAt, createdBy, updatedBy, version
- `BranchScopedEntity extends BaseEntity`: branchId (UUID)

### API Conventions

- Prefix: `/api/v1/`
- Response wrapper: `ApiResponse<T>` with success, data, message, timestamp
- Pagination: `PagedResponse<T>` with content, page, size, totalElements, totalPages
- Errors: RFC 7807 Problem Details via `GlobalExceptionHandler`
- Content-Type: `application/json`

### 16 User Roles

Realm roles: SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR
Client roles: BRANCH_ADMIN, BRANCH_MANAGER, DOCTOR, LAB_TECHNICIAN, PHLEBOTOMIST, NURSE, INVENTORY_MANAGER, BILLING_CLERK, CAMP_COORDINATOR, RECEPTIONIST, HOSPITAL_USER, DONOR

### 15 RabbitMQ Events

DonationCompletedEvent, CampCompletedEvent, TestResultAvailableEvent, UnitReleasedEvent, BloodStockUpdatedEvent, StockCriticalEvent, UnitExpiringEvent, BloodRequestCreatedEvent, BloodRequestMatchedEvent, EmergencyRequestEvent, TransfusionCompletedEvent, TransfusionReactionEvent, InvoiceGeneratedEvent, RecallInitiatedEvent

## Project Structure

```
BloodBank/
├── CLAUDE.md                    # THIS FILE
├── README.md
├── build.gradle.kts             # Root build (Spring Boot BOM, Java 21)
├── settings.gradle.kts          # All 20 modules
├── gradle.properties            # Version catalog
├── docker-compose.yml
├── Jenkinsfile
├── .claude/                     # Claude Code agent configuration
│   ├── settings.json
│   ├── commands/                # Slash commands
│   ├── hooks/                   # Event hooks
│   └── skills/                  # Reusable skills
├── backend/
│   ├── api-gateway/
│   ├── config-server/
│   ├── donor-service/
│   ├── inventory-service/
│   ├── lab-service/
│   ├── branch-service/
│   ├── transfusion-service/
│   ├── hospital-service/
│   ├── billing-service/
│   ├── request-matching-service/
│   ├── notification-service/
│   ├── reporting-service/
│   ├── document-service/
│   └── compliance-service/
├── shared-libs/
│   ├── common-dto/
│   ├── common-security/
│   ├── common-events/
│   ├── common-exceptions/
│   ├── common-model/
│   └── db-migration/
├── frontend/
│   └── bloodbank-ui/
├── keycloak/
├── k8s/
├── monitoring/
└── docs/
```

### Service Internal Structure (Standard for All Services)

```
backend/{service-name}/
├── build.gradle.kts
├── Dockerfile
└── src/
    ├── main/
    │   ├── java/com/bloodbank/{servicename}/
    │   │   ├── {ServiceName}Application.java
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/                    # Java 21 records
    │   │   ├── entity/                 # JPA entities, explicit getters/setters
    │   │   ├── event/                  # RabbitMQ publishers/listeners
    │   │   ├── exception/
    │   │   ├── mapper/                 # MapStruct interfaces
    │   │   ├── repository/
    │   │   └── service/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       ├── messages.properties
    │       ├── messages_es.properties
    │       └── messages_fr.properties
    └── test/
        └── java/com/bloodbank/{servicename}/
            ├── controller/
            ├── service/
            └── repository/
```

## 14 Services ↔ 24 Modules Mapping

| Service | Modules | Key Tables |
|---|---|---|
| donor-service | 1, 2, 9, 24 | donors, donor_health_records, donor_deferrals, donor_consents, donor_loyalty, collections, collection_adverse_reactions, collection_samples, blood_camps, camp_resources, camp_donors, camp_collections |
| inventory-service | 4, 5, 22 | blood_units, blood_components, component_processing, component_labels, pooled_components, storage_locations, stock_transfers, unit_disposals, unit_reservations, transport_requests, cold_chain_logs, transport_boxes, delivery_confirmations |
| lab-service | 3 | test_orders, test_results, test_panels, lab_instruments, quality_control_records |
| branch-service | 8, 17 | branches, branch_operating_hours, branch_equipment, branch_regions, blood_groups, component_types, deferral_reasons, reaction_types, countries, regions, cities, icd_codes |
| transfusion-service | 6, 7 | crossmatch_requests, crossmatch_results, blood_issues, emergency_issues, transfusions, transfusion_reactions, hemovigilance_reports, lookback_investigations |
| hospital-service | 10 | hospitals, hospital_contracts, hospital_requests, hospital_feedback |
| billing-service | 11 | rate_master, invoices, invoice_line_items, payments, credit_notes |
| request-matching-service | 6(matching), 23 | emergency_requests, disaster_events, donor_mobilizations |
| notification-service | 14 | notifications, notification_templates, notification_preferences, campaigns |
| reporting-service | 13, 18, 20 | audit_logs, digital_signatures, chain_of_custody, report_metadata, report_schedules, dashboard_widgets |
| document-service | 19 | documents, document_versions |
| compliance-service | 12 | regulatory_frameworks, sop_documents, licenses, deviations, recall_records |
| api-gateway | — | — |
| config-server | — | — |

## Testing Standards

- **Unit Tests**: JUnit 5 + Mockito, >80% line coverage, JaCoCo enforced
- **Integration Tests**: @SpringBootTest + Testcontainers (PostgreSQL, Redis, RabbitMQ)
- **E2E Tests**: Playwright or Cypress
- **Performance Tests**: Gatling or k6
- **Security Tests**: OWASP ZAP, Trivy, OWASP Dependency-Check, Snyk

## Regulatory Compliance

This is a healthcare application. All code must consider:
- **HIPAA** — PHI protection, access controls, audit trail, encryption
- **GDPR** — consent management, data erasure (anonymization), data portability
- **FDA 21 CFR Part 11** — electronic signatures, immutable audit trail
- **AABB Standards** — vein-to-vein traceability, chain of custody
- **WHO Guidelines** — blood safety, mandatory test panels

## Common Mistakes to Avoid

1. ❌ Using Lombok — NEVER
2. ❌ Database-per-service — we use SINGLE shared DB
3. ❌ Putting entity data in RabbitMQ events — events are thin (IDs only)
4. ❌ Forgetting `@Filter(name="branchFilter")` on branch-scoped entities
5. ❌ Forgetting `@PreAuthorize` on controller methods
6. ❌ Using `@Autowired` field injection — use constructor injection
7. ❌ Forgetting to extend `BaseEntity` or `BranchScopedEntity`
8. ❌ Using `spring.flyway.enabled=true` in services — must be `false`
9. ❌ Creating Flyway scripts inside service modules — they go in `shared-libs/db-migration/`
10. ❌ Returning raw entities from controllers — always use DTOs (records)