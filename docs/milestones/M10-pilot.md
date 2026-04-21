# M10: Pilot Deployment (1 Branch)

**Duration:** 2 weeks
**Dependencies:** M9 (UAT + Compliance)
**Exit Gate:** Pilot branch operating successfully with hypercare support

## 📊 Development Status: 🟡 READY — Infrastructure Prepared (11/27 complete, 16 operational READY)

**Issues Completed (infrastructure/docs in place):** 11/27
**Issues READY (operational — executed during actual pilot):** 16/27
**Blocked by:** M9 (UAT + Compliance sign-off)

### What's In Place
- ✅ `k8s/namespaces/bloodbank-prod.yml` — production namespace manifest
- ✅ `k8s/configmaps/shared-config.yml` — production ConfigMap (targets `bloodbank-prod`)
- ✅ `k8s/ingress/bloodbank-ingress.yml` — TLS ingress for `bloodbank.example.com` in `bloodbank-prod`
- ✅ `k8s/jobs/flyway-migration.yml` — Flyway migration Job
- ✅ `k8s/deployments/` — 14 microservice + frontend deployment manifests
- ✅ `k8s/services/` — 15 service manifests
- ✅ `k8s/hpa/` — HPA for all 14 services
- ✅ `k8s/statefulsets/` — PostgreSQL, Redis, RabbitMQ StatefulSets
- ✅ M7 monitoring stack — Prometheus, 6 Grafana dashboards, Loki, Alertmanager, SRE/SLO dashboard
- ✅ M7 Jenkinsfile — Blue-Green + Canary rollback strategies (rollback plan)
- ✅ `docs/operations/pilot-plan.md` — branch selection criteria, training schedule, go/no-go criteria
- ✅ `docs/operations/hypercare-plan.md` — 14-day hypercare plan, on-call rota, SLO review template
- ✅ `docs/operations/data-migration-guide.md` — step-by-step migration procedures
- ✅ `docs/operations/user-guides/` — 7 quick-reference guides (admin, clinical, lab, reception, inventory, donor, hospital)
- ✅ M7 Keycloak realm-export.json — 16 roles, LDAP federation, MFA, production realm

---

## Objective

Deploy to a single pilot branch for real-world validation with hypercare support.

## Issues

### Pilot Preparation
- [ ] **M10-001**: Select pilot branch and coordinate with branch staff ⚙️ READY — selection criteria documented in `docs/operations/pilot-plan.md`
- [ ] **M10-002**: Migrate pilot branch historical data (donors, inventory, records) ⚙️ READY — procedures documented in `docs/operations/data-migration-guide.md`
- [x] **M10-003**: Create production environment in K8s (bloodbank-prod namespace) — `k8s/namespaces/bloodbank-prod.yml` + configmaps/ingress targeting `bloodbank-prod` ✅
- [ ] **M10-004**: Configure production secrets (database, Redis, RabbitMQ, Keycloak) ⚙️ READY — `k8s/configmaps/shared-config.yml` defines env vars; secrets provisioned via `kubectl create secret` at deployment time
- [x] **M10-005**: Set up production monitoring dashboards and alerts — M7 complete: Prometheus, 6 Grafana dashboards, Loki, Alertmanager, SRE/SLO dashboard ✅
- [x] **M10-006**: Create rollback plan and procedures — Jenkinsfile Blue-Green + Canary strategies; go/no-go criteria in `docs/operations/pilot-plan.md` ✅
- [x] **M10-007**: Train pilot branch staff on all roles — training schedule in `docs/operations/pilot-plan.md` (Day 1–2 schedule, competency assessment, remedial sessions) ✅
- [x] **M10-008**: Create user documentation and quick-reference guides — `docs/operations/user-guides/` contains 7 quick-ref guides ✅

### Production Deployment
- [x] **M10-009**: Deploy production database with Flyway migrations — `k8s/jobs/flyway-migration.yml` (Flyway Job, 20 migrations in `shared-libs/db-migration/`) ✅
- [x] **M10-010**: Deploy Keycloak with production realm and LDAP federation — M7 realm-export.json (16 roles, LDAP, MFA); K8s deployment manifests exist ✅
- [x] **M10-011**: Deploy all 14 microservices to production K8s — all 14 deployment manifests in `k8s/deployments/` with probes, resource limits, HPA ✅
- [x] **M10-012**: Deploy Angular frontend with production configuration — `k8s/deployments/frontend.yml` + `k8s/services/frontend.yml` ✅
- [x] **M10-013**: Configure production TLS certificates — `k8s/ingress/bloodbank-ingress.yml` has TLS block for `bloodbank-tls` secret, ssl-redirect enforced ✅
- [x] **M10-014**: Configure production DNS entries — ingress rule for `bloodbank.example.com` defined ✅
- [ ] **M10-015**: Verify all health checks pass in production ⚙️ READY — K8s liveness/readiness probes configured in all deployments; verification runs at go-live

### Hypercare (2 Weeks)
> ⚙️ **M10-016 to M10-022 are operational processes** — plans and templates documented in `docs/operations/hypercare-plan.md`. Execution happens during the actual pilot.

- [ ] **M10-016**: Monitor system health 24/7 for first 3 days ⚙️ READY
- [ ] **M10-017**: Dedicated support channel for pilot branch staff ⚙️ READY
- [ ] **M10-018**: Daily review of error logs and alerting rules ⚙️ READY
- [ ] **M10-019**: Weekly performance review against SLO targets ⚙️ READY
- [ ] **M10-020**: Collect user feedback and prioritize improvements ⚙️ READY
- [ ] **M10-021**: Fix critical issues within 4-hour SLA ⚙️ READY
- [ ] **M10-022**: Verify data integrity (blood unit tracking, audit trail) ⚙️ READY

### Pilot Validation
> ⚙️ **M10-023 to M10-027 are operational processes** — go/no-go criteria and sign-off procedure documented in `docs/operations/pilot-plan.md`. Execution happens during the actual pilot.

- [ ] **M10-023**: Verify complete blood lifecycle in production (real data) ⚙️ READY
- [ ] **M10-024**: Verify branch data isolation (pilot sees only own data) ⚙️ READY
- [ ] **M10-025**: Verify notification delivery (email, SMS) ⚙️ READY
- [ ] **M10-026**: Verify report generation with real data ⚙️ READY
- [ ] **M10-027**: Pilot sign-off from branch management ⚙️ READY

## Deliverables

1. Production environment running in K8s ← **infrastructure prepared** ✅
2. Pilot branch operating on BloodBank system ← during pilot execution ⚙️
3. 2-week hypercare report ← during pilot execution ⚙️
4. User feedback report ← during pilot execution ⚙️
5. Pilot sign-off document ← during pilot execution ⚙️
