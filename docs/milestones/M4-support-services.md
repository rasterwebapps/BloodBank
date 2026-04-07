# M4: Support Services — billing, notification, reporting, document, compliance

**Duration:** 2 weeks
**Dependencies:** M2 (Core Services)
**Exit Gate:** All events consumed correctly, all support APIs functional

## 📊 Development Status: 🟡 PARTIAL (~79%)

**PR:** #12 (merged 2026-04-07) — 4 of 5 services implemented
**Issues Completed:** 52/66
- billing-service: ✅ Complete (41 main files, 8 test files)
- notification-service: ✅ Complete (36 main files, 8 test files)
- reporting-service: ✅ Complete (51 main files, 8 test files)
- document-service: ⚠️ Partial (20 main files, 0 test files)
- compliance-service: ❌ Scaffold only (1 file)

## 🔧 FIX REQUIRED

| # | Issue | Severity | Description |
|---|---|---|---|
| 1 | **compliance-service not implemented** | 🔴 HIGH | Only Application.java exists. All 5 entities (RegulatoryFramework, SopDocument, License, Deviation, RecallRecord), DTOs, services, controllers, RecallInitiatedEvent publisher, and all tests need to be created. This blocks M5. |
| 2 | **document-service missing tests** | 🟡 MEDIUM | 20 main source files exist (entities, services, controllers, MinIO storage), but 0 test files. Needs unit tests for DocumentService + DocumentVersionService, and controller tests for DocumentController + DocumentVersionController. Quality gate (>80% coverage) not met. |
| 3 | **Blocks M5 (Gateway + Frontend)** | 🟡 HIGH | M5 depends on M4 completion. compliance-service prevents closing this milestone. |

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
- [ ] **M4-053**: Write unit tests (>80% coverage) *(❌ 0 test files — NEEDS FIX)*
- [ ] **M4-054**: Write integration tests *(❌ not started)*

### compliance-service (Module 12)
- [ ] **M4-055**: Scaffold compliance-service project structure *(⚠️ only Application.java exists)*
- [ ] **M4-056**: Create entities — RegulatoryFramework, SopDocument, License, Deviation, RecallRecord
- [ ] **M4-057**: Create DTOs (records)
- [ ] **M4-058**: Create MapStruct mappers
- [ ] **M4-059**: Create repositories
- [ ] **M4-060**: Create services — ComplianceService, SopService, LicenseService, DeviationService, RecallService
- [ ] **M4-061**: Implement recall management workflow
- [ ] **M4-062**: Implement deviation/CAPA tracking
- [ ] **M4-063**: Create controllers with @PreAuthorize (AUDITOR role)
- [ ] **M4-064**: Create RabbitMQ publisher — RecallInitiatedEvent
- [ ] **M4-065**: Write unit tests (>80% coverage)
- [ ] **M4-066**: Write integration tests

## Deliverables

1. 5 running support services
2. Notification service consuming all 14 event types
3. Reporting service with immutable audit trail
4. MinIO document storage integration
5. Multi-currency billing with tax support
