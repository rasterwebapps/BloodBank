# /project:scaffold-service

Generate a complete Spring Boot microservice under `backend/$ARGUMENTS/`.

## Steps

1. Read CLAUDE.md for all project rules (NO LOMBOK, Java 21 records, constructor injection)
2. Read README.md to identify which modules, entities, events, and tables belong to this service
3. Generate `build.gradle.kts` with dependencies (spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, spring-boot-starter-amqp, spring-boot-starter-data-redis, spring-boot-starter-actuator, springdoc-openapi, mapstruct, common-dto, common-security, common-events, common-exceptions, common-model)
4. Generate `Dockerfile` (multi-stage: Gradle 8 + Java 21 build → Temurin JRE 21 Alpine runtime)
5. Generate `{ServiceName}Application.java` main class
6. Generate all entity classes — extend BaseEntity or BranchScopedEntity, @Filter on branch-scoped, explicit getters/setters
7. Generate all repository interfaces — JpaRepository + JpaSpecificationExecutor
8. Generate all request/response DTOs as Java 21 records with Jakarta Validation
9. Generate MapStruct mapper interfaces
10. Generate service classes — constructor injection, @Transactional, LoggerFactory logger
11. Generate REST controllers — @PreAuthorize per role matrix, OpenAPI annotations, return ApiResponse<T>
12. Generate RabbitMQ event publishers/listeners (only for events mapped to this service)
13. Generate application.yml, application-dev.yml, application-prod.yml (spring.flyway.enabled=false)
14. Generate messages.properties, messages_es.properties, messages_fr.properties
15. Generate unit tests for all service classes (JUnit 5 + Mockito, >80% coverage target)
16. Generate integration tests using Testcontainers (PostgreSQL, Redis, RabbitMQ)