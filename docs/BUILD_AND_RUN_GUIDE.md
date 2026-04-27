# BloodBank — Build & Run Guide

> **Audience:** Junior to Senior Engineers  
> **Covers:** Local Development · Docker Compose (full stack) · Kubernetes Production  
> **Last updated:** April 2026

---

## Table of Contents

1. [Architecture at a Glance](#1-architecture-at-a-glance)
2. [Prerequisites](#2-prerequisites)
3. [Clone & Project Structure](#3-clone--project-structure)
4. [Development Environment](#4-development-environment)
   - 4.1 [Start Infrastructure with Docker Compose](#41-start-infrastructure-with-docker-compose)
   - 4.2 [Build the Codebase (Gradle)](#42-build-the-codebase-gradle)
   - 4.3 [Run a Single Service Locally (bootRun)](#43-run-a-single-service-locally-bootrun)
   - 4.4 [Run the Frontend Dev Server](#44-run-the-frontend-dev-server)
   - 4.5 [Running Tests](#45-running-tests)
   - 4.6 [Development Service URLs](#46-development-service-urls)
5. [Full Stack via Docker Compose](#5-full-stack-via-docker-compose)
   - 5.1 [Build All Docker Images](#51-build-all-docker-images)
   - 5.2 [Start Services in the Correct Order](#52-start-services-in-the-correct-order)
   - 5.3 [Verify Everything is Healthy](#53-verify-everything-is-healthy)
   - 5.4 [Tear Down](#54-tear-down)
6. [Production — Kubernetes](#6-production--kubernetes)
   - 6.1 [Prerequisites](#61-prerequisites)
   - 6.2 [Step-by-Step Kubernetes Deploy](#62-step-by-step-kubernetes-deploy)
   - 6.3 [Verify the Cluster](#63-verify-the-cluster)
7. [CI/CD Pipeline (Jenkins)](#7-cicd-pipeline-jenkins)
8. [Configuration Reference](#8-configuration-reference)
9. [Port Reference](#9-port-reference)
10. [Troubleshooting](#10-troubleshooting)
11. [Quick-Reference Cheat Sheet](#11-quick-reference-cheat-sheet)

---

## 1. Architecture at a Glance

```
 ┌─────────────────────────────────────────────────────────────────────┐
 │  Client (Browser)                                                   │
 └──────────────┬──────────────────────────────────────────────────────┘
                │ :4200 (dev) / :80 (prod)
 ┌──────────────▼──────────────────────────────────────────────────────┐
 │  Frontend  (Angular 21 + Nginx)                                     │
 └──────────────┬──────────────────────────────────────────────────────┘
                │ /api/* → :8080
 ┌──────────────▼──────────────────────────────────────────────────────┐
 │  API Gateway  (Spring Cloud Gateway · :8080)                        │
 └──┬───────────────────────────────────────────────────────────────┬──┘
    │                                                               │
    │   Routes to individual microservices                          │
    │                                                               │
 ┌──▼──────────────────────────────────────────────────────────────▼──┐
 │                    12 Domain Microservices                          │
 │  branch :8081 │ donor :8082 │ lab :8083 │ inventory :8084          │
 │  transfusion :8085 │ hospital :8086 │ request-matching :8087       │
 │  billing :8088 │ notification :8089 │ reporting :8090              │
 │  document :8091 │ compliance :8092                                 │
 └──┬───────────────────────────────────────────────────────────────┬──┘
    │                                                               │
 ┌──▼──────────┐  ┌──────────┐  ┌───────────┐  ┌────────────────┐  │
 │ Config      │  │PostgreSQL│  │  Redis 7  │  │  RabbitMQ 3.13 │  │
 │ Server :8888│  │   :5432  │  │   :6379   │  │  :5672 / :15672│  │
 └─────────────┘  └──────────┘  └───────────┘  └────────────────┘  │
                                                                     │
 ┌───────────────────────────┐  ┌──────────────────────────────────┐  │
 │  Keycloak :8180           │  │  MinIO :9000/:9001               │◄─┘
 │  (OAuth2 / Keycloak 26+)  │  │  (Document Storage)              │
 └───────────────────────────┘  └──────────────────────────────────┘
```

**Key design rules**
- **Single shared PostgreSQL database** (`bloodbank_db`) — all services share one DB.
- **Flyway migrations** run once as a one-shot job before any service starts.
- **Config Server** must be healthy before any Spring Boot service launches.
- **RabbitMQ** is for async events only (thin payloads — IDs, not entity data).
- **Keycloak** manages 16 user roles across 3 portals (Staff / Hospital / Donor).

---

## 2. Prerequisites

### Minimum Tool Versions

| Tool | Required Version | Check Command |
|---|---|---|
| **Java (JDK)** | 21 (LTS) | `java -version` |
| **Docker** | 24+ | `docker --version` |
| **Docker Compose** | v2.x (plugin) | `docker compose version` |
| **Node.js** | 22 LTS | `node --version` |
| **npm** | 11+ | `npm --version` |
| **Git** | 2.x | `git --version` |

> **Note:** You do NOT need to install Gradle — the `gradlew` wrapper in the repo downloads the correct version automatically.

### For Kubernetes (production only)

| Tool | Required Version |
|---|---|
| **kubectl** | 1.30+ |
| **kubeseal** | 0.26+ (for secret encryption) |
| A running Kubernetes cluster | 1.30+ |

### System Resources (Docker Compose full stack)

| Resource | Minimum | Recommended |
|---|---|---|
| RAM | 12 GB | 16 GB |
| CPU | 4 cores | 8 cores |
| Disk | 20 GB free | 40 GB free |

---

## 3. Clone & Project Structure

```bash
git clone https://github.com/rasterwebapps/BloodBank.git
cd BloodBank
```

```
BloodBank/
├── build.gradle.kts            # Root Gradle build (Java 21, BOM imports)
├── settings.gradle.kts         # All 20 Gradle modules
├── gradle.properties           # Dependency versions
├── docker-compose.yml          # Full local stack definition
├── Jenkinsfile                 # CI/CD pipeline (11 stages)
├── config-repo/                # Spring Cloud Config files (per service, per profile)
├── backend/
│   ├── config-server/          # Spring Cloud Config Server (:8888)
│   ├── api-gateway/            # Spring Cloud Gateway (:8080)
│   ├── donor-service/          # Donor management (:8082)
│   ├── branch-service/         # Branch & master data (:8081)
│   ├── lab-service/            # Blood testing (:8083)
│   ├── inventory-service/      # Blood unit inventory (:8084)
│   ├── transfusion-service/    # Cross-match & transfusion (:8085)
│   ├── hospital-service/       # Hospital contracts (:8086)
│   ├── request-matching-service/ # Emergency matching (:8087)
│   ├── billing-service/        # Invoicing & payments (:8088)
│   ├── notification-service/   # Emails & alerts (:8089)
│   ├── reporting-service/      # Reports & dashboards (:8090)
│   ├── document-service/       # File storage via MinIO (:8091)
│   └── compliance-service/     # Regulatory compliance (:8092)
├── shared-libs/
│   ├── common-model/           # Base entities (BaseEntity, BranchScopedEntity)
│   ├── common-dto/             # Shared DTOs & API response wrappers
│   ├── common-events/          # RabbitMQ event records
│   ├── common-exceptions/      # Global exception handling
│   ├── common-security/        # JWT security config & branch filter
│   └── db-migration/           # Flyway SQL migrations (centralized)
├── frontend/
│   └── bloodbank-ui/           # Angular 21 application
├── k8s/                        # Kubernetes manifests
│   ├── namespaces/
│   ├── statefulsets/           # PostgreSQL, Redis, RabbitMQ
│   ├── deployments/            # All service deployments (blue-green)
│   ├── services/
│   ├── ingress/
│   ├── configmaps/
│   ├── secrets/                # SealedSecrets templates
│   ├── jobs/                   # Flyway migration Job
│   ├── hpa/                    # HorizontalPodAutoscalers
│   └── scripts/                # Operational scripts
├── keycloak/
│   └── realm-export.json       # Pre-configured realm with all 16 roles
└── monitoring/                 # Prometheus, Grafana, Loki, Tempo configs
```

---

## 4. Development Environment

> **Best for:** Day-to-day feature development. Run infrastructure in Docker and individual services on your host JVM for fast iteration.

### 4.1 Start Infrastructure with Docker Compose

Start only the infrastructure services (no application services yet):

```bash
# From the BloodBank/ root directory
docker compose up -d postgres redis rabbitmq keycloak minio mailhog
```

Wait for everything to become healthy (~2 minutes for Keycloak):

```bash
docker compose ps
# All services should show "healthy" in the STATUS column
```

> **Tip:** Keycloak alone can take 60–90 seconds on first boot because it imports the realm from `keycloak/realm-export.json`.

### 4.2 Build the Codebase (Gradle)

**Build everything (shared libs + all services):**

```bash
./gradlew build -x test --parallel
```

> `-x test` skips tests during the build phase. See [4.5 Running Tests](#45-running-tests) to run them separately.

**Build only a specific service (faster during development):**

```bash
./gradlew :backend:donor-service:build -x test
```

**Why shared libs first?** The Gradle dependency graph handles this automatically — shared libraries (`common-model`, `common-security`, etc.) are always compiled before any service that depends on them.

### 4.3 Run a Single Service Locally (bootRun)

> **Prerequisite:** Infrastructure from step 4.1 must be running.

The Config Server must start first because all other services fetch their configuration from it:

```bash
# Terminal 1 — Config Server (required first)
./gradlew :backend:config-server:bootRun
# Wait until you see: "Tomcat started on port(s): 8888"
```

Then start any service you are working on:

```bash
# Terminal 2 — example: donor-service
./gradlew :backend:donor-service:bootRun
```

Each service automatically picks up its configuration from `config-repo/` via the running Config Server.

**Run any other service the same way:**

| Service | Command |
|---|---|
| branch-service | `./gradlew :backend:branch-service:bootRun` |
| lab-service | `./gradlew :backend:lab-service:bootRun` |
| inventory-service | `./gradlew :backend:inventory-service:bootRun` |
| transfusion-service | `./gradlew :backend:transfusion-service:bootRun` |
| hospital-service | `./gradlew :backend:hospital-service:bootRun` |
| request-matching-service | `./gradlew :backend:request-matching-service:bootRun` |
| billing-service | `./gradlew :backend:billing-service:bootRun` |
| notification-service | `./gradlew :backend:notification-service:bootRun` |
| reporting-service | `./gradlew :backend:reporting-service:bootRun` |
| document-service | `./gradlew :backend:document-service:bootRun` |
| compliance-service | `./gradlew :backend:compliance-service:bootRun` |
| api-gateway | `./gradlew :backend:api-gateway:bootRun` |

> **Tip (debug mode):** Add `--debug-jvm` to any `bootRun` command and attach your IDE debugger on port 5005.

### 4.4 Run the Frontend Dev Server

```bash
cd frontend/bloodbank-ui

# Install dependencies (first time only)
npm install

# Start the development server with hot-reload
npm start
# Opens at http://localhost:4200
```

The dev server proxies API calls to `http://localhost:8080` (API Gateway). Make sure the gateway is running if you need to make API calls.

### 4.5 Running Tests

**Run all tests:**

```bash
./gradlew test
```

**Run tests for a specific service:**

```bash
./gradlew :backend:donor-service:test
```

**Run tests with coverage report:**

```bash
./gradlew :backend:donor-service:test jacocoTestReport
# HTML report: backend/donor-service/build/reports/jacoco/test/html/index.html
```

**Enforce coverage threshold (80% minimum):**

```bash
./gradlew jacocoTestCoverageVerification
```

**Frontend unit tests:**

```bash
cd frontend/bloodbank-ui
npm test
```

**Frontend E2E tests (Playwright):**

```bash
cd frontend/bloodbank-ui
npm run e2e
```

> **Note:** Integration tests in `backend/integration-tests/` use Testcontainers and require Docker to be running. They will automatically spin up PostgreSQL, Redis, and RabbitMQ containers.

### 4.6 Development Service URLs

| Service | URL | Notes |
|---|---|---|
| Frontend | http://localhost:4200 | Angular dev server |
| API Gateway | http://localhost:8080 | All API calls go through here |
| Config Server | http://localhost:8888 | Health: `/actuator/health` |
| donor-service | http://localhost:8082 | Swagger: `/swagger-ui.html` |
| branch-service | http://localhost:8081 | Swagger: `/swagger-ui.html` |
| lab-service | http://localhost:8083 | Swagger: `/swagger-ui.html` |
| inventory-service | http://localhost:8084 | Swagger: `/swagger-ui.html` |
| transfusion-service | http://localhost:8085 | Swagger: `/swagger-ui.html` |
| hospital-service | http://localhost:8086 | Swagger: `/swagger-ui.html` |
| request-matching-service | http://localhost:8087 | Swagger: `/swagger-ui.html` |
| billing-service | http://localhost:8088 | Swagger: `/swagger-ui.html` |
| notification-service | http://localhost:8089 | Swagger: `/swagger-ui.html` |
| reporting-service | http://localhost:8090 | Swagger: `/swagger-ui.html` |
| document-service | http://localhost:8091 | Swagger: `/swagger-ui.html` |
| compliance-service | http://localhost:8092 | Swagger: `/swagger-ui.html` |
| **Keycloak Admin** | http://localhost:8180 | `admin` / `admin` |
| **RabbitMQ UI** | http://localhost:15672 | `guest` / `guest` |
| **MinIO Console** | http://localhost:9001 | `minioadmin` / `minioadmin` |
| **MailHog** | http://localhost:8025 | Catches all outbound emails |

---

## 5. Full Stack via Docker Compose

> **Best for:** Integration testing, QA, demoing the complete system locally. Everything runs in Docker — no local JVM required.

### 5.1 Build All Docker Images

From the repository root:

```bash
# Build all backend and frontend images
docker compose build
```

This triggers multi-stage Docker builds:
- **Stage 1** — `gradle:8-jdk21` compiles and packages each service's JAR
- **Stage 2** — `eclipse-temurin:21-jre-alpine` runs the packaged JAR in a minimal image
- **Frontend** — `node:22-alpine` builds the Angular app; `nginx:alpine-slim` serves it

> **First build takes 10–20 minutes** (Gradle downloads dependencies). Subsequent builds are fast due to Docker layer caching.

### 5.2 Start Services in the Correct Order

Docker Compose respects `depends_on` + `healthcheck` conditions, so you can use the sequence below for safety and clarity:

#### Step 1 — Core Infrastructure

```bash
docker compose up -d postgres redis rabbitmq
```

Wait for all three to be healthy:

```bash
docker compose ps postgres redis rabbitmq
# Wait until STATUS column shows "healthy" for all three
```

#### Step 2 — Authentication, Storage & Mail

```bash
docker compose up -d keycloak minio mailhog
```

> Keycloak imports the realm on first start — allow **60–90 seconds**. Watch progress:
> ```bash
> docker compose logs -f keycloak
> # Ready when you see: "Keycloak 26.x.x on JVM ... started"
> ```

#### Step 3 — Config Server

```bash
docker compose up -d config-server
```

Wait until healthy (~60 seconds):

```bash
docker compose logs -f config-server
# Ready when you see: "Tomcat started on port(s): 8888"
```

#### Step 4 — Run Database Migrations (one-shot)

```bash
docker compose up flyway-migration
```

> This is a **one-shot container** — it runs all Flyway migrations then exits with code 0. You must wait for it to finish before proceeding.

```bash
# Verify it completed successfully
docker compose ps flyway-migration
# STATUS should show "Exited (0)"
```

#### Step 5 — All Backend Services

```bash
docker compose up -d \
  branch-service \
  donor-service \
  lab-service \
  inventory-service \
  transfusion-service \
  hospital-service \
  request-matching-service \
  billing-service \
  notification-service \
  reporting-service \
  document-service \
  compliance-service
```

#### Step 6 — API Gateway

```bash
docker compose up -d api-gateway
```

#### Step 7 — Frontend

```bash
docker compose up -d frontend
```

#### Step 8 — Monitoring Stack (optional)

```bash
docker compose up -d \
  alertmanager prometheus loki tempo promtail \
  grafana postgres-exporter redis-exporter node-exporter
```

---

### One-Command Start (after first-time setup)

Once all images have been built, you can bring up the entire stack with a single command. Docker Compose will honour the `depends_on` / `healthcheck` order automatically:

```bash
docker compose up -d
```

### 5.3 Verify Everything is Healthy

```bash
# See the status of all containers
docker compose ps

# Health check all backend services at once
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090 8091 8092; do
  echo -n "Port $port: "
  curl -s http://localhost:$port/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['status'])" 2>/dev/null || echo "UNREACHABLE"
done
```

**Full-stack URLs:**

| URL | Service |
|---|---|
| http://localhost:4200 | Frontend (Angular) |
| http://localhost:8080 | API Gateway |
| http://localhost:8180 | Keycloak Admin Console (`admin` / `admin`) |
| http://localhost:15672 | RabbitMQ Management (`guest` / `guest`) |
| http://localhost:9001 | MinIO Console (`minioadmin` / `minioadmin`) |
| http://localhost:8025 | MailHog (email catcher) |
| http://localhost:3000 | Grafana (`admin` / `bloodbank_admin`) |
| http://localhost:9090 | Prometheus |

### 5.4 Tear Down

```bash
# Stop all containers (preserves volumes/data)
docker compose down

# Stop AND remove all volumes (full reset — deletes database data)
docker compose down -v
```

---

## 6. Production — Kubernetes

> **Best for:** Production, UAT, Staging deployments. Uses blue-green deployments, HPA, SealedSecrets, and an NGINX Ingress.

The repository provides K8s manifests for four namespaces:

| Namespace | Purpose |
|---|---|
| `bloodbank-dev` | Shared dev cluster |
| `bloodbank-staging` | Integration / regression |
| `bloodbank-uat` | User acceptance testing |
| `bloodbank-prod` | Production |

> All commands below use `bloodbank-prod`. Replace with `bloodbank-dev` / `bloodbank-staging` / `bloodbank-uat` as needed.

### 6.1 Prerequisites

```bash
# Verify tools
kubectl version --client
kubeseal --version

# Point kubectl at your cluster
export KUBECONFIG=/path/to/your/kubeconfig

# Verify cluster access
kubectl cluster-info
```

### 6.2 Step-by-Step Kubernetes Deploy

#### Step 1 — Create the Namespace

```bash
kubectl apply -f k8s/namespaces/bloodbank-prod.yml

# Verify
kubectl get namespace bloodbank-prod
```

#### Step 2 — Install the Sealed Secrets Controller (one-time per cluster)

```bash
kubectl apply -f k8s/secrets/sealed-secrets-controller.yml

# Wait for the controller to be ready
kubectl rollout status deployment/sealed-secrets-controller -n kube-system
```

#### Step 3 — Generate & Apply Encrypted Secrets

```bash
# Generates encrypted SealedSecret files for each namespace
bash k8s/scripts/seal-secrets.sh bloodbank-prod

# Apply the generated sealed secrets
kubectl apply -f k8s/secrets/bloodbank-sealed-secret-prod.yml

# Verify the secret was decrypted successfully
kubectl get secret bloodbank-secrets -n bloodbank-prod
```

> **What is a SealedSecret?** It is a Kubernetes-native way to store secrets safely in Git. The `seal-secrets.sh` script encrypts your credentials using the cluster's public key so that only the cluster itself can decrypt them.

#### Step 4 — Apply ConfigMaps

```bash
kubectl apply -f k8s/configmaps/shared-config.yml
kubectl apply -f k8s/configmaps/services-config.yml

# Verify
kubectl get configmap bloodbank-shared-config -n bloodbank-prod
```

#### Step 5 — Deploy Stateful Infrastructure

```bash
kubectl apply -f k8s/statefulsets/postgres.yml
kubectl apply -f k8s/statefulsets/redis.yml
kubectl apply -f k8s/statefulsets/rabbitmq.yml

# Wait for all StatefulSets to be ready
kubectl rollout status statefulset/postgres  -n bloodbank-prod
kubectl rollout status statefulset/redis     -n bloodbank-prod
kubectl rollout status statefulset/rabbitmq  -n bloodbank-prod
```

#### Step 6 — Run Flyway Migration Job

```bash
# Set the image tag (use the tag built by your CI pipeline)
export IMAGE_TAG=v1.0.0-abc1234

# Apply the migration job (substituting the image tag)
sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/jobs/flyway-migration.yml \
  | kubectl apply -f -

# Watch until the job completes
kubectl wait --for=condition=complete job/flyway-migration \
  -n bloodbank-prod --timeout=300s

# Verify
kubectl get job flyway-migration -n bloodbank-prod
```

#### Step 7 — Deploy Config Server

```bash
sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/deployments/config-server.yml \
  | kubectl apply -f -

kubectl rollout status deployment/config-server -n bloodbank-prod
```

#### Step 8 — Deploy All Domain Services

```bash
for service in branch-service donor-service lab-service inventory-service \
               transfusion-service hospital-service request-matching-service \
               billing-service notification-service reporting-service \
               document-service compliance-service; do
  echo "Deploying $service..."
  sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/deployments/${service}.yml \
    | kubectl apply -f -
done

# Wait for all deployments to roll out
for service in branch-service donor-service lab-service inventory-service \
               transfusion-service hospital-service request-matching-service \
               billing-service notification-service reporting-service \
               document-service compliance-service; do
  kubectl rollout status deployment/${service}-blue -n bloodbank-prod
done
```

#### Step 9 — Deploy API Gateway & Frontend

```bash
sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/deployments/api-gateway.yml \
  | kubectl apply -f -
kubectl rollout status deployment/api-gateway -n bloodbank-prod

sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/deployments/frontend.yml \
  | kubectl apply -f -
kubectl rollout status deployment/frontend -n bloodbank-prod
```

#### Step 10 — Apply Services

```bash
kubectl apply -f k8s/services/ -n bloodbank-prod
```

#### Step 11 — Apply HPA (Horizontal Pod Autoscalers)

```bash
kubectl apply -f k8s/hpa/ -n bloodbank-prod
```

#### Step 12 — Apply Ingress

> **Prerequisite:** NGINX Ingress Controller must be installed in the cluster:
> ```bash
> kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/cloud/deploy.yaml
> ```

```bash
# Create TLS secret (replace with your real certificate)
kubectl create secret tls bloodbank-tls \
  --cert=tls.crt \
  --key=tls.key \
  -n bloodbank-prod

# Apply ingress (update host in the file to your domain first)
kubectl apply -f k8s/ingress/bloodbank-ingress.yml
```

### 6.3 Verify the Cluster

```bash
# All pods should be Running or Completed (for the migration job)
kubectl get pods -n bloodbank-prod

# Check all deployments are at desired replicas
kubectl get deployments -n bloodbank-prod

# Check HPAs
kubectl get hpa -n bloodbank-prod

# Check ingress
kubectl get ingress -n bloodbank-prod

# Stream logs for a specific pod
kubectl logs -f deployment/donor-service-blue -n bloodbank-prod

# Port-forward a service for local debugging
kubectl port-forward service/donor-service 8082:8082 -n bloodbank-prod
```

---

## 7. CI/CD Pipeline (Jenkins)

The `Jenkinsfile` defines an **11-stage pipeline**:

| Stage | What it does |
|---|---|
| **1. Checkout** | Git checkout, compute image tag (`vX.Y.Z-<sha>`) |
| **2. Build & Test** | `./gradlew build` — compile, test, JaCoCo coverage |
| **3. Code Quality** | SonarQube scan |
| **4. Security Scan** | OWASP Dependency-Check + Trivy + Snyk |
| **5. Build Images** | `docker compose build` → push to registry |
| **6. Dev Deploy** | Deploy to `bloodbank-dev` namespace |
| **7. Integration Tests** | Run Testcontainers integration suite |
| **8. Staging Deploy** | Deploy to `bloodbank-staging` |
| **9. Staging Tests** | Smoke tests against staging |
| **10. Prod Deploy** | Blue-green deploy to `bloodbank-prod` (requires approval) |
| **11. Post-Deploy** | Health checks, Slack notification |

**Trigger:** Any push to `main` or a release tag `v*`.

**Manual Production Approval:** Stage 10 pauses and requires a human to click "Approve" in the Jenkins UI before deploying to production.

---

## 8. Configuration Reference

### How Configuration Works

1. The **Config Server** (`backend/config-server`) serves all properties from `config-repo/`.
2. Each service fetches its configuration on startup using Spring Cloud Config.
3. Properties are layered (later overrides earlier):
   - `config-repo/application.yml` — global defaults for all services
   - `config-repo/application-dev.yml` — dev profile overrides
   - `config-repo/application-prod.yml` — prod profile overrides
   - `config-repo/{service-name}.yml` — per-service overrides

### Environment Variables

| Variable | Description | Default (dev) |
|---|---|---|
| `DB_USERNAME` | PostgreSQL username | `bloodbank` |
| `DB_PASSWORD` | PostgreSQL password | `bloodbank` |
| `RABBITMQ_USERNAME` | RabbitMQ user | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
| `MINIO_ACCESS_KEY` | MinIO access key | `minioadmin` |
| `MINIO_SECRET_KEY` | MinIO secret key | `minioadmin` |
| `ENCRYPT_KEY` | Config encryption key | `bloodbank-config-encryption-key-change-in-production` |

> **Security:** Never commit real credentials. In Kubernetes, all secrets are stored as SealedSecrets. In Docker Compose, override via a `.env` file or shell environment.

### Active Profiles

- `dev` — verbose SQL logging, DEBUG log level for `com.bloodbank`
- `prod` — no SQL logging, INFO level, stricter connection pool settings

Set the profile with: `SPRING_PROFILES_ACTIVE=dev` (or `prod`).

---

## 9. Port Reference

| Port | Service |
|---|---|
| **4200** | Frontend (Angular dev server) |
| **80** | Frontend (Nginx in Docker/Kubernetes) |
| **8080** | API Gateway |
| **8081** | branch-service |
| **8082** | donor-service |
| **8083** | lab-service |
| **8084** | inventory-service |
| **8085** | transfusion-service |
| **8086** | hospital-service |
| **8087** | request-matching-service |
| **8088** | billing-service |
| **8089** | notification-service |
| **8090** | reporting-service |
| **8091** | document-service |
| **8092** | compliance-service |
| **8888** | Config Server |
| **5432** | PostgreSQL |
| **6379** | Redis |
| **5672** | RabbitMQ (AMQP) |
| **15672** | RabbitMQ Management UI |
| **8180** | Keycloak |
| **9000** | MinIO API |
| **9001** | MinIO Console |
| **1025** | MailHog SMTP |
| **8025** | MailHog Web UI |
| **9090** | Prometheus |
| **3000** | Grafana |
| **3100** | Loki |
| **3200** | Tempo |
| **9093** | Alertmanager |
| **9187** | PostgreSQL Exporter |
| **9121** | Redis Exporter |
| **9100** | Node Exporter |

---

## 10. Troubleshooting

### Common Issues and Fixes

#### "Connection refused" to Config Server on startup

A service is trying to start before the Config Server is healthy.

```bash
# Check Config Server health
curl http://localhost:8888/actuator/health

# Check logs
docker compose logs config-server
# or
./gradlew :backend:config-server:bootRun  # wait for "Tomcat started on port 8888"
```

#### Flyway migration fails

```bash
# Check the migration container logs
docker compose logs flyway-migration

# Common fix: ensure postgres is fully ready
docker compose ps postgres   # must show "healthy"

# Re-run migration after fixing
docker compose rm flyway-migration
docker compose up flyway-migration
```

#### Keycloak 401 Unauthorized on API calls

```bash
# 1. Verify Keycloak is healthy
curl http://localhost:8180/health/ready

# 2. Check the realm was imported
# Open http://localhost:8180 → login as admin/admin
# Confirm "bloodbank" realm exists

# 3. Check the issuer-uri matches in config-repo/application.yml:
#    issuer-uri: http://localhost:8180/realms/bloodbank
```

#### RabbitMQ connection errors in services

```bash
docker compose logs rabbitmq
docker compose ps rabbitmq   # must show "healthy"

# Check credentials in docker-compose.yml match what the service expects:
# RABBITMQ_DEFAULT_USER: guest
# RABBITMQ_DEFAULT_PASS: guest
```

#### Docker Compose "service X depends on service Y, but it is not healthy"

Some services take longer to become healthy than their `start_period` allows on slow machines.

```bash
# Increase Docker resources (RAM / CPU) in Docker Desktop settings
# Then restart:
docker compose down
docker compose up -d
```

#### Gradle build fails with Java version error

```bash
java -version   # must show: openjdk 21.x.x

# If you have multiple Java versions, set JAVA_HOME:
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64  # Ubuntu
```

#### MapStruct doesn't generate implementation classes

```bash
# Force a clean build
./gradlew clean :backend:donor-service:build -x test

# Verify annotationProcessor is in build.gradle.kts:
# annotationProcessor("org.mapstruct:mapstruct-processor:...")
```

#### Kubernetes pod stuck in "Pending"

```bash
kubectl describe pod <pod-name> -n bloodbank-prod
# Look for "Insufficient memory" or "No nodes available" in Events
# Fix: check node resources or reduce resource requests in deployment YAML
```

#### Kubernetes pod stuck in "CrashLoopBackOff"

```bash
kubectl logs <pod-name> -n bloodbank-prod --previous
# Most common causes:
# - Config Server not reachable (check initContainer logs)
# - Database not ready (check Flyway job completed)
# - Missing or wrong secret keys
```

#### Angular build fails: "Cannot find module @angular/core"

```bash
cd frontend/bloodbank-ui
rm -rf node_modules package-lock.json
npm install
npm start
```

---

## 11. Quick-Reference Cheat Sheet

```bash
# ── DEVELOPMENT ─────────────────────────────────────────────────────────────

# Start just the infrastructure
docker compose up -d postgres redis rabbitmq keycloak minio mailhog

# Build all modules (skip tests)
./gradlew build -x test --parallel

# Run Config Server (must start first)
./gradlew :backend:config-server:bootRun

# Run a specific service
./gradlew :backend:donor-service:bootRun

# Run Frontend
cd frontend/bloodbank-ui && npm start

# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Validate no Lombok crept in
bash .claude/hooks/validate-no-lombok.sh

# ── DOCKER COMPOSE (FULL STACK) ──────────────────────────────────────────────

# Build all images
docker compose build

# Start everything (ordered)
docker compose up -d postgres redis rabbitmq
docker compose up -d keycloak minio mailhog
docker compose up -d config-server
docker compose up flyway-migration          # wait for exit 0
docker compose up -d branch-service donor-service lab-service \
  inventory-service transfusion-service hospital-service \
  request-matching-service billing-service notification-service \
  reporting-service document-service compliance-service
docker compose up -d api-gateway
docker compose up -d frontend

# Check health of all containers
docker compose ps

# Stream logs for a service
docker compose logs -f donor-service

# Restart a single service (after code change)
docker compose build donor-service && docker compose up -d donor-service

# Stop everything (keep data)
docker compose down

# Full reset (delete all data)
docker compose down -v

# ── KUBERNETES ────────────────────────────────────────────────────────────────

# Set context
export KUBECONFIG=/path/to/kubeconfig

# Full deploy (replace with your real tag)
export IMAGE_TAG=v1.0.0-abc1234
kubectl apply -f k8s/namespaces/bloodbank-prod.yml
kubectl apply -f k8s/secrets/bloodbank-sealed-secret-prod.yml
kubectl apply -f k8s/configmaps/
sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/statefulsets/postgres.yml  | kubectl apply -f -
sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/statefulsets/redis.yml     | kubectl apply -f -
sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/statefulsets/rabbitmq.yml  | kubectl apply -f -
sed "s/\${IMAGE_TAG}/${IMAGE_TAG}/g" k8s/jobs/flyway-migration.yml  | kubectl apply -f -
kubectl wait --for=condition=complete job/flyway-migration -n bloodbank-prod --timeout=300s

# View all pods
kubectl get pods -n bloodbank-prod

# Tail logs
kubectl logs -f deployment/donor-service-blue -n bloodbank-prod

# Port-forward for local debugging
kubectl port-forward service/donor-service 8082:8082 -n bloodbank-prod

# Roll back a deployment
kubectl rollout undo deployment/donor-service-blue -n bloodbank-prod
```

---

*For further details on Angular patterns, security roles, event contracts, or database migrations, see the other documents in the `docs/` folder and `CLAUDE.md` at the project root.*
