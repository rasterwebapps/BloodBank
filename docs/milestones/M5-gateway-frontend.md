# M5: API Gateway + Frontend

**Duration:** 3 weeks
**Dependencies:** M2 (Core), M3 (Clinical), M4 (Support)
**Exit Gate:** All features render correctly per user role

---

## Objective

Implement the API Gateway with SSO integration and the complete Angular 21 frontend with 17 feature modules across 3 portals.

## Issues

### API Gateway (Spring Cloud Gateway)
- [ ] **M5-001**: Scaffold api-gateway project structure
- [ ] **M5-002**: Configure route definitions for all 12 backend services
- [ ] **M5-003**: Implement JWT validation filter (Keycloak token verification)
- [ ] **M5-004**: Implement BranchIdExtractionFilter (extract branch_id from JWT → X-Branch-Id header)
- [ ] **M5-005**: Configure rate limiting (Resilience4j + Redis)
- [ ] **M5-006**: Configure CORS policies
- [ ] **M5-007**: Implement request/response logging
- [ ] **M5-008**: Configure circuit breaker for downstream services
- [ ] **M5-009**: Create health check aggregation endpoint
- [ ] **M5-010**: Write integration tests for gateway routing

### Config Server (Spring Cloud Config)
- [ ] **M5-011**: Scaffold config-server project structure
- [ ] **M5-012**: Configure Git-backed configuration repository
- [ ] **M5-013**: Create environment-specific configs (dev, staging, prod)
- [ ] **M5-014**: Configure encryption for sensitive properties
- [ ] **M5-015**: Test config refresh across services

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
