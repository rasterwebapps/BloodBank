# M5: API Gateway + Frontend

**Duration:** 3 weeks
**Dependencies:** M2 (Core), M3 (Clinical), M4 (Support)
**Exit Gate:** All features render correctly per user role

## 📊 Development Status: 🟡 NEARLY COMPLETE (51/52 = 98%)

**Started:** 2026-04-07 | **PRs:** #15 (merged 2026-04-07) + subsequent Angular frontend work
**Issues Completed:** 51/52
- API Gateway: ✅ Complete (7 main files, 10 test files, 30 tests — PR #15)
- Config Server: ✅ Complete (2 main files, 1 test file, 10 tests — PR #15)
- Config Repository: ✅ Complete (16 YAML files in config-repo/ — PR #15)
- Angular frontend: ✅ Complete (17 feature modules, 3 portals, core + shared)
- **Remaining:** M5-023 i18n language files (no `src/assets/i18n/` directory — en/es/fr JSON missing)

## ⚠️ REMAINING WORK

| # | Issue | Severity | Description |
|---|---|---|---|
| 1 | **M5-023: i18n language files missing** | 🟡 MEDIUM | No `src/assets/i18n/` directory. `en.json`, `es.json`, `fr.json` translation files have not been created. Application displays hardcoded English strings. |

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
- [x] **M5-016**: Scaffold Angular 21 project with standalone components *(app.ts, app.config.ts, app.routes.ts, angular.json — zoneless, standalone)*
- [x] **M5-017**: Create core module — AuthService, HttpInterceptor, RoleGuard, ErrorHandler *(core/auth/, core/guards/, core/interceptors/, core/services/)*
- [x] **M5-018**: Implement Keycloak OIDC integration (keycloak-angular) *(KeycloakService + APP_INITIALIZER in app.config.ts, auth.init.ts, auth.service.ts)*
- [x] **M5-019**: Create shared components — DataTable, SearchBar, FormField, StatusBadge, ConfirmDialog *(+ BloodGroupBadge, EmptyState, ErrorCard, LoadingSkeleton, UnauthorizedPage)*
- [x] **M5-020**: Create shared layout — SideNav, TopBar, Breadcrumb, Footer, Shell *(shared/layout/ — all 5 layout components complete)*
- [x] **M5-021**: Implement role-based navigation (show/hide menu items per role) *(roleGuard on every route + hasRole directive + role-aware sidenav)*
- [x] **M5-022**: Create shared models — ApiResponse, PagedResponse, User, Branch *(shared/models/ — 3 model files; User in core/models/user.model.ts)*
- [ ] **M5-023**: Configure i18n (en, es, fr language files) *(⚠️ MISSING — no src/assets/i18n/ directory)*
- [x] **M5-024**: Create design system — Angular Material + custom theme *(src/styles/ — _material-theme.scss, _healthcare-colors.scss, _tailwind.scss, _typography.scss, _variables.scss)*

### Angular Frontend — Feature Modules (Staff Portal)
- [x] **M5-025**: Feature: Dashboard — role-specific widgets, charts, KPIs *(dashboard.component, kpi-card sub-component, dashboard.service, models, routes — 9 files)*
- [x] **M5-026**: Feature: Donor — list, register, search, profile, history, deferral *(donor-list, donor-detail, donor-form, donor-search, service, models, routes — 15 files)*
- [x] **M5-027**: Feature: Collection — collection recording, adverse reactions, samples *(collection-list, collection-form, adverse-reaction-form, sample-registration, service, models, routes — 15 files)*
- [x] **M5-028**: Feature: Lab — test orders, results, QC, dual-review *(test-order-list, test-result-form, qc-dashboard, instrument-list, service, models, routes — 15 files)*
- [x] **M5-029**: Feature: Inventory — stock dashboard, components, storage, transfers, disposal *(stock-dashboard, blood-unit-list, component-processing, storage-management, transfer-form, disposal-form, service, models, routes — 24 files)*
- [x] **M5-030**: Feature: Transfusion — cross-match, issue, transfuse, reaction reporting *(cross-match-request, cross-match-result, blood-issue, transfusion-record, hemovigilance-list, reaction-report, service, models, routes — 21 files)*
- [x] **M5-031**: Feature: Branch — branch management, master data *(branch-list, branch-detail, branch-form, master-data, service, models, routes — 15 files)*
- [x] **M5-032**: Feature: Camp — blood camp planning, execution, follow-up *(camp-list, camp-detail, camp-form, camp-donor-registration, service, models, routes — 15 files)*
- [x] **M5-033**: Feature: Billing — invoices, payments, rates, credit notes *(invoice-list, invoice-detail, payment-form, rate-management, service, models, routes — 15 files)*
- [x] **M5-034**: Feature: Compliance — SOPs, licenses, deviations, recalls *(framework-list, sop-list, license-list, deviation-list, recall-list, service, models, routes — 18 files)*
- [x] **M5-035**: Feature: Notification — templates, campaigns, preferences *(notification-list, template-management, campaign, preference, service, models, routes — 15 files)*
- [x] **M5-036**: Feature: Reporting — operational reports, regulatory reports, custom builder *(report-builder, audit-log, chain-of-custody, scheduled-reports, service, models, routes — 15 files)*
- [x] **M5-037**: Feature: Document — upload, version, browse, download *(document-browser, document-upload, document-version, service, models, routes — 12 files)*
- [x] **M5-038**: Feature: Emergency — emergency requests, disaster response *(emergency-request, emergency-dashboard, disaster-response, service, models, routes — 12 files)*
- [x] **M5-039**: Feature: User Management — Keycloak admin integration *(user-list, user-form, user-activity, service, models, routes — 12 files)*
- [x] **M5-040**: Feature: Settings — system settings, feature flags *(system-settings, feature-flags, service, models, routes — 9 files)*

### Angular Frontend — Hospital Portal
- [x] **M5-041**: Hospital login with HOSPITAL_USER role *(hospital-dashboard with roleGuard + HOSPITAL_USER in app.routes.ts canActivate)*
- [x] **M5-042**: Blood request submission form *(blood-request-form component — 3 files)*
- [x] **M5-043**: Request tracking dashboard *(request-tracking component — 3 files)*
- [x] **M5-044**: Contract and SLA viewing *(contract-view component — 3 files)*
- [x] **M5-045**: Feedback submission *(feedback-form component — 3 files)*

### Angular Frontend — Donor Portal
- [x] **M5-046**: Donor self-registration *(donor-self-registration component — public route, no role guard)*
- [x] **M5-047**: Donation history view *(donation-history component)*
- [x] **M5-048**: Appointment booking *(appointment-booking component)*
- [x] **M5-049**: Eligibility self-check *(eligibility-check component)*
- [x] **M5-050**: Nearby camp finder *(nearby-camp-finder component)*
- [x] **M5-051**: Digital donor card *(digital-donor-card component)*
- [x] **M5-052**: Referral program *(referral component)*

## Deliverables

1. ✅ API Gateway with JWT validation, rate limiting, branch extraction
2. ✅ Config Server with environment-specific configurations
3. ✅ Angular 21 frontend with 17 feature modules (16 Staff + hospital-portal + donor-portal)
4. ✅ 3 portals: Staff, Hospital, Donor
5. ✅ Role-based navigation and access control (roleGuard on every route, hasRole directive)
6. ⚠️ Multi-language support (en, es, fr) — i18n translation files not yet created

## Verification Notes (2026-04-13)

### ✅ Angular Core (12 files)
- `core/auth/`: auth.init.ts, auth.interceptor.ts, auth.service.ts
- `core/guards/`: role.guard.ts, branch.guard.ts
- `core/interceptors/`: branch.interceptor.ts, error.interceptor.ts
- `core/models/`: role.enum.ts, user.model.ts
- `core/services/`: branch-context.service.ts, notification.service.ts, theme.service.ts

### ✅ Angular Shared (52 files)
- Components (10): data-table, search-bar, form-field, status-badge, confirm-dialog, blood-group-badge, empty-state, error-card, loading-skeleton, unauthorized-page
- Layout (5): shell, sidenav, topbar, breadcrumb, footer
- Directives (2): has-role, auto-focus
- Models (3): api-response, paged-response, branch
- Pipes (3): blood-group, date-ago, truncate

### ✅ Feature Modules (all 17 directories present)
| Feature | Files | Route |
|---|---|---|
| dashboard | 9 | /staff/dashboard |
| donor | 15 | /staff/donors |
| collection | 15 | /staff/collections |
| camp | 15 | /staff/camps |
| lab | 15 | /staff/lab |
| inventory | 24 | /staff/inventory |
| transfusion | 21 | /staff/transfusion |
| emergency | 12 | /staff/emergency |
| branch | 15 | /staff/branches |
| user-management | 12 | /staff/users |
| settings | 9 | /staff/settings |
| billing | 15 | /staff/billing |
| compliance | 18 | /staff/compliance |
| notification | 15 | /staff/notifications |
| reporting | 15 | /staff/reports |
| document | 12 | /staff/documents |
| hospital-portal | 18 | /hospital/* |
| donor-portal | 20 | /donor/* |

### ⚠️ Gap: i18n (M5-023)
- `src/assets/` directory does not exist
- No `en.json`, `es.json`, or `fr.json` translation files
- Application uses hardcoded English strings throughout
