## Description

<!-- Briefly describe the changes in this PR. -->

## Related Issue

<!-- Link to the GitHub issue this PR addresses. -->

Closes #

## Type of Change

- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update
- [ ] Refactoring (no functional changes)
- [ ] Infrastructure/DevOps change

## Service(s) Modified

- [ ] donor-service
- [ ] inventory-service
- [ ] lab-service
- [ ] branch-service
- [ ] transfusion-service
- [ ] hospital-service
- [ ] billing-service
- [ ] request-matching-service
- [ ] notification-service
- [ ] reporting-service
- [ ] document-service
- [ ] compliance-service
- [ ] api-gateway
- [ ] config-server
- [ ] frontend (Angular)
- [ ] shared-libs
- [ ] db-migration (Flyway)

## Checklist

### Code Quality

- [ ] **No Lombok** — No `@Data`, `@Getter`, `@Setter`, `@Builder`, `@Slf4j`, or any Lombok annotation used
- [ ] Java 21 records used for all DTOs and events
- [ ] Explicit getters/setters on JPA entities
- [ ] Constructor injection used (no `@Autowired` field injection)
- [ ] `LoggerFactory.getLogger()` used for logging (no `@Slf4j`)

### Security

- [ ] `@PreAuthorize` present on **every** controller method
- [ ] `@Filter(name = "branchFilter")` on all branch-scoped entities
- [ ] No sensitive data (passwords, tokens, PII) logged or exposed
- [ ] Input validation with Jakarta Validation annotations

### Database

- [ ] Flyway migrations in `shared-libs/db-migration/` (not in service modules)
- [ ] `spring.flyway.enabled=false` in service `application.yml`
- [ ] `branch_id` column on all branch-scoped tables
- [ ] Appropriate indexes added for new tables/columns

### Events

- [ ] RabbitMQ events carry **IDs only** (thin payloads)
- [ ] Events defined as Java 21 records in `shared-libs/common-events/`

### Testing

- [ ] Unit tests written (JUnit 5 + Mockito)
- [ ] Integration tests written (Testcontainers) where applicable
- [ ] Tests pass locally (`./gradlew test`)
- [ ] Code coverage ≥ 80% for changed files

### API

- [ ] Endpoints follow `/api/v1/` prefix convention
- [ ] Responses wrapped in `ApiResponse<T>`
- [ ] Pagination uses `PagedResponse<T>`
- [ ] Errors follow RFC 7807 Problem Details

### Documentation

- [ ] Code comments added where necessary
- [ ] API documentation (OpenAPI annotations) updated
- [ ] README or docs updated if applicable

## Screenshots (if applicable)

<!-- Add screenshots for UI changes -->

## Testing Instructions

<!-- Describe how reviewers can test these changes -->

1. 
2. 
3. 

## Additional Notes

<!-- Any additional context for reviewers -->
