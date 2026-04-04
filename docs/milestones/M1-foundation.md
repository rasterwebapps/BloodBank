# M1: Foundation — Build System, Database, Shared Libraries

**Duration:** 2 weeks
**Dependencies:** M0 (Project Setup)
**Exit Gate:** Gradle builds successfully, Flyway creates all 87 tables, shared libs compile

---

## Objective

Create the complete project skeleton — Gradle multi-module build, all 20 Flyway migrations (~87 tables), and 6 shared libraries.

## Issues

### Build System (Gradle 8 Kotlin DSL)
- [ ] **M1-001**: Create root `build.gradle.kts` with Spring Boot BOM, Java 21 config, common plugins
- [ ] **M1-002**: Create `settings.gradle.kts` including all 20 modules (14 services + 6 shared-libs)
- [ ] **M1-003**: Create `gradle.properties` with version catalog (Spring Boot 3.4.x, Spring Cloud 2024.x, MapStruct 1.6+, etc.)
- [ ] **M1-004**: Configure JaCoCo plugin with 80% coverage threshold
- [ ] **M1-005**: Configure Checkstyle and SpotBugs plugins
- [ ] **M1-006**: Create `build.gradle.kts` for each of the 14 services with correct dependencies

### Database Migrations (Flyway)
- [ ] **M1-007**: Create V1 — Master tables (blood_groups, component_types, countries, regions, cities, deferral_reasons, reaction_types, icd_codes)
- [ ] **M1-008**: Create V2 — Branch tables (branches, branch_operating_hours, branch_equipment, branch_regions)
- [ ] **M1-009**: Create V3 — Donor tables (donors, donor_health_records, donor_deferrals, donor_consents, donor_loyalty)
- [ ] **M1-010**: Create V4 — Collection tables (collections, collection_adverse_reactions, collection_samples)
- [ ] **M1-011**: Create V5 — Blood camp tables (blood_camps, camp_resources, camp_donors, camp_collections)
- [ ] **M1-012**: Create V6 — Lab tables (test_orders, test_results, test_panels, lab_instruments, quality_control_records)
- [ ] **M1-013**: Create V7 — Inventory tables (blood_units, blood_components, component_processing, component_labels, pooled_components, storage_locations, stock_transfers, unit_disposals, unit_reservations)
- [ ] **M1-014**: Create V8 — Transfusion tables (crossmatch_requests, crossmatch_results, blood_issues, emergency_issues, transfusions, transfusion_reactions, hemovigilance_reports, lookback_investigations)
- [ ] **M1-015**: Create V9 — Hospital tables (hospitals, hospital_contracts, hospital_requests, hospital_feedback)
- [ ] **M1-016**: Create V10 — Billing tables (rate_master, invoices, invoice_line_items, payments, credit_notes)
- [ ] **M1-017**: Create V11 — Notification tables (notifications, notification_templates, notification_preferences, campaigns)
- [ ] **M1-018**: Create V12 — Compliance tables (regulatory_frameworks, sop_documents, licenses, deviations, recall_records)
- [ ] **M1-019**: Create V13 — Reporting tables (audit_logs, digital_signatures, chain_of_custody, report_metadata, report_schedules, dashboard_widgets) + append-only trigger for audit_logs
- [ ] **M1-020**: Create V14 — Document tables (documents, document_versions)
- [ ] **M1-021**: Create V15 — Logistics tables (transport_requests, cold_chain_logs, transport_boxes, delivery_confirmations)
- [ ] **M1-022**: Create V16 — Emergency tables (emergency_requests, disaster_events, donor_mobilizations)
- [ ] **M1-023**: Create V17 — User management tables (user_profiles, user_branch_assignments, user_activity_logs, user_sessions, role_change_audit)
- [ ] **M1-024**: Create V18 — System tables (system_settings, feature_flags, scheduled_jobs, tenant_configs)
- [ ] **M1-025**: Create V19 — All indexes (FKs, branch_id composites, status, created_at, search)
- [ ] **M1-026**: Create V20 — Seed reference data (blood groups, component types, test panels, countries, templates)

### Shared Libraries
- [ ] **M1-027**: `common-model` — BaseEntity, BranchScopedEntity, AuditableEntity, enums (BloodGroupEnum, ComponentStatusEnum, etc.)
- [ ] **M1-028**: `common-dto` — ApiResponse<T>, PagedResponse<T>, ErrorResponse, ValidationError
- [ ] **M1-029**: `common-events` — All 15 event records (DonationCompletedEvent, etc.)
- [ ] **M1-030**: `common-security` — SecurityConfig, BranchDataFilterAspect, DataMaskingAspect, JwtUtils, role constants
- [ ] **M1-031**: `common-exceptions` — ResourceNotFoundException, BusinessException, ConflictException, UnauthorizedException, GlobalExceptionHandler
- [ ] **M1-032**: `db-migration` — Flyway configuration, migration test

### Docker Compose
- [ ] **M1-033**: Create `docker-compose.yml` with PostgreSQL 17, Redis 7, RabbitMQ 3.13, Keycloak 26, MinIO, Mailhog

## Deliverables

1. Compilable Gradle multi-module project with all 20 modules
2. 20 Flyway migration scripts creating ~87 tables
3. 6 shared libraries with base classes, DTOs, events, security, exceptions
4. Docker Compose for local development infrastructure
