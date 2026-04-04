# M4: Support Services — billing, notification, reporting, document, compliance

**Duration:** 2 weeks
**Dependencies:** M2 (Core Services)
**Exit Gate:** All events consumed correctly, all support APIs functional

---

## Objective

Implement the 5 support services that handle billing, notifications, reporting/audit, document management, and regulatory compliance.

## Issues

### billing-service (Module 11)
- [ ] **M4-001**: Scaffold billing-service project structure
- [ ] **M4-002**: Create entities — RateMaster, Invoice, InvoiceLineItem, Payment, CreditNote
- [ ] **M4-003**: Create DTOs (records)
- [ ] **M4-004**: Create MapStruct mappers
- [ ] **M4-005**: Create repositories
- [ ] **M4-006**: Create services — RateService, InvoiceService, PaymentService
- [ ] **M4-007**: Implement auto-invoice generation from blood request matching
- [ ] **M4-008**: Implement multi-currency support with exchange rate handling
- [ ] **M4-009**: Implement GST/VAT tax calculation
- [ ] **M4-010**: Create controllers with @PreAuthorize (BILLING_CLERK role)
- [ ] **M4-011**: Create RabbitMQ publisher — InvoiceGeneratedEvent
- [ ] **M4-012**: Create RabbitMQ listener — BloodRequestMatchedEvent
- [ ] **M4-013**: Write unit tests (>80% coverage)
- [ ] **M4-014**: Write integration tests

### notification-service (Module 14)
- [ ] **M4-015**: Scaffold notification-service project structure
- [ ] **M4-016**: Create entities — Notification, NotificationTemplate, NotificationPreference, Campaign
- [ ] **M4-017**: Create DTOs (records)
- [ ] **M4-018**: Create MapStruct mappers
- [ ] **M4-019**: Create repositories
- [ ] **M4-020**: Create services — NotificationService, TemplateService, PreferenceService, CampaignService
- [ ] **M4-021**: Implement email channel (Spring Mail + Mailhog for dev)
- [ ] **M4-022**: Implement SMS channel integration interface
- [ ] **M4-023**: Implement push notification channel
- [ ] **M4-024**: Implement multi-language template rendering (en, es, fr)
- [ ] **M4-025**: Implement per-user notification preferences and opt-out
- [ ] **M4-026**: Create controllers with @PreAuthorize
- [ ] **M4-027**: Create RabbitMQ listeners for ALL domain events (14 event types)
- [ ] **M4-028**: Write unit tests (>80% coverage)
- [ ] **M4-029**: Write integration tests

### reporting-service (Modules 13, 18, 20)
- [ ] **M4-030**: Scaffold reporting-service project structure
- [ ] **M4-031**: Create entities — AuditLog, DigitalSignature, ChainOfCustody
- [ ] **M4-032**: Create entities — ReportMetadata, ReportSchedule, DashboardWidget
- [ ] **M4-033**: Create DTOs (records)
- [ ] **M4-034**: Create MapStruct mappers
- [ ] **M4-035**: Create repositories
- [ ] **M4-036**: Create services — AuditService, ReportService, DashboardService, ChainOfCustodyService
- [ ] **M4-037**: Implement immutable audit log writing (append-only)
- [ ] **M4-038**: Implement chain of custody tracking (vein-to-vein)
- [ ] **M4-039**: Implement dashboard data aggregation endpoints
- [ ] **M4-040**: Create controllers with @PreAuthorize (AUDITOR, REGIONAL_ADMIN roles)
- [ ] **M4-041**: Create RabbitMQ listeners for ALL events (audit trail)
- [ ] **M4-042**: Write unit tests (>80% coverage)
- [ ] **M4-043**: Write integration tests

### document-service (Module 19)
- [ ] **M4-044**: Scaffold document-service project structure
- [ ] **M4-045**: Create entities — Document, DocumentVersion
- [ ] **M4-046**: Create DTOs (records)
- [ ] **M4-047**: Create MapStruct mappers
- [ ] **M4-048**: Create repositories
- [ ] **M4-049**: Create services — DocumentService, DocumentVersionService
- [ ] **M4-050**: Implement MinIO/S3 integration for file storage
- [ ] **M4-051**: Implement document versioning
- [ ] **M4-052**: Create controllers with @PreAuthorize
- [ ] **M4-053**: Write unit tests (>80% coverage)
- [ ] **M4-054**: Write integration tests

### compliance-service (Module 12)
- [ ] **M4-055**: Scaffold compliance-service project structure
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
