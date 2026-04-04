# Skill: Scaffold Spring Boot Microservice

Generate a complete Spring Boot microservice under `backend/$ARGUMENTS/`.

## Pre-Conditions

1. Read `CLAUDE.md` for ALL project rules (NO LOMBOK, Java 21 records, constructor injection)
2. Read `README.md` to identify which modules, entities, events, and tables belong to this service

## Steps

1. **build.gradle.kts** — Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, spring-boot-starter-amqp, spring-boot-starter-data-redis, spring-boot-starter-actuator, springdoc-openapi, mapstruct, common-dto, common-security, common-events, common-exceptions, common-model
2. **Dockerfile** — Multi-stage: Gradle 8 + Java 21 build → Temurin JRE 21 Alpine runtime
3. **{ServiceName}Application.java** — `@SpringBootApplication` main class with `@EnableJpaRepositories`, `@EnableCaching`
4. **config/** — `SecurityConfig.java` (OAuth2 resource server), `RabbitMQConfig.java`, `RedisConfig.java`, `OpenApiConfig.java`, `JpaConfig.java`
5. **entity/** — All entities extending `BaseEntity` or `BranchScopedEntity`, `@Filter` on branch-scoped, explicit getters/setters, NO LOMBOK
6. **repository/** — `JpaRepository<Entity, UUID>` + `JpaSpecificationExecutor<Entity>`, custom query methods
7. **dto/** — Java 21 records with Jakarta Validation annotations (`@NotBlank`, `@NotNull`, `@Email`, `@Pattern`)
8. **mapper/** — MapStruct interfaces: `@Mapper(componentModel = "spring")`, toEntity/toResponse/toResponseList
9. **service/** — Constructor injection, `@Transactional(readOnly = true)` class-level, `@Transactional` on write methods, `LoggerFactory.getLogger()` explicit logger
10. **controller/** — `@RestController`, `@RequestMapping("/api/v1/{resource}")`, `@PreAuthorize` on EVERY method, `@Tag` OpenAPI, return `ResponseEntity<ApiResponse<T>>`
11. **event/** — RabbitMQ publishers/listeners (only events mapped to this service per README), thin payloads (IDs only)
12. **exception/** — Service-specific exceptions extending common exceptions
13. **application.yml** — `spring.flyway.enabled=false`, datasource, redis, rabbitmq, keycloak config
14. **application-dev.yml** — Local dev overrides (localhost URLs)
15. **application-prod.yml** — Production config (K8s service names, secrets refs)
16. **messages.properties** — English messages
17. **messages_es.properties** — Spanish messages
18. **messages_fr.properties** — French messages

## Test Generation

19. **Unit Tests** — JUnit 5 + Mockito for all service classes, >80% coverage target
20. **Integration Tests** — `@SpringBootTest` + Testcontainers (PostgreSQL, Redis, RabbitMQ)
21. **Controller Tests** — `@WebMvcTest` with `@WithMockUser` for each role combination

## Service-Module Mapping Reference

| Service | Modules | Key Entities |
|---|---|---|
| donor-service | 1, 2, 9, 24 | Donor, DonorHealthRecord, DonorDeferral, DonorConsent, DonorLoyalty, Collection, CollectionAdverseReaction, CollectionSample, BloodCamp, CampResource, CampDonor, CampCollection |
| inventory-service | 4, 5, 22 | BloodUnit, BloodComponent, ComponentProcessing, ComponentLabel, PooledComponent, StorageLocation, StockTransfer, UnitDisposal, UnitReservation, TransportRequest, ColdChainLog, TransportBox, DeliveryConfirmation |
| lab-service | 3 | TestOrder, TestResult, TestPanel, LabInstrument, QualityControlRecord |
| branch-service | 8, 17 | Branch, BranchOperatingHours, BranchEquipment, BranchRegion, BloodGroup, ComponentType, DeferralReason, ReactionType, Country, Region, City, IcdCode |
| transfusion-service | 6, 7 | CrossMatchRequest, CrossMatchResult, BloodIssue, EmergencyIssue, Transfusion, TransfusionReaction, HemovigilanceReport, LookBackInvestigation |
| hospital-service | 10 | Hospital, HospitalContract, HospitalRequest, HospitalFeedback |
| billing-service | 11 | RateMaster, Invoice, InvoiceLineItem, Payment, CreditNote |
| request-matching-service | 6(matching), 23 | EmergencyRequest, DisasterEvent, DonorMobilization |
| notification-service | 14 | Notification, NotificationTemplate, NotificationPreference, Campaign |
| reporting-service | 13, 18, 20 | AuditLog, DigitalSignature, ChainOfCustody, ReportMetadata, ReportSchedule, DashboardWidget |
| document-service | 19 | Document, DocumentVersion |
| compliance-service | 12 | RegulatoryFramework, SopDocument, License, Deviation, RecallRecord |
| api-gateway | — | JWT validation, rate limiting, routing, CORS |
| config-server | — | Centralized configuration distribution |

## Validation Checklist

- [ ] Zero Lombok annotations in all generated files
- [ ] All DTOs are Java 21 records
- [ ] All entities extend BaseEntity or BranchScopedEntity
- [ ] All branch-scoped entities have `@FilterDef` + `@Filter`
- [ ] All controllers have `@PreAuthorize` on every public method
- [ ] All services use constructor injection (no `@Autowired`)
- [ ] All loggers use `LoggerFactory.getLogger()`
- [ ] `spring.flyway.enabled=false` in application.yml
- [ ] Events carry IDs only (thin payloads)
- [ ] API prefix is `/api/v1/`
- [ ] Response wrapper is `ApiResponse<T>`
