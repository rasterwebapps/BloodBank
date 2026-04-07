# M5: API Gateway + Frontend

**Duration:** 3 weeks
**Dependencies:** M2 (Core), M3 (Clinical), M4 (Support)
**Exit Gate:** All features render correctly per user role

## 📊 Development Status: 🟡 IN PROGRESS (~29%)

**Started:** 2026-04-07 | **PRs:** #15 (merged 2026-04-07)
**Issues Completed:** 15/52
- API Gateway: ✅ Complete (7 main files, 10 test files, 30 tests — PR #15)
- Config Server: ✅ Complete (2 main files, 1 test file, 10 tests — PR #15)
- Config Repository: ✅ Complete (16 YAML files in config-repo/ — PR #15)
- Angular frontend: ❌ Not started (directory does not exist yet)

## 🔧 FIX REQUIRED

| # | Issue | Severity | Description |
|---|---|---|---|
| 1 | **Blocked by M3 for frontend clinical features** | 🔴 BLOCKER | M3 (clinical services) is only ~35% complete. hospital-service and request-matching-service must be finished before frontend clinical features can be built. transfusion-service is complete (PR #11 merged). |
| 2 | **Blocked by M4 for frontend compliance features** | 🔴 BLOCKER | M4 compliance-service is not implemented. Must be completed before this milestone can fully close. |
| 3 | **frontend/ directory missing** | 🟡 INFO | Angular 21 project has not been scaffolded yet. Will need `ng new bloodbank-ui` with standalone components. |

---

## Objective

Implement the API Gateway with SSO integration and the complete Angular 21 frontend with 17 feature modules across 3 portals.

## Issues

### API Gateway (Spring Cloud Gateway)
- [x] **M5-001**: Scaffold api-gateway project structure *(PR #15)*
- [x] **M5-002**: Configure route definitions for all 12 backend services *(PR #15 — path-based predicates for ports 8081–8092)*
- [x] **M5-003**: Implement JWT validation filter (Keycloak token verification) *(PR #15 — spring-boot-starter-oauth2-resource-server with reactive Keycloak role extraction)*
- [x] **M5-004**: Implement BranchIdExtractionFilter (extract branch_id from JWT → X-Branch-Id header) *(PR #15 — 1st layer of 4-layer branch isolation)*
- [x] **M5-005**: Configure rate limiting (Resilience4j + Redis) *(PR #15 — RequestRateLimiter, 100 req/sec per user)*
- [x] **M5-006**: Configure CORS policies *(PR #15 — configurable allowed origins, defaults to localhost:4200)*
- [x] **M5-007**: Implement request/response logging *(PR #15 — X-Request-Id propagation for distributed tracing)*
- [x] **M5-008**: Configure circuit breaker for downstream services *(PR #15 — Resilience4j, 50% failure threshold, 10s wait)*
- [x] **M5-009**: Create health check aggregation endpoint *(PR #15 — /actuator/health with show-details)*
- [x] **M5-010**: Write integration tests for gateway routing *(PR #15 — 30 tests, >80% coverage)*

### Config Server (Spring Cloud Config)
- [x] **M5-011**: Scaffold config-server project structure *(PR #15)*
- [x] **M5-012**: Configure Git-backed configuration repository *(PR #15 — native profile with config-repo/ directory + classpath)*
- [x] **M5-013**: Create environment-specific configs (dev, staging, prod) *(PR #15 — application-dev.yml, application-staging.yml, application-prod.yml)*
- [x] **M5-014**: Configure encryption for sensitive properties *(PR #15 — ENCRYPT_KEY env var)*
- [x] **M5-015**: Test config refresh across services *(PR #15 — 10 tests)*

### Angular Frontend — Core & Shared
- [ ] **M5-016**: Scaffold Angular 21 project with standalone components
- [ ] **M5-017**: Create core module — AuthService, HttpInterceptor, RoleGuard, ErrorHandler
- [ ] **M5-018**: Implement Keycloak OIDC integration (keycloak-angular)
- [ ] **M5-019**: Create shared components — DataTable, SearchBar, FormField, StatusBadge, ConfirmDialog
- [ ] **M5-020**: Create shared layout — SideNav, TopBar, Breadcrumb, NotificationBell
- [ ] **M5-021**: Implement role-based navigation (show/hide menu items per role)
- [ ] **M5-022**: Create shared models — ApiResponse, PagedResponse, User, Branch
- [ ] **M5-023**: Configure i18n (en, es, fr language files)
- [ ] **M5-024**: Create design system — Angular Material + custom theme

### Angular Frontend — Feature Modules (Staff Portal)
- [ ] **M5-025**: Feature: Dashboard — role-specific widgets, charts, KPIs
- [ ] **M5-026**: Feature: Donor — list, register, search, profile, history, deferral
- [ ] **M5-027**: Feature: Collection — collection recording, adverse reactions, samples
- [ ] **M5-028**: Feature: Lab — test orders, results, QC, dual-review
- [ ] **M5-029**: Feature: Inventory — stock dashboard, components, storage, transfers, disposal
- [ ] **M5-030**: Feature: Transfusion — cross-match, issue, transfuse, reaction reporting
- [ ] **M5-031**: Feature: Branch — branch management, master data
- [ ] **M5-032**: Feature: Camp — blood camp planning, execution, follow-up
- [ ] **M5-033**: Feature: Billing — invoices, payments, rates, credit notes
- [ ] **M5-034**: Feature: Compliance — SOPs, licenses, deviations, recalls
- [ ] **M5-035**: Feature: Notification — templates, campaigns, preferences
- [ ] **M5-036**: Feature: Reporting — operational reports, regulatory reports, custom builder
- [ ] **M5-037**: Feature: Document — upload, version, browse, download
- [ ] **M5-038**: Feature: Emergency — emergency requests, disaster response
- [ ] **M5-039**: Feature: User Management — Keycloak admin integration
- [ ] **M5-040**: Feature: Settings — system settings, feature flags

### Angular Frontend — Hospital Portal
- [ ] **M5-041**: Hospital login with HOSPITAL_USER role
- [ ] **M5-042**: Blood request submission form
- [ ] **M5-043**: Request tracking dashboard
- [ ] **M5-044**: Contract and SLA viewing
- [ ] **M5-045**: Feedback submission

### Angular Frontend — Donor Portal
- [ ] **M5-046**: Donor self-registration
- [ ] **M5-047**: Donation history view
- [ ] **M5-048**: Appointment booking
- [ ] **M5-049**: Eligibility self-check
- [ ] **M5-050**: Nearby camp finder
- [ ] **M5-051**: Digital donor card
- [ ] **M5-052**: Referral program

## Deliverables

1. API Gateway with JWT validation, rate limiting, branch extraction
2. Config Server with environment-specific configurations
3. Angular 21 frontend with 17 feature modules
4. 3 portals: Staff, Hospital, Donor
5. Role-based navigation and access control
6. Multi-language support (en, es, fr)
