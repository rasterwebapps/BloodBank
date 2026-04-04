# M7: Infrastructure — Docker, Kubernetes, CI/CD, Monitoring

**Duration:** 2 weeks
**Dependencies:** M2 (Core Services — can start early in parallel)
**Exit Gate:** Jenkins pipeline deploys all services to DEV environment

---

## Objective

Set up complete infrastructure: containerization, orchestration, CI/CD pipeline, identity management, and observability stack.

## Issues

### Docker
- [ ] **M7-001**: Create multi-stage Dockerfiles for all 14 services
- [ ] **M7-002**: Create Dockerfile for Angular frontend (nginx)
- [ ] **M7-003**: Optimize Docker images (non-root user, health checks, minimal layers)
- [ ] **M7-004**: Create `.dockerignore` files
- [ ] **M7-005**: Update docker-compose.yml with all services
- [ ] **M7-006**: Test full stack startup via docker-compose

### Kubernetes
- [ ] **M7-007**: Create namespace manifests (bloodbank-dev, bloodbank-staging, bloodbank-uat, bloodbank-prod)
- [ ] **M7-008**: Create Deployment manifests for all 14 services
- [ ] **M7-009**: Create Service manifests (ClusterIP) for all services
- [ ] **M7-010**: Create Ingress manifest (NGINX) with TLS
- [ ] **M7-011**: Create ConfigMaps for environment-specific configuration
- [ ] **M7-012**: Create Secrets for credentials (DB, Redis, RabbitMQ, Keycloak)
- [ ] **M7-013**: Create HPA (Horizontal Pod Autoscaler) for production services
- [ ] **M7-014**: Create StatefulSets for PostgreSQL, Redis, RabbitMQ (if self-hosted)
- [ ] **M7-015**: Create Flyway migration K8s Job (runs BEFORE services start)
- [ ] **M7-016**: Configure readiness and liveness probes for all services
- [ ] **M7-017**: Configure resource requests and limits
- [ ] **M7-018**: Test deployment to K8s dev namespace

### Jenkins CI/CD Pipeline
- [ ] **M7-019**: Create Jenkinsfile with 11-stage pipeline
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
- [ ] **M7-032**: Create realm-export.json for `bloodbank` realm
- [ ] **M7-033**: Configure clients: `bloodbank-api` (confidential), `bloodbank-ui` (public PKCE)
- [ ] **M7-034**: Create all 16 roles (4 realm + 12 client)
- [ ] **M7-035**: Create group hierarchy: /global, /regions/{region}/{branch}, /hospitals/{hospital}
- [ ] **M7-036**: Configure LDAP federation (READ_ONLY, LDAPS port 636)
- [ ] **M7-037**: Configure MFA policies (required for admin, optional for clinical)
- [ ] **M7-038**: Configure session policies (idle timeout, max lifetime per role type)
- [ ] **M7-039**: Configure password policies (12+ chars, complexity, history)
- [ ] **M7-040**: Create test users for each of the 16 roles

### Monitoring & Observability
- [ ] **M7-041**: Configure Prometheus — scrape configs for all services, custom metrics
- [ ] **M7-042**: Create Grafana dashboards — service health, JVM metrics, API latency, error rates
- [ ] **M7-043**: Configure Loki — log aggregation from all services
- [ ] **M7-044**: Configure Tempo — distributed tracing collection
- [ ] **M7-045**: Create Alertmanager rules — service down, high error rate, stock critical, slow API
- [ ] **M7-046**: Create SRE dashboard — SLO tracking, error budget

## Deliverables

1. Dockerized all 14 services + frontend
2. Kubernetes manifests for 4 environments
3. Jenkins 11-stage CI/CD pipeline
4. Keycloak realm with 16 roles, LDAP, MFA
5. Prometheus + Grafana + Loki + Tempo observability stack
6. Automated deployment to DEV environment
