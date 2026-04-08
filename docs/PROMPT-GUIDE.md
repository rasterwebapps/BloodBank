# BloodBank Project — Copilot Agent Prompt Guide

> **52 prompts to complete the project.** One prompt → one agent → one PR → review → merge → next.

## How to Use This Guide

1. **Merge the agents PR first** — all 8 agent files must exist in `.github/agents/`
2. **Execute prompts in order** — each prompt depends on the previous one being merged
3. **Exception: M7 (Phase 5)** — can run in parallel with Phases 1–4
4. **Never skip the `@project-tracker` prompts** — they verify completeness before moving on

## Agent Quick Reference

```
TASK                                    AGENT TO USE
────────────────────────────────────    ──────────────────
New/modify database tables or columns   @migration-author
Build Java APIs (entity → controller)   @backend-developer
Write unit/controller/integration tests @test-writer
Build Angular components/pages/routes   @frontend-developer
Review/fix roles, auth, branch filter   @security-reviewer
Create Docker/K8s/Jenkins/monitoring    @devops-engineer
Update README, docs, architecture       @docs-writer
Check project status, what's next       @project-tracker
```

---

## Current Project Status (as of 2026-04-08)

| Milestone | Status | Completion | What's Done | What's Left |
|---|---|---|---|---|
| M0 | ✅ COMPLETE | 100% | Docs, skills, hooks, milestones | — |
| M1 | ✅ COMPLETE | 100% | Gradle, Docker, skeleton | — |
| M2 | ✅ COMPLETE | 100% | 20 Flyway migrations, 6 shared libs, 4 core services | — |
| M3 | 🟡 IN PROGRESS | ~35% | transfusion-service complete | hospital-service incomplete, request-matching scaffold only |
| M4 | 🟡 IN PROGRESS | ~79% | billing, notification, reporting complete | compliance-service scaffold only, document-service 0 tests |
| M5 | 🟡 IN PROGRESS | ~29% | API Gateway + Config Server | Angular frontend not started |
| M6 | 🔴 NOT STARTED | 0% | — | Blocked by M5 |
| M7 | 🔴 NOT STARTED | 0% | — | **Can start NOW** (depends only on M2) |
| M8 | 🔴 NOT STARTED | 0% | — | Blocked by M6 |
| M9 | 🔴 NOT STARTED | 0% | — | Blocked by M8 |
| M10 | 🔴 NOT STARTED | 0% | — | Blocked by M9 |
| M11 | 🔴 NOT STARTED | 0% | — | Blocked by M10 |
| M12 | 🔴 NOT STARTED | 0% | — | Blocked by M11 |
| M13 | 🔴 NOT STARTED | 0% | — | Blocked by M12 |

---

## PHASE 1: Finish M3 — Clinical Services (28 issues remaining)

### PROMPT #1 → `@backend-developer`

```
Complete hospital-service (M3-016 to M3-023).

Current state on main:
- Application class, build.gradle.kts, application.yml exist
- Hospital and HospitalContract entities exist with 6 enums
- MISSING: everything else

What to implement:

1. ENTITIES (complete the remaining 2):
   - HospitalRequest entity (table: hospital_requests from V9__hospital_tables.sql)
     Columns: hospital_id, blood_group, component_type, quantity, priority, status,
     required_date, clinical_notes, patient_id
   - HospitalFeedback entity (table: hospital_feedback from V9__hospital_tables.sql)
     Columns: hospital_id, request_id, rating, feedback_text, response_text
   - Both must extend BranchScopedEntity with @FilterDef/@Filter

2. DTOs (Java 21 records):
   - HospitalCreateRequest, HospitalResponse
   - HospitalContractCreateRequest, HospitalContractResponse
   - HospitalRequestCreateRequest, HospitalRequestResponse
   - HospitalFeedbackCreateRequest, HospitalFeedbackResponse

3. REPOSITORIES: for all 4 entities

4. MAPPERS: MapStruct mappers for all 4 entities

5. SERVICES:
   - HospitalService — CRUD for hospitals
   - ContractService — manage hospital contracts, SLA tracking
   - BloodRequestService — submit/track blood requests, credit management
   - FeedbackService — submit/respond to feedback

6. CONTROLLERS with @PreAuthorize:
   - HospitalController (/api/v1/hospitals) — BRANCH_ADMIN, BRANCH_MANAGER
   - HospitalContractController (/api/v1/hospitals/{id}/contracts) — BRANCH_ADMIN
   - HospitalRequestController (/api/v1/hospital-requests) — HOSPITAL_USER, BRANCH_MANAGER
   - HospitalFeedbackController (/api/v1/hospital-feedback) — HOSPITAL_USER

7. RABBITMQ:
   - Publish BloodRequestCreatedEvent when hospital submits a blood request

Reference: backend/donor-service/ for complete pattern (entities, DTOs, services, controllers)
Reference: docs/security/rbac-matrix.md for role assignments
Reference: docs/architecture/event-contracts.md for event definition
```

---

### PROMPT #2 → `@test-writer`

```
Write tests for hospital-service (M3-024, M3-025).

Service has 4 services and 4 controllers to test:
- HospitalService, ContractService, BloodRequestService, FeedbackService
- HospitalController, HospitalContractController, HospitalRequestController, HospitalFeedbackController

Write:
1. Unit tests (JUnit 5 + Mockito) for all 4 services
2. Controller tests (@WebMvcTest) for all 4 controllers
3. Test role access:
   - HOSPITAL_USER can submit requests and feedback
   - BRANCH_ADMIN can manage hospitals and contracts
   - BRANCH_MANAGER can view requests
   - DONOR role should get 403 on all endpoints

Target: >80% JaCoCo coverage

Reference patterns:
- backend/donor-service/src/test/java/**/service/DonorServiceTest.java
- backend/donor-service/src/test/java/**/controller/DonorControllerTest.java
```

---

### PROMPT #3 → `@backend-developer`

```
Implement request-matching-service (M3-026 to M3-038).

Current state: Only Application.java exists. Port 8087.

What to implement:

1. APPLICATION CONFIG:
   - application.yml with port 8087, spring.flyway.enabled=false
   - RabbitMQ config for listeners and publishers

2. ENTITIES (all extend BranchScopedEntity with @FilterDef/@Filter):
   - EmergencyRequest (table: emergency_requests from V16__emergency_tables.sql)
     Columns: requesting_hospital_id, blood_group, component_type, quantity,
     priority, status, patient_condition, required_by
   - DisasterEvent (table: disaster_events from V16)
     Columns: event_name, event_type, severity, location, start_time, end_time,
     status, estimated_casualties, blood_units_needed
   - DonorMobilization (table: donor_mobilizations from V16)
     Columns: disaster_event_id, donor_id, status, mobilization_type,
     notification_sent_at, response_received_at

3. ENUMS: EmergencyPriorityEnum, EmergencyStatusEnum, DisasterTypeEnum,
   DisasterSeverityEnum, MobilizationStatusEnum, MobilizationTypeEnum

4. DTOs: Java 21 records for all entities (request + response pairs)

5. REPOSITORIES: for all 3 entities

6. MAPPERS: MapStruct for all 3 entities

7. SERVICES:
   - RequestMatchingService — match blood requests to available inventory
     using ABO/Rh compatibility + FEFO selection (oldest-expiring first)
   - EmergencyService — emergency blood request workflow, O-negative
     emergency protocol, priority escalation
   - DisasterResponseService — mass casualty protocol, donor mobilization,
     emergency stock rebalancing across branches

8. CONTROLLERS with @PreAuthorize:
   - RequestMatchingController (/api/v1/matching) — DOCTOR, BRANCH_MANAGER
   - EmergencyController (/api/v1/emergencies) — DOCTOR, BRANCH_MANAGER
   - DisasterResponseController (/api/v1/disasters) — SUPER_ADMIN, REGIONAL_ADMIN

9. RABBITMQ:
   - Listen: BloodStockUpdatedEvent, BloodRequestCreatedEvent
   - Publish: BloodRequestMatchedEvent, EmergencyRequestEvent

Reference: backend/transfusion-service/ for BloodCompatibilityUtil pattern
Reference: backend/inventory-service/ for FEFO dispatch pattern
Reference: docs/architecture/event-contracts.md for event definitions
```

---

### PROMPT #4 → `@test-writer`

```
Write tests for request-matching-service (M3-039, M3-040).

Service has 3 services and 3 controllers:
- RequestMatchingService, EmergencyService, DisasterResponseService
- RequestMatchingController, EmergencyController, DisasterResponseController

Write:
1. Unit tests for all 3 services
   - RequestMatchingService: test ABO/Rh compatibility matching, FEFO selection,
     no-match scenario, partial match
   - EmergencyService: test O-negative emergency protocol, priority escalation
   - DisasterResponseService: test mass casualty protocol, donor mobilization
2. Controller tests (@WebMvcTest) for all 3 controllers
3. Test role access:
   - DOCTOR can create emergency requests and matching requests
   - SUPER_ADMIN can manage disaster events
   - HOSPITAL_USER should get 403 on matching endpoints

Target: >80% JaCoCo coverage

Reference: backend/transfusion-service/src/test/java/ for similar test patterns
```

---

### PROMPT #5 → `@test-writer`

```
Write cross-service clinical workflow integration tests (M3-041, M3-042, M3-043).

Create test files in a new directory: backend/integration-tests/src/test/java/

M3-041: Hospital Request → Match → Cross-Match → Issue → Transfuse
- Verify BloodRequestCreatedEvent triggers matching
- Verify BloodRequestMatchedEvent triggers billing

M3-042: Emergency Request → O-Neg Issue → Transfusion → Reaction → Hemovigilance
- Verify emergency O-negative protocol bypasses normal matching
- Verify TransfusionReactionEvent triggers hemovigilance report

M3-043: Disaster Event → Mass Mobilization → Emergency Stock Rebalancing
- Verify disaster event triggers donor mobilization
- Verify EmergencyRequestEvent reaches all branches

Use Testcontainers (PostgreSQL 17, RabbitMQ 3.13) for integration tests.
These tests verify RabbitMQ event flow between services.
```

---

### PROMPT #6 → `@project-tracker`

```
M3 should now be complete. Review all files in:
- backend/hospital-service/src/main/java/
- backend/hospital-service/src/test/java/
- backend/request-matching-service/src/main/java/
- backend/request-matching-service/src/test/java/

Verify all M3 issues (M3-001 to M3-043) are complete.
Update docs/milestones/M3-clinical-services.md — check off completed items.
Update docs/milestones/STATUS-REPORT.md with current M3 status.
```

---

## PHASE 2: Finish M4 — Support Services (14 issues remaining)

### PROMPT #7 → `@backend-developer`

```
Implement compliance-service (M4-055 to M4-064).

Current state: Only Application.java exists. Port 8092.

What to implement:

1. APPLICATION CONFIG:
   - application.yml with port 8092, spring.flyway.enabled=false
   - RabbitMQ config

2. ENTITIES (all extend BranchScopedEntity with @FilterDef/@Filter):
   - RegulatoryFramework (table: regulatory_frameworks from V12__compliance_tables.sql)
     Columns: name, authority, version, effective_date, expiry_date, scope, status
   - SopDocument (table: sop_documents from V12)
     Columns: framework_id, title, document_number, version, effective_date,
     review_date, status, content_path, approved_by
   - License (table: licenses from V12)
     Columns: license_type, license_number, issuing_authority, issue_date,
     expiry_date, status, conditions
   - Deviation (table: deviations from V12)
     Columns: deviation_number, type, severity, description, root_cause,
     corrective_action, status, reported_by, reported_at, resolved_at
   - RecallRecord (table: recall_records from V12)
     Columns: recall_number, recall_type, severity, affected_units_count,
     reason, status, initiated_by, initiated_at, completed_at

3. ENUMS: FrameworkStatusEnum, SopStatusEnum, LicenseStatusEnum, LicenseTypeEnum,
   DeviationTypeEnum, DeviationSeverityEnum, DeviationStatusEnum,
   RecallTypeEnum, RecallStatusEnum

4. DTOs: Java 21 records for all 5 entities

5. REPOSITORIES: for all 5 entities

6. MAPPERS: MapStruct for all 5 entities

7. SERVICES:
   - ComplianceService — manage regulatory frameworks
   - SopService — SOP document lifecycle (draft, review, approve, retire)
   - LicenseService — license tracking with expiry alerts
   - DeviationService — deviation/CAPA tracking and resolution workflow
   - RecallService — recall management (initiate, investigate, resolve)

8. CONTROLLERS with @PreAuthorize:
   - ComplianceController (/api/v1/compliance/frameworks) — AUDITOR, SUPER_ADMIN
   - SopController (/api/v1/compliance/sops) — AUDITOR, BRANCH_ADMIN
   - LicenseController (/api/v1/compliance/licenses) — AUDITOR, BRANCH_ADMIN
   - DeviationController (/api/v1/compliance/deviations) — AUDITOR, BRANCH_MANAGER
   - RecallController (/api/v1/compliance/recalls) — AUDITOR, SUPER_ADMIN

9. RABBITMQ:
   - Publish RecallInitiatedEvent when a recall is created

Reference: backend/billing-service/ for similar service pattern
Reference: docs/architecture/event-contracts.md for RecallInitiatedEvent
```

---

### PROMPT #8 → `@test-writer`

```
Write tests for compliance-service AND document-service (M4-053, M4-054, M4-065, M4-066).

COMPLIANCE-SERVICE (new — all tests needed):
- 5 services: ComplianceService, SopService, LicenseService, DeviationService, RecallService
- 5 controllers: ComplianceController, SopController, LicenseController, DeviationController, RecallController
- Test roles: AUDITOR (read all), SUPER_ADMIN (frameworks/recalls), BRANCH_ADMIN (SOPs/licenses), BRANCH_MANAGER (deviations)
- DONOR and HOSPITAL_USER should get 403

Write unit tests (Mockito) for all services.
Write controller tests (@WebMvcTest) for all controllers.
Target: >80% JaCoCo coverage for both services.

Reference: backend/billing-service/src/test/java/ for test pattern
```

---

### PROMPT #9 → `@project-tracker`

```
M4 should now be complete. Review all files in:
- backend/compliance-service/src/main/java/
- backend/compliance-service/src/test/java/
- backend/document-service/src/test/java/

Verify all M4 issues (M4-001 to M4-066) are complete.
Update docs/milestones/M4-support-services.md — check off completed items.
Update docs/milestones/STATUS-REPORT.md with current M4 status.
```

---

## PHASE 3: Finish M5 — Frontend (37 issues remaining)

### PROMPT #10 → `@frontend-developer`

```
Scaffold Angular 21 project and implement core module (M5-016, M5-017, M5-018).

Create the project at: frontend/bloodbank-ui/
Follow: docs/ANGULAR_GUIDELINES.md exactly.

1. SCAFFOLD:
   ng new bloodbank-ui --standalone --style=scss --routing --ssr=false --zoneless

2. DEPENDENCIES:
   - Angular Material 21 (M3 theme)
   - Tailwind CSS 4 (with Preflight DISABLED)
   - keycloak-js + keycloak-angular
   - chart.js + ng2-charts
   - date-fns

3. CORE MODULE (src/app/core/):
   - auth/auth.service.ts — Keycloak wrapper using KeycloakService
   - auth/auth.init.ts — APP_INITIALIZER for Keycloak (login-required, in-memory tokens)
   - auth/auth.interceptor.ts — JWT Bearer token injection via HttpInterceptor
   - guards/role.guard.ts — canActivate guard checking user roles from Keycloak
   - guards/branch.guard.ts — branch context guard
   - interceptors/error.interceptor.ts — global HTTP error handler
   - interceptors/branch.interceptor.ts — inject X-Branch-Id header
   - services/notification.service.ts — MatSnackBar wrapper
   - services/branch-context.service.ts — branch selection state (Signals)
   - services/theme.service.ts — dark/light mode toggle
   - models/user.model.ts — User interface
   - models/role.enum.ts — all 16 roles

4. TSCONFIG PATH ALIASES:
   @core/*, @shared/*, @features/*, @env/*, @models/*

5. ENVIRONMENTS:
   - environment.ts, environment.development.ts, environment.production.ts
   - apiUrl, keycloakUrl, keycloakRealm, keycloakClientId

6. STYLES:
   - styles/_variables.scss — design tokens
   - styles/_material-theme.scss — M3 custom theme (red primary, blue secondary, green tertiary)
   - styles/_tailwind.scss — Tailwind imports
   - styles/_typography.scss — Inter font stack
   - styles/_healthcare-colors.scss — blood group + clinical status CSS custom properties
   - styles/styles.scss — global entry point

7. TAILWIND CONFIG:
   - Preflight: false
   - Healthcare custom colors (bloodbank.primary, secondary, success, warning, danger, info)
```

---

### PROMPT #11 → `@frontend-developer`

```
Create shared components and layout (M5-019, M5-020, M5-021, M5-022, M5-023, M5-024).

All in frontend/bloodbank-ui/src/app/shared/

1. SHARED COMPONENTS (src/app/shared/components/):
   - data-table/ — reusable wrapper around mat-table with mat-sort and mat-paginator
   - search-bar/ — mat-form-field with search icon and debounced input
   - status-badge/ — colored badge per status (AVAILABLE, RESERVED, QUARANTINE, etc.)
   - blood-group-badge/ — circular badge with blood group color
   - confirm-dialog/ — mat-dialog with title, message, confirm/cancel
   - loading-skeleton/ — animated pulse placeholder rows
   - empty-state/ — icon + message + optional action button
   - error-card/ — error display with retry button
   - form-field/ — standard mat-form-field wrapper with error display

2. SHARED LAYOUT (src/app/shared/layout/):
   - shell/ — main layout with sidenav + topbar + breadcrumb + router-outlet
   - sidenav/ — role-filtered navigation menu (use navItems from Guidelines Section 9)
   - topbar/ — branch selector dropdown, notification bell, user menu
   - breadcrumb/ — auto-generated from route data
   - footer/ — copyright, version

3. SHARED PIPES (src/app/shared/pipes/):
   - blood-group.pipe.ts — A_POSITIVE → "A+"
   - date-ago.pipe.ts — relative time using date-fns
   - truncate.pipe.ts — truncate long text with ellipsis

4. SHARED DIRECTIVES:
   - has-role.directive.ts — structural directive to show/hide by role
   - auto-focus.directive.ts — auto-focus input on init

5. SHARED MODELS (src/app/shared/models/):
   - api-response.model.ts — ApiResponse<T> interface
   - paged-response.model.ts — PagedResponse<T> interface
   - branch.model.ts — Branch interface

6. APP ROUTES (src/app/app.routes.ts):
   - /staff/* routes with roleGuard (12 staff roles)
   - /hospital/* routes with roleGuard (HOSPITAL_USER)
   - /donor/* routes with roleGuard (DONOR)
   - All lazy-loaded with loadComponent/loadChildren

7. DESIGN SYSTEM:
   - Material M3 theme configured in _material-theme.scss
   - Tailwind for layout ONLY (never override Material)

Every component: standalone: true, ChangeDetectionStrategy.OnPush, inject() for DI,
signal inputs/outputs, @if/@for/@switch control flow.
```

---

### PROMPT #12 → `@frontend-developer`

```
Create Dashboard feature module (M5-025).

Path: frontend/bloodbank-ui/src/app/features/dashboard/
Route: /staff/dashboard (lazy loaded)
Roles: all authenticated staff roles (*)

Components:
1. dashboard.routes.ts — lazy loaded from app.routes.ts
2. DashboardComponent — container with role-specific KPI cards and charts
3. KpiCardComponent — reusable card showing label, value, trend, icon
   (signal inputs: label, value, trend, icon, colorClass)

KPI Cards (data from API):
- Total Donors (today/month)
- Collections Today
- Available Blood Units by group
- Pending Test Orders
- Expiring Units (next 7 days)
- Open Hospital Requests

Charts (ng2-charts):
- Bar chart: Blood stock levels by group (8 groups × available/reserved)
- Doughnut chart: Collection status breakdown
- Line chart: Donation trends (last 30 days)

Service:
- DashboardService — GET /api/v1/dashboard/kpis, GET /api/v1/dashboard/stock-levels,
  GET /api/v1/dashboard/collection-stats, GET /api/v1/dashboard/donation-trends

Layout: grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 for KPIs,
grid grid-cols-1 lg:grid-cols-2 gap-6 for charts.
```

---

### PROMPT #13 → `@frontend-developer`

```
Create Donor feature module (M5-026).

Path: frontend/bloodbank-ui/src/app/features/donor/
Route: /staff/donors (lazy loaded)
Roles: RECEPTIONIST, PHLEBOTOMIST, BRANCH_ADMIN, BRANCH_MANAGER

Components:
1. donor.routes.ts with child routes: list, :id, :id/edit, new
2. DonorListComponent — mat-table with columns: name, bloodGroup (badge), phone,
   status (badge), lastDonation, actions. mat-sort, mat-paginator, search bar.
3. DonorDetailComponent — donor profile card, health records, donation history,
   deferrals, consents, loyalty points
4. DonorFormComponent — register/edit form with reactive forms (NonNullableFormBuilder)
   Fields: firstName, lastName, bloodGroup (mat-select), email, phone, dateOfBirth
   (mat-datepicker), gender, address (nested group), consent checkbox
5. DonorSearchComponent — advanced search with bloodGroup, status, dateRange filters

Service:
- DonorService — CRUD: GET/POST/PUT /api/v1/donors, GET /api/v1/donors/{id},
  GET /api/v1/donors/search

Backend API is live on main (donor-service port 8082).
```

---

### PROMPT #14 → `@frontend-developer`

```
Create Collection and Camp feature modules (M5-027, M5-032).

COLLECTION MODULE:
Path: features/collection/
Route: /staff/collections
Roles: PHLEBOTOMIST

Components:
1. CollectionListComponent — table of today's collections with status badges
2. CollectionFormComponent — record blood collection (donor lookup, vitals,
   bag number, volume, start/end time)
3. AdverseReactionFormComponent — report adverse reaction during collection
4. SampleRegistrationComponent — register samples from collection

Service: CollectionService — /api/v1/collections

CAMP MODULE:
Path: features/camp/
Route: /staff/camps
Roles: CAMP_COORDINATOR

Components:
1. CampListComponent — table of camps with status (PLANNED, ACTIVE, COMPLETED)
2. CampDetailComponent — camp info, resources, registered donors, collections
3. CampFormComponent — plan new camp (name, location, date, target donors)
4. CampDonorRegistrationComponent — register walk-in donors at camp

Service: CampService — /api/v1/camps
```

---

### PROMPT #15 → `@frontend-developer`

```
Create Lab feature module (M5-028).

Path: features/lab/
Route: /staff/lab
Roles: LAB_TECHNICIAN

Components:
1. TestOrderListComponent — table of pending/in-progress test orders with priority badges
2. TestResultFormComponent — enter test results (HIV, HBV, HCV, Syphilis, Malaria,
   Blood Grouping, Antibody Screening). Include dual-review workflow:
   first tech enters result, second tech confirms.
3. QcDashboardComponent — quality control records, instrument status, Levey-Jennings chart
4. InstrumentListComponent — lab instrument management with calibration status

Service: LabService — /api/v1/test-orders, /api/v1/test-results, /api/v1/qc, /api/v1/instruments
```

---

### PROMPT #16 → `@frontend-developer`

```
Create Inventory feature module (M5-029).

Path: features/inventory/
Route: /staff/inventory
Roles: INVENTORY_MANAGER

Components:
1. StockDashboardComponent — blood stock levels by group × component type,
   color-coded (green=adequate, yellow=low, red=critical). Bar chart visualization.
2. BloodUnitListComponent — table of blood units with status badges,
   filters by blood group, component type, status, expiry date range
3. ComponentProcessingComponent — process whole blood into components
4. StorageManagementComponent — storage locations, temperature monitoring,
   capacity utilization
5. TransferFormComponent — initiate inter-branch stock transfer
6. DisposalFormComponent — dispose expired/rejected units with reason

Services:
- InventoryService — /api/v1/blood-units, /api/v1/components, /api/v1/stock
- TransferService — /api/v1/transfers
- LogisticsService — /api/v1/logistics
```

---

### PROMPT #17 → `@frontend-developer`

```
Create Transfusion and Emergency feature modules (M5-030, M5-038).

TRANSFUSION MODULE:
Path: features/transfusion/
Route: /staff/transfusions
Roles: DOCTOR, NURSE

Components:
1. CrossMatchRequestComponent — request cross-match (patient, blood group, units needed)
2. CrossMatchResultComponent — view compatibility results
3. BloodIssueComponent — issue blood units to patient
4. TransfusionRecordComponent — record transfusion (start/end, volume, outcome)
5. ReactionReportComponent — report transfusion reaction (type, severity, actions taken)
6. HemovigilanceListComponent — hemovigilance reports and lookback investigations

Service: TransfusionService — /api/v1/crossmatch, /api/v1/blood-issues,
/api/v1/transfusions, /api/v1/hemovigilance

EMERGENCY MODULE:
Path: features/emergency/
Route: /staff/emergency
Roles: DOCTOR, BRANCH_MANAGER

Components:
1. EmergencyRequestComponent — create emergency blood request with priority level
2. EmergencyDashboardComponent — active emergencies, matched units, pending responses
3. DisasterResponseComponent — mass casualty management (SUPER_ADMIN, REGIONAL_ADMIN only)

Service: EmergencyService — /api/v1/emergencies, /api/v1/matching, /api/v1/disasters
```

---

### PROMPT #18 → `@frontend-developer`

```
Create Branch, User Management, and Settings feature modules (M5-031, M5-039, M5-040).

BRANCH MODULE:
Path: features/branch/
Route: /staff/branches
Roles: BRANCH_ADMIN, SUPER_ADMIN

Components:
1. BranchListComponent — table of branches with status
2. BranchDetailComponent — operating hours, equipment, coverage regions
3. BranchFormComponent — create/edit branch
4. MasterDataComponent — manage blood groups, component types, deferral reasons,
   reaction types (SUPER_ADMIN only)

Service: BranchService — /api/v1/branches, /api/v1/master-data/*

USER MANAGEMENT MODULE:
Path: features/user-management/
Route: /staff/users
Roles: BRANCH_ADMIN, SUPER_ADMIN

Components:
1. UserListComponent — staff list with role badges
2. UserFormComponent — assign roles, assign to branch
3. UserActivityComponent — activity logs

Service: UserService — Keycloak Admin REST API via backend proxy

SETTINGS MODULE:
Path: features/settings/
Route: /staff/settings
Roles: BRANCH_ADMIN, SUPER_ADMIN

Components:
1. SystemSettingsComponent — key-value system settings
2. FeatureFlagsComponent — toggle feature flags
```

---

### PROMPT #19 → `@frontend-developer`

```
Create Billing, Notification, Reporting, Document, and Compliance feature modules
(M5-033, M5-034, M5-035, M5-036, M5-037).

BILLING (features/billing/) — /staff/billing — BILLING_CLERK, BRANCH_MANAGER:
1. InvoiceListComponent — invoices table with status, amount, hospital
2. InvoiceDetailComponent — line items, payments, credit notes
3. PaymentFormComponent — record payment
4. RateManagementComponent — manage pricing (BRANCH_ADMIN)
Service: BillingService — /api/v1/invoices, /api/v1/payments, /api/v1/rates

COMPLIANCE (features/compliance/) — /staff/compliance — AUDITOR:
1. FrameworkListComponent — regulatory frameworks
2. SopListComponent — SOP documents with version status
3. LicenseListComponent — licenses with expiry alerts
4. DeviationListComponent — deviations/CAPAs with status workflow
5. RecallListComponent — recall records with status tracking
Service: ComplianceService — /api/v1/compliance/*

NOTIFICATION (features/notification/) — /staff/notifications — BRANCH_ADMIN:
1. NotificationListComponent — sent notifications
2. TemplateManagementComponent — email/SMS templates
3. CampaignComponent — create notification campaigns
4. PreferenceComponent — per-user opt-out settings
Service: NotificationService — /api/v1/notifications, /api/v1/templates, /api/v1/campaigns

REPORTING (features/reporting/) — /staff/reports — AUDITOR, REGIONAL_ADMIN:
1. AuditLogComponent — immutable audit trail table (read-only)
2. ReportBuilderComponent — custom report builder with filters
3. ChainOfCustodyComponent — vein-to-vein blood unit tracking
4. ScheduledReportsComponent — manage scheduled report generation
Service: ReportingService — /api/v1/audit-logs, /api/v1/reports, /api/v1/chain-of-custody

DOCUMENT (features/document/) — /staff/documents — all authenticated:
1. DocumentBrowserComponent — file browser with folders
2. DocumentUploadComponent — drag-drop upload with metadata
3. DocumentVersionComponent — version history
Service: DocumentService — /api/v1/documents
```

---

### PROMPT #20 → `@frontend-developer`

```
Create Hospital Portal (M5-041 to M5-045).

Route prefix: /hospital/*
Role: HOSPITAL_USER only
All routes guarded with roleGuard, data: { roles: ['HOSPITAL_USER'] }

Components (features/hospital-portal/):
1. hospital-portal.routes.ts — lazy loaded
2. HospitalDashboardComponent — overview: active requests, contract status,
   recent deliveries, pending feedback
3. BloodRequestFormComponent — submit new blood request
   (blood group, component, quantity, priority, required date, clinical notes)
4. RequestTrackingComponent — table of all requests with real-time status updates
   (SUBMITTED → MATCHED → ISSUED → DELIVERED)
5. ContractViewComponent — view hospital contract details and SLA metrics
6. FeedbackFormComponent — submit feedback on fulfilled requests (rating 1-5, text)

Service: HospitalPortalService — /api/v1/hospital-requests, /api/v1/hospitals/{id}/contracts,
/api/v1/hospital-feedback

Layout: same shell component but with hospital-specific sidenav
(Dashboard, Request Blood, My Requests, Contract, Feedback)
```

---

### PROMPT #21 → `@frontend-developer`

```
Create Donor Portal (M5-046 to M5-052).

Route prefix: /donor/*
Role: DONOR only
All routes guarded with roleGuard, data: { roles: ['DONOR'] }

Components (features/donor-portal/):
1. donor-portal.routes.ts — lazy loaded
2. DonorDashboardComponent — next eligible date, total donations, loyalty tier,
   nearby camps, upcoming appointments
3. DonorSelfRegistrationComponent — public registration form
   (first/last name, blood group, email, phone, DOB, address, consent)
4. DonationHistoryComponent — table of past donations with dates, locations, volumes
5. AppointmentBookingComponent — select branch, date, time slot, confirm
6. EligibilityCheckComponent — self-service eligibility quiz
   (weight, hemoglobin, recent travel, medications, last donation date)
7. NearbyMaCampFinderComponent — list of upcoming blood camps with map/location
8. DigitalDonorCardComponent — donor card with QR code, blood group badge,
   name, donor ID, total donations, loyalty tier
9. ReferralComponent — refer a friend (generate referral link, track referrals)

Service: DonorPortalService — /api/v1/donors/me, /api/v1/donors/me/donations,
/api/v1/donors/me/appointments, /api/v1/donors/me/eligibility, /api/v1/camps/nearby

Layout: donor-specific sidenav (Dashboard, My History, Book Appointment,
Check Eligibility, Find Camps, My Card, Refer a Friend)
```

---

### PROMPT #22 → `@project-tracker`

```
M5 frontend should now be complete. Review:
- frontend/bloodbank-ui/src/app/core/
- frontend/bloodbank-ui/src/app/shared/
- frontend/bloodbank-ui/src/app/features/ (all 17 feature directories)

Verify all M5 issues (M5-001 to M5-052) are complete.
Update docs/milestones/M5-gateway-frontend.md.
Update docs/milestones/STATUS-REPORT.md.
Report: which features are complete, which have missing components.
```

---

## PHASE 4: M6 — Integration + Security Testing (30 issues)

### PROMPT #23 → `@test-writer`

```
Write full lifecycle E2E integration tests (M6-001 to M6-009).

Create in: backend/integration-tests/src/test/java/

Use Testcontainers (PostgreSQL 17, RabbitMQ 3.13, Redis 7).
Use @SpringBootTest with RANDOM_PORT.

Tests:
1. M6-001: Donor Registration → Eligibility → Collection → Lab Testing →
   Component Processing → Inventory (verify DonationCompletedEvent →
   TestResultAvailableEvent → UnitReleasedEvent → BloodStockUpdatedEvent)
2. M6-002: Hospital Request → Match → Cross-Match → Issue → Transfusion → Outcome
3. M6-003: Camp Planning → Registration → Collection → Post-Camp Follow-Up
4. M6-004: Test Result → Quarantine → Release → Stock Update
5. M6-005: Emergency → O-Neg Issue → Transfusion → Reaction → Hemovigilance
6. M6-006: Recall → Notification → Investigation → Resolution
7. M6-007: Invoice Generation → Payment → Credit Note
8. M6-008: Inter-Branch Transfer → Cold Chain → Delivery Confirmation
9. M6-009: Donor Portal self-registration → Appointment → Digital Card
```

---

### PROMPT #24 → `@test-writer`

```
Write cross-service event flow tests and RabbitMQ tests (M6-010 to M6-012).

M6-010: Verify all 15 RabbitMQ events flow correctly:
- DonationCompletedEvent: donor-service → lab-service, inventory-service
- TestResultAvailableEvent: lab-service → inventory-service
- UnitReleasedEvent: lab-service → inventory-service
- BloodStockUpdatedEvent: inventory-service → request-matching-service
- BloodRequestCreatedEvent: hospital-service → request-matching-service, billing-service
- BloodRequestMatchedEvent: request-matching-service → billing-service, notification-service
- TransfusionCompletedEvent: transfusion-service → notification-service, reporting-service
- TransfusionReactionEvent: transfusion-service → notification-service, reporting-service
- InvoiceGeneratedEvent: billing-service → notification-service
- RecallInitiatedEvent: compliance-service → notification-service
- CampCompletedEvent: donor-service → notification-service, reporting-service
- EmergencyRequestEvent: request-matching-service → notification-service, donor-service
- UnitExpiringEvent: inventory-service → notification-service
- StockCriticalEvent: inventory-service → notification-service

M6-011: Dead letter queue test — publish malformed event, verify 3 retries then DLQ
M6-012: Idempotency test — publish same event twice, verify only processed once
```

---

### PROMPT #25 → `@security-reviewer`

```
Perform full security audit (M6-013 to M6-025).

Create security test files in: backend/integration-tests/src/test/java/security/

M6-013: Test all 16 roles × all endpoints — verify correct 200/403 responses
M6-014: Test branch isolation — user in Branch A cannot see Branch B data
M6-015: Test break-glass — DOCTOR emergency override bypasses normal flow
M6-016: Test dual authorization — test result release requires 2 different users
M6-017: Generate OWASP ZAP scan config for all API endpoints
M6-018: Test JWT expiration handling (expired token → 401)
M6-019: Verify CSRF not needed (JWT Bearer tokens)
M6-020: Test SQL injection on all search endpoints
M6-021: Test XSS on all text input fields
M6-022: Test role escalation — user cannot promote themselves
M6-023: Test PII masking — unauthorized roles see masked data
M6-024: Test GDPR erasure — donor anonymization removes PII
M6-025: Test audit_logs immutability — attempt UPDATE/DELETE → blocked by trigger

Reference: docs/security/rbac-matrix.md for expected role access
Reference: docs/security/branch-isolation.md for 4-layer isolation
```

---

### PROMPT #26 → `@test-writer`

```
Write API contract validation tests (M6-026 to M6-030).

M6-026: Validate all endpoints return correct response structure
        (ApiResponse<T> with success, data, error fields)
M6-027: Verify all endpoints use /api/v1/ prefix
M6-028: Verify error responses follow RFC 7807 Problem Details format
        (type, title, status, detail, instance fields)
M6-029: Verify paginated endpoints return PagedResponse<T>
        (content, page, size, totalElements, totalPages)
M6-030: Test rate limiting — exceed 100 req/sec → 429 Too Many Requests

Create in: backend/integration-tests/src/test/java/contracts/
```

---

### PROMPT #27 → `@project-tracker`

```
M6 should now be complete. Review all test files in:
- backend/integration-tests/src/test/java/

Update docs/milestones/M6-integration-security.md.
Update docs/milestones/STATUS-REPORT.md.
```

---

## PHASE 5: M7 — Infrastructure (46 issues — CAN RUN IN PARALLEL)

> **Note:** M7 prompts can be started as early as after M2 is complete.
> They are independent of M3-M6. You can run these in parallel.

### PROMPT #28 → `@devops-engineer`

```
Create Dockerfiles for all 14 backend services and the Angular frontend (M7-001 to M7-004).

For each of these 14 services, create backend/{service}/Dockerfile:
- branch-service (8081)
- donor-service (8082)
- lab-service (8083)
- inventory-service (8084)
- transfusion-service (8085)
- hospital-service (8086)
- request-matching-service (8087)
- billing-service (8088)
- notification-service (8089)
- reporting-service (8090)
- document-service (8091)
- compliance-service (8092)
- api-gateway (8080)
- config-server (8888)

Pattern: multi-stage (gradle:8-jdk21 build → eclipse-temurin:21-jre-alpine run)
Include: non-root user, HEALTHCHECK, .dockerignore, minimal layers

For Angular frontend: frontend/bloodbank-ui/Dockerfile
Pattern: multi-stage (node:22-alpine build → nginx:alpine-slim serve)
Include: custom nginx.conf for SPA routing, non-root user

Also create .dockerignore files for each service.
```

---

### PROMPT #29 → `@devops-engineer`

```
Update docker-compose.yml and test full stack (M7-005, M7-006).

Update the existing docker-compose.yml to include ALL 14 backend services + frontend.
Keep existing infrastructure services (PostgreSQL, Redis, RabbitMQ, Keycloak, MinIO, Mailhog).

Add all 14 backend services with:
- Build context pointing to root (for shared-libs access)
- Dockerfile path for each service
- Correct port mapping
- Depends_on: postgres, redis, rabbitmq, keycloak, config-server
- Environment variables for DB, Redis, RabbitMQ, Keycloak URLs
- Health checks

Add frontend service:
- Build from frontend/bloodbank-ui/Dockerfile
- Port 4200:80
- Depends_on: api-gateway

Add a docker-compose.override.yml for dev (volume mounts, debug ports).
```

---

### PROMPT #30 → `@devops-engineer`

```
Create Kubernetes manifests (M7-007 to M7-018).

Create in k8s/ directory:

1. NAMESPACES (k8s/namespaces/):
   - bloodbank-dev.yml, bloodbank-staging.yml, bloodbank-uat.yml, bloodbank-prod.yml

2. DEPLOYMENTS (k8s/deployments/):
   One Deployment per service (14 services + frontend = 15 files)
   Include: 2 replicas for critical services, resource requests/limits,
   readiness probe (HTTP /actuator/health), liveness probe,
   env from ConfigMap + Secret references

3. SERVICES (k8s/services/):
   ClusterIP for all internal services
   LoadBalancer for api-gateway

4. INGRESS (k8s/ingress/):
   NGINX Ingress for api-gateway with TLS
   Path: / → frontend, /api/* → api-gateway

5. CONFIGMAPS (k8s/configmaps/):
   Per-service config (spring profiles, app-specific settings)
   Shared config (database URL, Redis URL, RabbitMQ URL, Keycloak URL)

6. SECRETS (k8s/secrets/):
   Database credentials, Redis password, RabbitMQ credentials,
   Keycloak admin password, JWT signing key, ENCRYPT_KEY

7. HPA (k8s/hpa/):
   donor-service, inventory-service, api-gateway: min 2, max 10, CPU target 70%

8. STATEFULSETS (k8s/statefulsets/):
   PostgreSQL 17, Redis 7, RabbitMQ 3.13 (with PersistentVolumeClaims)

9. JOBS (k8s/jobs/):
   Flyway migration job — runs BEFORE any service deployment
   Uses shared-libs/db-migration Docker image

10. RESOURCE LIMITS:
    Regular services: 256Mi-512Mi request, 512Mi-1Gi limit, 200m-500m CPU
    api-gateway: 512Mi request, 1Gi limit, 500m-1000m CPU
    PostgreSQL: 1Gi request, 2Gi limit
```

---

### PROMPT #31 → `@devops-engineer`

```
Create Jenkins CI/CD pipeline (M7-019 to M7-031).

Create Jenkinsfile at project root with 11 stages:

Stage 1: Checkout — git checkout with credentials
Stage 2: Gradle Build — ./gradlew build -x test (parallel module builds)
Stage 3: Unit Tests + Coverage — ./gradlew test jacocoTestReport,
         fail if JaCoCo < 80%, publish test results
Stage 4: SonarQube — sonar-scanner analysis, quality gate check
Stage 5: Security Scan — Trivy image scan, OWASP dependency-check,
         fail on CRITICAL/HIGH vulnerabilities
Stage 6: Docker Build & Push — build all 15 images, tag with
         GIT_COMMIT_SHA + semver, push to registry
Stage 7: Flyway Migration — kubectl apply flyway-migration-job,
         wait for completion
Stage 8: Deploy DEV — kubectl apply -f k8s/ -n bloodbank-dev,
         wait for rollout, run smoke tests
Stage 9: Integration Tests — run backend/integration-tests against DEV
Stage 10: Deploy STAGING — same as DEV but bloodbank-staging namespace
Stage 11: Deploy PRODUCTION — input(message: 'Approve production?'),
          apply deployment strategy per service (Blue-Green/Canary/Rolling)

Include: Slack notifications, artifact archiving, parallel stages where possible.
Configure per-service deployment strategies:
- Blue-Green: donor, inventory, lab, transfusion, api-gateway, frontend
- Canary (10%→50%→100%): request-matching, billing
- Rolling: all others
```

---

### PROMPT #32 → `@devops-engineer`

```
Create Keycloak configuration (M7-032 to M7-040).

Create in keycloak/ directory:

1. realm-export.json — bloodbank realm with:
   - 2 clients: bloodbank-api (confidential, service account),
     bloodbank-ui (public, PKCE, redirect to localhost:4200/*)
   - 4 realm roles: SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR
   - 12 client roles on bloodbank-api: BRANCH_ADMIN, BRANCH_MANAGER, DOCTOR,
     LAB_TECHNICIAN, PHLEBOTOMIST, NURSE, INVENTORY_MANAGER, BILLING_CLERK,
     CAMP_COORDINATOR, RECEPTIONIST, HOSPITAL_USER, DONOR
   - Group hierarchy: /global, /regions/{region}, /regions/{region}/{branch},
     /hospitals/{hospital}
   - Custom JWT mapper: add branch_id claim from user attribute
   - Token settings: access token 5min, refresh token 30min

2. LDAP federation config — READ_ONLY, LDAPS port 636, user sync every 15min

3. MFA policies:
   - Required for: SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR, BRANCH_ADMIN
   - Optional for: DOCTOR, LAB_TECHNICIAN, NURSE
   - Not required for: DONOR, HOSPITAL_USER

4. Session policies:
   - Admin roles: idle 15min, max 8 hours
   - Clinical roles: idle 30min, max 12 hours
   - Portal users: idle 60min, max 24 hours

5. Password policies: 12+ chars, 1 uppercase, 1 lowercase, 1 digit,
   1 special char, history 5, expire 90 days

6. Test users — one per role (16 users) with pre-set passwords for dev
```

---

### PROMPT #33 → `@devops-engineer`

```
Create monitoring and observability stack (M7-041 to M7-046).

Create in monitoring/ directory:

1. PROMETHEUS (monitoring/prometheus/):
   - prometheus.yml — scrape configs for all 14 services (/actuator/prometheus)
   - alert-rules.yml — rules for: service down >1min, error rate >5%,
     P95 latency >500ms, stock critical, expiring units, disk >80%

2. GRAFANA (monitoring/grafana/):
   - provisioning/datasources/ — Prometheus, Loki, Tempo datasources
   - provisioning/dashboards/ — auto-provisioned dashboards:
     a. Service Health — up/down status, request rate, error rate, latency P50/P95/P99
     b. JVM Metrics — heap, GC, threads per service
     c. API Performance — latency by endpoint, error codes, throughput
     d. Business Metrics — donations/day, stock levels, active emergencies
     e. Infrastructure — PostgreSQL connections, Redis hit rate, RabbitMQ queue depth

3. LOKI (monitoring/loki/):
   - loki-config.yml — log aggregation config
   - promtail-config.yml — log collection from all services (parse JSON logs)

4. TEMPO (monitoring/tempo/):
   - tempo-config.yml — distributed trace collection
   - Configure Spring Boot to send traces via OTLP

5. ALERTMANAGER (monitoring/alertmanager/):
   - alertmanager.yml — routes: critical → PagerDuty/SMS, warning → Slack, info → email
   - Templates for alert notifications

6. SRE DASHBOARD:
   - SLO tracking: availability >99.9%, latency P95 <200ms, error rate <0.1%
   - Error budget visualization
```

---

### PROMPT #34 → `@project-tracker`

```
M7 should now be complete. Review all files in:
- backend/*/Dockerfile
- docker-compose.yml
- k8s/
- Jenkinsfile
- keycloak/
- monitoring/

Verify all M7 issues (M7-001 to M7-046) are complete.
Update docs/milestones/M7-infrastructure.md.
Update docs/milestones/STATUS-REPORT.md.
```

---

## PHASE 6: M8 — Performance Testing (28 issues)

### PROMPT #35 → `@devops-engineer`

```
Set up performance test framework and create load tests (M8-001 to M8-016).

Create in performance-tests/ directory:

1. FRAMEWORK SETUP (M8-001):
   - Use k6 (JavaScript-based load testing)
   - package.json with k6 dependency
   - k6.config.js — base configuration

2. TEST DATA GENERATORS (M8-002, M8-003):
   - generators/donors.js — generate realistic donor data
   - generators/blood-units.js — generate blood unit data
   - generators/hospitals.js — generate hospital data
   - seed-database.js — seed 100K donors, 500K blood units, 50 hospitals

3. LOAD TESTS (M8-005 to M8-010):
   - tests/donor-registration.js — 100 concurrent registrations/sec
   - tests/blood-request.js — 50 concurrent requests/sec
   - tests/inventory-search.js — 200 concurrent queries/sec
   - tests/dashboard-load.js — 500 concurrent users
   - tests/report-generation.js — 20 concurrent large reports
   - tests/mixed-workload.js — 1000 concurrent users across all endpoints

4. STRESS TESTS (M8-011 to M8-014):
   - tests/stress-ramp.js — gradual ramp to 2000 users
   - tests/stress-spike.js — spike to 5000 users for 60 seconds
   - tests/stress-failure.js — kill one service, observe degradation
   - tests/stress-connection-pool.js — exhaust DB connection pool

5. ENDURANCE TESTS (M8-015, M8-016):
   - tests/endurance-4hr.js — 500 users sustained for 4 hours
   - Monitor: memory, connections, threads (via Prometheus during test)

Thresholds in all tests:
- P95 response time < 200ms
- P99 response time < 500ms
- Error rate < 1%
- Throughput > 500 req/sec
```

---

### PROMPT #36 → `@backend-developer`

```
Implement performance optimizations (M8-017 to M8-023).

Review and optimize across all services:

1. DATABASE (M8-017, M8-022, M8-023):
   - Review slow queries in each service's repository
   - Add @QueryHint for read-only queries
   - Ensure all list endpoints use Spring Data pagination (never findAll())
   - Add missing composite indexes for common query patterns

2. REDIS CACHING (M8-018):
   - Review cache TTLs — master data 24h, stock levels 5min, user sessions 30min
   - Add @Cacheable to frequently accessed read-only endpoints
   - Add @CacheEvict on write operations

3. JVM TUNING (M8-019):
   - Add JVM flags to all Dockerfiles: -XX:+UseZGC -XX:MaxRAMPercentage=75
   - Configure virtual threads for I/O-bound operations

4. HIKARI TUNING (M8-020):
   - Set minimum-idle: 5, maximum-pool-size: 20
   - connection-timeout: 30000, idle-timeout: 600000
   - leak-detection-threshold: 60000

5. K8S TUNING (M8-021):
   - Update resource limits based on observed usage patterns
   - Tune HPA thresholds

Only modify application.yml configs, Dockerfiles, and add @Cacheable/@QueryHint
annotations. Do not restructure services.
```

---

### PROMPT #37 → `@project-tracker`

```
M8 should now be complete. Review:
- performance-tests/ directory
- Any optimization changes in backend services

Update docs/milestones/M8-performance-testing.md.
Update docs/milestones/STATUS-REPORT.md.
```

---

## PHASE 7: M9 — UAT + Compliance (40 issues)

### PROMPT #38 → `@docs-writer`

```
Create UAT preparation documents (M9-001 to M9-005).

Create in docs/uat/ directory:

1. docs/uat/README.md — UAT overview, timeline, process

2. docs/uat/test-scripts/ — one file per role:
   - uat-super-admin.md
   - uat-regional-admin.md
   - uat-branch-admin.md
   - uat-branch-manager.md
   - uat-receptionist.md
   - uat-phlebotomist.md
   - uat-lab-technician.md
   - uat-inventory-manager.md
   - uat-doctor.md
   - uat-nurse.md
   - uat-billing-clerk.md
   - uat-camp-coordinator.md
   - uat-hospital-user.md
   - uat-donor.md
   - uat-auditor.md
   - uat-system-admin.md

   Each file has numbered test scenarios specific to that role's workflows.
   Format: Step | Action | Expected Result | Pass/Fail

3. docs/uat/uat-tracking.md — tracking template (scenario, tester, status, defect ID)

4. docs/uat/keycloak-test-users.md — 16 test accounts (username, role, branch, password)
```

---

### PROMPT #39 → `@docs-writer`

```
Create compliance validation documents (M9-022 to M9-032).

Create in docs/compliance/ directory:

1. docs/compliance/hipaa-validation.md
   - PHI protection checklist (encryption at rest/transit, access controls)
   - Audit trail coverage verification (all PHI access logged)

2. docs/compliance/gdpr-validation.md
   - Consent management workflow verification
   - Data erasure/anonymization procedure
   - Data portability export verification

3. docs/compliance/fda-21cfr11-validation.md
   - Electronic signatures verification
   - Immutable audit trail verification (audit_logs trigger)

4. docs/compliance/aabb-validation.md
   - Vein-to-vein traceability verification (donor → recipient)
   - Chain of custody logging verification

5. docs/compliance/who-validation.md
   - Mandatory test panel enforcement verification
   - Blood safety protocol compliance

6. docs/compliance/accessibility-report.md
   - WCAG 2.1 AA checklist
   - Keyboard navigation checklist
   - Screen reader compatibility checklist
   - Color contrast requirements
```

---

### PROMPT #40 → `@project-tracker`

```
Update status for M9. Review:
- docs/uat/ directory
- docs/compliance/ directory

Update docs/milestones/M9-uat-compliance.md.
Update docs/milestones/STATUS-REPORT.md.
Note: M9-006 to M9-021 (UAT execution) and M9-033 to M9-040 (accessibility + defects)
are manual processes. Mark them as READY but not automatable.
```

---

## PHASE 8: M10 — Pilot Deployment (27 issues)

### PROMPT #41 → `@devops-engineer`

```
Create production deployment manifests and procedures (M10-003 to M10-006, M10-009 to M10-015).

1. K8S PRODUCTION NAMESPACE (k8s/production/):
   - namespace-prod.yml
   - All deployments with production resource limits (higher than dev)
   - 3 replicas minimum for critical services
   - Production secrets with placeholder references (to be filled manually)
   - Production ingress with real domain and TLS certificate

2. FLYWAY PRODUCTION JOB:
   - k8s/production/flyway-migration-job.yml — runs V1-V20 against production DB

3. ROLLBACK PROCEDURES:
   - docs/operations/rollback-plan.md — step-by-step rollback for each service
   - k8s/scripts/rollback.sh — kubectl rollout undo commands

4. PRODUCTION MONITORING:
   - k8s/production/monitoring/ — production-specific Prometheus rules
   - Higher alert thresholds for production
   - PagerDuty integration for critical alerts

5. PRODUCTION HEALTH VERIFICATION:
   - k8s/scripts/verify-health.sh — check all service health endpoints
   - k8s/scripts/smoke-test.sh — basic API smoke tests post-deployment
```

---

### PROMPT #42 → `@docs-writer`

```
Create pilot deployment documentation (M10-001, M10-002, M10-007, M10-008).

1. docs/operations/pilot-plan.md
   - Pilot branch selection criteria
   - Data migration checklist
   - Staff training schedule (2 days per role group)
   - Go/no-go decision criteria
   - Communication plan

2. docs/operations/data-migration-guide.md
   - Historical donor data migration steps
   - Inventory data migration steps
   - Data validation queries
   - Rollback procedure for data migration

3. docs/operations/user-guides/ — one per role group:
   - quick-ref-reception.md (Receptionist, Phlebotomist)
   - quick-ref-lab.md (Lab Technician)
   - quick-ref-inventory.md (Inventory Manager)
   - quick-ref-clinical.md (Doctor, Nurse)
   - quick-ref-admin.md (Branch Admin, Branch Manager)
   - quick-ref-hospital.md (Hospital User)
   - quick-ref-donor.md (Donor)

4. docs/operations/hypercare-plan.md
   - 24/7 monitoring plan for first 3 days
   - Support channel setup
   - Escalation matrix
   - Daily review checklist
   - Weekly SLO review template
```

---

### PROMPT #43 → `@project-tracker`

```
Update status for M10. Review:
- k8s/production/ directory
- docs/operations/ directory

Update docs/milestones/M10-pilot.md.
Update docs/milestones/STATUS-REPORT.md.
Note: M10-016 to M10-027 (hypercare + validation) are operational processes.
Mark as READY — execution happens during actual pilot.
```

---

## PHASE 9: M11 — Regional Rollout (34 issues)

### PROMPT #44 → `@devops-engineer`

```
Create automated rollout scripts (M11-001 to M11-005).

Create in k8s/scripts/ and docs/operations/:

1. k8s/scripts/branch-onboard.sh
   - Input: branch_name, region, admin_email
   - Creates Keycloak group under /regions/{region}/{branch}
   - Creates admin user with BRANCH_ADMIN role
   - Runs data migration for the branch
   - Verifies branch data isolation

2. k8s/scripts/batch-rollout.sh
   - Input: batch config file (list of branches)
   - Loops through branches: onboard → verify → report
   - Generates per-batch report

3. docs/operations/rollout-schedule.md
   - Batch grouping strategy (by region/risk)
   - 2-4 branches per week
   - Per-branch migration checklist
   - Staff training schedule template

4. k8s/scripts/verify-branch.sh
   - Verify branch user can login
   - Verify branch data isolation (cannot see other branch)
   - Verify all features functional for branch
   - Generate verification report

5. k8s/scripts/scaling-check.sh
   - Check HPA status after each batch
   - Check database connection pool
   - Check Redis cache hit rates
   - Check RabbitMQ queue depths
```

---

### PROMPT #45 → `@docs-writer`

```
Create regional rollout documentation (M11-026 to M11-034).

1. docs/operations/cross-branch-validation.md
   - Inter-branch transfer test procedure
   - Regional dashboard verification
   - Emergency broadcast verification
   - REGIONAL_ADMIN multi-branch view verification

2. docs/operations/scaling-validation.md
   - HPA scaling verification procedure
   - Database performance with multi-branch volume
   - Redis cache hit rate targets (>90%)
   - Alert threshold tuning guide

3. docs/operations/regional-signoff-template.md
   - Template for regional management sign-off
   - Criteria: all branches live, no critical issues, staff trained
```

---

### PROMPT #46 → `@project-tracker`

```
Update status for M11. Review:
- k8s/scripts/ directory
- docs/operations/ directory

Update docs/milestones/M11-regional-rollout.md.
Update docs/milestones/STATUS-REPORT.md.
Note: M11-006 to M11-025 (batch execution) are operational. Mark as READY.
```

---

## PHASE 10: M12 — Worldwide Launch (20 issues)

### PROMPT #47 → `@devops-engineer`

```
Create worldwide launch infrastructure (M12-002 to M12-005, M12-009, M12-010).

1. k8s/scripts/final-security-scan.sh
   - Run Trivy on all 15 Docker images
   - Run OWASP dependency-check on Gradle
   - Fail on any CRITICAL vulnerability
   - Generate security report

2. k8s/scripts/dr-test.sh (disaster recovery test)
   - Simulate primary database failure
   - Verify failover to replica
   - Verify service recovery
   - Test backup restore procedure

3. k8s/production/cdn-config.yml
   - CDN configuration for frontend static assets
   - Cache headers for JS/CSS/images

4. k8s/production/tls-config.yml
   - Production TLS certificate configuration
   - Security headers: HSTS, X-Frame-Options, CSP, X-Content-Type-Options

5. k8s/scripts/dns-switch.sh
   - DNS cutover script for production domains
   - Health check after DNS propagation
```

---

### PROMPT #48 → `@docs-writer`

```
Create worldwide launch documentation (M12-001, M12-006, M12-012 to M12-020).

1. docs/operations/launch-checklist.md
   - Pre-launch verification (all branches live, security scan clean,
     DR tested, backups verified)
   - SLA documentation summary

2. docs/operations/runbooks/ — one per scenario:
   - runbook-service-down.md — service health check failed
   - runbook-database-issues.md — connection pool, slow queries, failover
   - runbook-high-error-rate.md — error rate >5%
   - runbook-security-incident.md — suspected breach or unauthorized access
   - runbook-data-corruption.md — data integrity issue
   - runbook-rollback.md — emergency version rollback

3. docs/operations/on-call-guide.md
   - On-call rotation schedule template
   - Escalation procedures (L1 → L2 → L3)
   - Incident severity definitions
   - Response time SLAs per severity

4. docs/operations/incident-response.md
   - Incident response playbook
   - Communication templates (internal, external)
   - Post-incident review template

5. docs/operations/go-live-announcement.md
   - Template for go-live communication to all users
```

---

### PROMPT #49 → `@project-tracker`

```
Update status for M12. Review:
- k8s/scripts/ directory
- k8s/production/ directory
- docs/operations/ directory

Update docs/milestones/M12-worldwide-launch.md.
Update docs/milestones/STATUS-REPORT.md.
Note: M12-007, M12-008, M12-017 to M12-020 are operational. Mark as READY.
```

---

## PHASE 11: M13 — Post-Launch + Continuous Improvement (33 issues)

### PROMPT #50 → `@docs-writer`

```
Create post-launch operations documentation (M13-001 to M13-016).

1. docs/operations/stabilization-plan.md
   - 24/7 monitoring plan for first 2 weeks
   - Daily triage process
   - Critical bug SLA (4 hours)
   - Alert tuning schedule

2. docs/operations/ongoing-operations.md
   - Weekly operations review template
   - Monthly SLO review template
   - Quarterly DR drill schedule
   - Quarterly security assessment checklist
   - Quarterly dependency update process (Spring Boot, Angular, etc.)
   - Annual penetration testing requirements

3. docs/compliance/ongoing-compliance.md
   - Quarterly access review process (dormant accounts >90 days)
   - Annual HIPAA audit checklist
   - Annual GDPR review checklist
   - Regulatory update monitoring process (FDA, AABB, WHO)
   - SOP version management lifecycle
```

---

### PROMPT #51 → `@docs-writer`

```
Create future enhancement roadmap and SRE documentation (M13-017 to M13-033).

1. docs/roadmap/future-enhancements.md
   - Mobile app (React Native / Flutter) — scope and timeline estimate
   - Offline blood camp PWA — requirements
   - AI/ML blood demand forecasting — data requirements, model approach
   - IoT cold chain monitoring — sensor integration architecture
   - WhatsApp Business API — notification channel integration
   - Government ID verification — API integration points
   - Additional languages (Hindi, Arabic, Chinese) — i18n expansion
   - HL7 FHIR R4 — hospital system integration architecture
   - Payment gateway — integration points for billing-service
   - ERP export — data export formats and scheduling
   - Advanced analytics/BI — dashboard requirements
   - Donor gamification — social sharing, achievements

2. docs/operations/sre-guide.md
   - SLO definitions for all services (availability, latency, error rate)
   - Error budget calculation and tracking
   - Burn rate alerting configuration
   - Chaos engineering experiment catalog (pod kills, network latency, disk full)
   - Capacity planning model (users × branches → resource requirements)
   - Cost optimization guide (right-sizing K8s, reserved instances, spot instances)
```

---

### PROMPT #52 → `@project-tracker`

```
FINAL STATUS UPDATE.

Review the ENTIRE project:
- All 14 backend services in backend/
- Frontend in frontend/bloodbank-ui/
- Infrastructure in k8s/, monitoring/, keycloak/
- Tests in backend/integration-tests/, performance-tests/
- Documentation in docs/

Update docs/milestones/STATUS-REPORT.md with FINAL status for ALL milestones M0-M13.
Update docs/milestones/README.md.
Calculate final completion percentage.
List any remaining items that require manual/operational execution.
```

---