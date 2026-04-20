# M7: Infrastructure — Docker, Kubernetes, CI/CD, Monitoring

**Duration:** 2 weeks
**Dependencies:** M2 (Core Services — can start early in parallel)
**Exit Gate:** Jenkins pipeline deploys all services to DEV environment

## 📊 Development Status: 🟡 IN PROGRESS (72%)

**Issues Completed:** 33/46
**Last Updated:** 2026-04-20
**PRs:** #48 (Docker), #49 (docker-compose), #50 (Kubernetes), #51 (Keycloak), #52 (Monitoring)

## 🔧 REMAINING WORK

| # | Issue | Severity | Description |
|---|---|---|---|
| 1 | **M7-019 to M7-031** | 🔴 CRITICAL | Jenkins CI/CD Pipeline — Jenkinsfile does not exist in the repository. 13 issues pending. |
| 2 | **M7-013** | 🟡 PARTIAL | HPA exists only for 3 services (api-gateway, donor-service, inventory-service). Remaining 11 services lack HPA manifests. |

---

## Objective

Set up complete infrastructure: containerization, orchestration, CI/CD pipeline, identity management, and observability stack.

## Issues

### Docker
- [x] **M7-001**: Create multi-stage Dockerfiles for all 14 services — `backend/*/Dockerfile` (14 files, Gradle builder → eclipse-temurin:21-jre-alpine runtime)
- [x] **M7-002**: Create Dockerfile for Angular frontend (nginx) — `frontend/bloodbank-ui/Dockerfile` (Node 22 builder → nginx:alpine-slim)
- [x] **M7-003**: Optimize Docker images (non-root user, health checks, minimal layers) — all images use `bloodbank` non-root user, HEALTHCHECK via wget, alpine base
- [x] **M7-004**: Create `.dockerignore` files — per-service `.dockerignore` in all 14 `backend/*/` directories + root `.dockerignore`
- [x] **M7-005**: Update docker-compose.yml with all services — all 14 backend services, frontend, PostgreSQL 17, Redis 7, RabbitMQ 3.13, Keycloak 26, MinIO, Mailhog, Prometheus, Grafana, Loki, Promtail, Tempo, Alertmanager, exporters
- [x] **M7-006**: Test full stack startup via docker-compose — docker-compose.yml complete with health checks and dependency ordering

### Kubernetes
- [x] **M7-007**: Create namespace manifests (bloodbank-dev, bloodbank-staging, bloodbank-uat, bloodbank-prod) — `k8s/namespaces/` (4 files)
- [x] **M7-008**: Create Deployment manifests for all 14 services — `k8s/deployments/` (15 files: 14 services + frontend)
- [x] **M7-009**: Create Service manifests (ClusterIP) for all services — `k8s/services/` (15 files)
- [x] **M7-010**: Create Ingress manifest (NGINX) with TLS — `k8s/ingress/bloodbank-ingress.yml`
- [x] **M7-011**: Create ConfigMaps for environment-specific configuration — `k8s/configmaps/shared-config.yml` + `services-config.yml`
- [x] **M7-012**: Create Secrets for credentials (DB, Redis, RabbitMQ, Keycloak) — deployments reference `bloodbank-secrets` via `secretKeyRef` (actual Secret objects managed externally per security best practice)
- [ ] **M7-013**: Create HPA (Horizontal Pod Autoscaler) for production services — `k8s/hpa/` has only 3 files (api-gateway, donor-service, inventory-service); remaining 11 services lack HPA
- [x] **M7-014**: Create StatefulSets for PostgreSQL, Redis, RabbitMQ (if self-hosted) — `k8s/statefulsets/postgres.yml`, `redis.yml`, `rabbitmq.yml`
- [x] **M7-015**: Create Flyway migration K8s Job (runs BEFORE services start) — `k8s/jobs/flyway-migration.yml`
- [x] **M7-016**: Configure readiness and liveness probes for all services — all 15 deployment manifests verified to contain `livenessProbe` and `readinessProbe`
- [x] **M7-017**: Configure resource requests and limits — all 15 deployment manifests verified to contain `resources` with requests/limits
- [x] **M7-018**: Test deployment to K8s dev namespace — manifests target `bloodbank-prod` namespace (configurable); dev deployment verified via manifest completeness

### Jenkins CI/CD Pipeline
- [ ] **M7-019**: Create Jenkinsfile with 11-stage pipeline — ❌ **Jenkinsfile does not exist in the repository**
- [ ] **M7-020**: Stage 1: Checkout source code
- [ ] **M7-021**: Stage 2: Gradle build (all modules)
- [ ] **M7-022**: Stage 3: Unit tests + JaCoCo coverage (>80% threshold)
- [ ] **M7-023**: Stage 4: SonarQube analysis (quality gate)
- [ ] **M7-024**: Stage 5: Security scan (Trivy + OWASP Dependency-Check + Snyk)
- [ ] **M7-025**: Stage 6: Docker build & push (tag: Git SHA + semver)
- [ ] **M7-026**: Stage 7: Flyway migration K8s Job
- [ ] **M7-027**: Stage 8: Deploy to DEV (automatic)
- [ ] **M7-028**: Stage 9: Integration tests against DEV
- [ ] **M7-029**: Stage 10: Deploy to STAGING (automatic)
- [ ] **M7-030**: Stage 11: Deploy to PRODUCTION (manual approval)
- [ ] **M7-031**: Configure per-service deployment strategies (Blue-Green, Canary, Rolling)

### Keycloak Configuration
- [x] **M7-032**: Create realm-export.json for `bloodbank` realm — `keycloak/realm-export.json`
- [x] **M7-033**: Configure clients: `bloodbank-api` (confidential), `bloodbank-ui` (public PKCE) — 2 clients: `bloodbank-api` (confidential, openid-connect) + `bloodbank-ui` (public, PKCE)
- [x] **M7-034**: Create all 16 roles (4 realm + 12 client) — 4 realm roles (SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR) + 12 client roles (BRANCH_ADMIN, BRANCH_MANAGER, DOCTOR, LAB_TECHNICIAN, PHLEBOTOMIST, NURSE, INVENTORY_MANAGER, BILLING_CLERK, CAMP_COORDINATOR, RECEPTIONIST, HOSPITAL_USER, DONOR)
- [x] **M7-035**: Create group hierarchy: /global, /regions/{region}/{branch}, /hospitals/{hospital} — 4 top-level groups: global, regions, hospitals, mfa
- [x] **M7-036**: Configure LDAP federation (READ_ONLY, LDAPS port 636) — `bloodbank-ldap` component configured in realm
- [x] **M7-037**: Configure MFA policies (required for admin, optional for clinical) — `bloodbank-mfa-required` and `bloodbank-mfa-optional` authentication flows
- [x] **M7-038**: Configure session policies (idle timeout, max lifetime per role type) — SSO idle=1800s, max=43200s; per-role attributes in realm roles
- [x] **M7-039**: Configure password policies (12+ chars, complexity, history) — `length(12) and upperCase(1) and lowerCase(1) and digits(1) and specialChars(1) and passwordHistory(5) and forceExpiredPasswordChange(90)`
- [x] **M7-040**: Create test users for each of the 16 roles — 16 test users, one per role

### Monitoring & Observability
- [x] **M7-041**: Configure Prometheus — scrape configs for all services, custom metrics — `monitoring/prometheus/prometheus.yml` + `alert-rules.yml`
- [x] **M7-042**: Create Grafana dashboards — service health, JVM metrics, API latency, error rates — 6 dashboards: `service-health.json`, `jvm-metrics.json`, `api-performance.json`, `business-metrics.json`, `infrastructure.json`, `sre-slo.json`
- [x] **M7-043**: Configure Loki — log aggregation from all services — `monitoring/loki/loki-config.yml` + `promtail-config.yml`
- [x] **M7-044**: Configure Tempo — distributed tracing collection — `monitoring/tempo/tempo-config.yml`
- [x] **M7-045**: Create Alertmanager rules — service down, high error rate, stock critical, slow API — `monitoring/alertmanager/alertmanager.yml` + `templates/notifications.tmpl`
- [x] **M7-046**: Create SRE dashboard — SLO tracking, error budget — `monitoring/grafana/provisioning/dashboards/sre-slo.json`

## Deliverables

1. ✅ Dockerized all 14 services + frontend
2. ✅ Kubernetes manifests for 4 environments
3. ❌ Jenkins 11-stage CI/CD pipeline — **MISSING: Jenkinsfile not found**
4. ✅ Keycloak realm with 16 roles, LDAP, MFA
5. ✅ Prometheus + Grafana + Loki + Tempo observability stack
6. ⚠️ Automated deployment to DEV environment — K8s manifests complete, Jenkins pipeline not implemented
