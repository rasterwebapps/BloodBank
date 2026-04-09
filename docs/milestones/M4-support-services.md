# M4: Support Services — billing, notification, reporting, document, compliance

**Duration:** 2 weeks
**Dependencies:** M2 (Core Services)
**Exit Gate:** All events consumed correctly, all support APIs functional

## 📊 Development Status: ✅ COMPLETE (100%)

**PRs:** #12 (merged 2026-04-07), subsequent PRs completed remaining work
**Issues Completed:** 66/66
- billing-service: ✅ Complete (41 main files, 8 test files)
- notification-service: ✅ Complete (36 main files, 8 test files)
- reporting-service: ✅ Complete (51 main files, 8 test files)
- document-service: ✅ Complete (20 main files, 4 test files, 34 @Test methods)
- compliance-service: ✅ Complete (51 main files, 10 test files, 152 @Test methods)

---

## Objective

Implement the 5 support services that handle billing, notifications, reporting/audit, document management, and regulatory compliance.

## Issues

### billing-service (Module 11)
- [x] **M4-001**: Scaffold billing-service project structure *(PR #12)*
- [x] **M4-002**: Create entities — RateMaster, Invoice, InvoiceLineItem, Payment, CreditNote *(PR #12)*
- [x] **M4-003**: Create DTOs (records) *(PR #12)*
- [x] **M4-004**: Create MapStruct mappers *(PR #12)*
- [x] **M4-005**: Create repositories *(PR #12)*
- [x] **M4-006**: Create services — RateService, InvoiceService, PaymentService *(PR #12)*
- [x] **M4-007**: Implement auto-invoice generation from blood request matching *(PR #12 — via BloodRequestMatchedEvent listener)*
- [x] **M4-008**: Implement multi-currency support with exchange rate handling *(PR #12)*
- [x] **M4-009**: Implement GST/VAT tax calculation *(PR #12)*
- [x] **M4-010**: Create controllers with @PreAuthorize (BILLING_CLERK role) *(PR #12 — 4 controllers)*
- [x] **M4-011**: Create RabbitMQ publisher — InvoiceGeneratedEvent *(PR #12)*
- [x] **M4-012**: Create RabbitMQ listener — BloodRequestMatchedEvent *(PR #12)*
- [x] **M4-013**: Write unit tests (>80% coverage) *(PR #12 — 8 test classes)*
- [x] **M4-014**: Write integration tests *(PR #12)*

### notification-service (Module 14)
- [x] **M4-015**: Scaffold notification-service project structure *(PR #12)*
- [x] **M4-016**: Create entities — Notification, NotificationTemplate, NotificationPreference, Campaign *(PR #12)*
- [x] **M4-017**: Create DTOs (records) *(PR #12)*
- [x] **M4-018**: Create MapStruct mappers *(PR #12)*
- [x] **M4-019**: Create repositories *(PR #12)*
- [x] **M4-020**: Create services — NotificationService, TemplateService, PreferenceService, CampaignService *(PR #12)*
- [x] **M4-021**: Implement email channel (Spring Mail + Mailhog for dev) *(PR #12)*
- [x] **M4-022**: Implement SMS channel integration interface *(PR #12)*
- [x] **M4-023**: Implement push notification channel *(PR #12)*
- [x] **M4-024**: Implement multi-language template rendering (en, es, fr) *(PR #12)*
- [x] **M4-025**: Implement per-user notification preferences and opt-out *(PR #12)*
- [x] **M4-026**: Create controllers with @PreAuthorize *(PR #12 — 4 controllers)*
- [x] **M4-027**: Create RabbitMQ listeners for ALL domain events (14 event types) *(PR #12 — DomainEventListener with 14 @RabbitListener methods)*
- [x] **M4-028**: Write unit tests (>80% coverage) *(PR #12 — 8 test classes)*
- [x] **M4-029**: Write integration tests *(PR #12)*

### reporting-service (Modules 13, 18, 20)
- [x] **M4-030**: Scaffold reporting-service project structure *(PR #12)*
- [x] **M4-031**: Create entities — AuditLog, DigitalSignature, ChainOfCustody *(PR #12)*
- [x] **M4-032**: Create entities — ReportMetadata, ReportSchedule, DashboardWidget *(PR #12)*
- [x] **M4-033**: Create DTOs (records) *(PR #12)*
- [x] **M4-034**: Create MapStruct mappers *(PR #12)*
- [x] **M4-035**: Create repositories *(PR #12)*
- [x] **M4-036**: Create services — AuditService, ReportService, DashboardService, ChainOfCustodyService *(PR #12)*
- [x] **M4-037**: Implement immutable audit log writing (append-only) *(PR #12)*
- [x] **M4-038**: Implement chain of custody tracking (vein-to-vein) *(PR #12)*
- [x] **M4-039**: Implement dashboard data aggregation endpoints *(PR #12)*
- [x] **M4-040**: Create controllers with @PreAuthorize (AUDITOR, REGIONAL_ADMIN roles) *(PR #12 — 5 controllers)*
- [x] **M4-041**: Create RabbitMQ listeners for ALL events (audit trail) *(PR #12 — AuditEventListener consuming 14 events)*
- [x] **M4-042**: Write unit tests (>80% coverage) *(PR #12 — 8 test classes)*
- [x] **M4-043**: Write integration tests *(PR #12)*

### document-service (Module 19)
- [x] **M4-044**: Scaffold document-service project structure *(PR #12)*
- [x] **M4-045**: Create entities — Document, DocumentVersion *(PR #12)*
- [x] **M4-046**: Create DTOs (records) *(PR #12)*
- [x] **M4-047**: Create MapStruct mappers *(PR #12)*
- [x] **M4-048**: Create repositories *(PR #12)*
- [x] **M4-049**: Create services — DocumentService, DocumentVersionService *(PR #12)*
- [x] **M4-050**: Implement MinIO/S3 integration for file storage *(PR #12 — StorageService + LocalStorageService)*
- [x] **M4-051**: Implement document versioning *(PR #12)*
- [x] **M4-052**: Create controllers with @PreAuthorize *(PR #12 — 2 controllers)*
- [x] **M4-053**: Write unit tests (>80% coverage) *(4 test classes: DocumentServiceTest 11 tests, DocumentVersionServiceTest 7 tests, DocumentControllerTest 10 tests, DocumentVersionControllerTest 6 tests — 34 total @Test methods)*
- [x] **M4-054**: Write integration tests *(controller tests with @WebMvcTest + @WithMockUser for role verification)*

### compliance-service (Module 12)
- [x] **M4-055**: Scaffold compliance-service project structure *(51 main files, 10 test files — full service implementation)*
- [x] **M4-056**: Create entities — RegulatoryFramework (BaseEntity), SopDocument, License, Deviation, RecallRecord (BranchScopedEntity with @FilterDef/@Filter) *(5 entities, 11 enums)*
- [x] **M4-057**: Create DTOs (records) *(11 Java 21 record classes — request + response DTOs for all entities)*
- [x] **M4-058**: Create MapStruct mappers *(5 mappers: DeviationMapper, LicenseMapper, RecallRecordMapper, RegulatoryFrameworkMapper, SopDocumentMapper — all @Mapper(componentModel="spring"))*
- [x] **M4-059**: Create repositories *(5 repositories: DeviationRepository, LicenseRepository, RecallRecordRepository, RegulatoryFrameworkRepository, SopDocumentRepository — all extend JpaRepository + JpaSpecificationExecutor)*
- [x] **M4-060**: Create services — ComplianceService, SopService, LicenseService, DeviationService, RecallService *(5 services with constructor injection + explicit Logger)*
- [x] **M4-061**: Implement recall management workflow *(RecallService: create with RecallInitiatedEvent publishing, updateStatus, close)*
- [x] **M4-062**: Implement deviation/CAPA tracking *(DeviationService: create, investigate, addCorrectiveAction, close, reopen — full workflow)*
- [x] **M4-063**: Create controllers with @PreAuthorize (AUDITOR role) *(5 controllers: ComplianceController, DeviationController, LicenseController, RecallController, SopController — 34 endpoints, @PreAuthorize on every method)*
- [x] **M4-064**: Create RabbitMQ publisher — RecallInitiatedEvent *(EventPublisher class publishes RecallInitiatedEvent via RabbitTemplate)*
- [x] **M4-065**: Write unit tests (>80% coverage) *(10 test classes: 5 service tests + 5 controller tests — 152 total @Test methods)*
- [x] **M4-066**: Write integration tests *(controller tests with @WebMvcTest + @WithMockUser for role-based access verification)*

## Deliverables

1. ✅ 5 running support services
2. ✅ Notification service consuming all 14 event types
3. ✅ Reporting service with immutable audit trail
4. ✅ MinIO document storage integration
5. ✅ Multi-currency billing with tax support
