# 🩸 BloodBank — Worldwide Multi-Branch Microservices Blood Bank Management System

[![Java 21](https://img.shields.io/badge/Java-21_LTS-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring_Boot-3.4.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Angular 21](https://img.shields.io/badge/Angular-21-red)](https://angular.dev/)
[![PostgreSQL 17](https://img.shields.io/badge/PostgreSQL-17-blue)](https://www.postgresql.org/)
[![Keycloak 26](https://img.shields.io/badge/Keycloak-26+-purple)](https://www.keycloak.org/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.30+-blue)](https://kubernetes.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

> A production-grade, containerized microservices blood bank management system supporting **worldwide multi-branch operations**, managing the complete blood lifecycle from **Donor Registration → Blood Collection → Testing → Component Processing → Inventory Management → Cross-Matching → Issuing → Transfusion → Hemovigilance**.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Module Breakdown (24 Modules)](#module-breakdown-24-modules)
- [Microservice Mapping (14 Services)](#microservice-mapping-14-services)
- [Database Design (~87 Tables)](#database-design-87-tables)
- [User Management (16 Roles)](#user-management-16-roles)
- [Project Structure](#project-structure)
- [Code Generation Rules — No Lombok](#code-generation-rules--no-lombok)
- [RabbitMQ Event Architecture](#rabbitmq-event-architecture)
- [AI-Assisted Development System](#ai-assisted-development-system)
- [Development Milestones](#development-milestones)
- [Deployment Strategy](#deployment-strategy)
- [GitHub Copilot Prompt](#github-copilot-prompt)
- [Getting Started](#getting-started)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

BloodBank is a **24-module, 14-microservice** application designed for blood bank organizations operating across multiple countries, regions, and branches. It provides:

- **Complete Blood Lifecycle Management** — vein-to-vein traceability (donor to recipient)
- **Multi-Branch Operations** — branch-level data isolation with cross-branch transfers
- **16 User Roles** — from Super Admin to Donor self-service, with fine-grained RBAC
- **Regulatory Compliance** — HIPAA, GDPR, FDA 21 CFR Part 11, AABB, WHO standards
- **Real-Time Inventory** — stock levels, expiry alerts, emergency blood matching
- **Worldwide Market Support** — multi-language (en/es/fr), multi-currency, multi-timezone

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 (LTS) with virtual threads |
| **Backend Framework** | Spring Boot 3.4.x + Spring Cloud 2024.x |
| **Build Tool** | Gradle 8 (Kotlin DSL) |
| **Frontend** | Angular 21 (standalone components, signals, zoneless) |
| **Database** | PostgreSQL 17 (single shared DB, ~87 tables) |
| **DB Migrations** | Flyway (centralized in shared-libs/db-migration) |
| **Cache** | Redis 7 |
| **Messaging** | RabbitMQ 3.13+ (async action triggers, thin events) |
| **Identity & Access** | Keycloak 26+ (OAuth2/OIDC, LDAP federation, MFA) |
| **User Directory** | LDAP / Active Directory |
| **API Gateway** | Spring Cloud Gateway |
| **Configuration** | Spring Cloud Config Server |
| **Object Storage** | MinIO (S3-compatible) |
| **Containerization** | Docker (multi-stage builds) |
| **Orchestration** | Kubernetes 1.30+ |
| **CI/CD** | Jenkins (declarative pipeline) |
| **Resilience** | Resilience4j (circuit breaker, retry, rate limiter) |
| **Observability** | OpenTelemetry + Micrometer → Prometheus + Grafana + Loki + Tempo |
| **API Docs** | SpringDoc OpenAPI 2.x |
| **Object Mapping** | MapStruct 1.6+ (compile-time) |
| **Validation** | Jakarta Validation (Hibernate Validator) |
| **Testing** | JUnit 5 + Mockito + Testcontainers + WireMock |
| **Code Quality** | SonarQube + Checkstyle + SpotBugs |
| **Security Scanning** | Trivy + OWASP Dependency-Check + Snyk |
| **Barcode Standard** | ISBT 128 |
| **Healthcare Interop** | HL7 FHIR R4 |

> **⚠️ NO LOMBOK** — This project does not use Lombok anywhere. Java 21 records are used for DTOs and events. Entities use explicit getters/setters.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CLIENTS                                       │
│  ┌──────────┐  ┌──────────────┐  ┌────────────────┐  ┌──────────────┐ │
│  │  Staff    │  │  Hospital    │  │  Donor         │  │  Mobile      │ │
│  │  Portal   │  │  Portal      │  │  Portal        │  │  App         │ │
│  │ (Angular) │  │ (Angular)    │  │ (Angular)      │  │ (Future)     │ │
│  └─────┬─────┘  └──────┬───────┘  └───────┬────────┘  └──────┬───────┘ │
│        └────────────────┼──────────────────┼─────────────────┘         │
│                         ▼                  ▼                           │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    KEYCLOAK 26+ (SSO / OAuth2 / OIDC)            │  │
│  │                    LDAP Federation | MFA | 16 Roles              │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                              ▼                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    API GATEWAY (Spring Cloud Gateway)             │  │
│  │           JWT Validation | Rate Limiting | CORS | Routing        │  │
│  └──────────────────────────┬───────────────────────────────────────┘  │
│                              ▼                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                   14 BACKEND MICROSERVICES                       │  │
│  │                                                                  │  │
│  │  donor-service      │ inventory-service  │ lab-service           │  │
│  │  branch-service     │ transfusion-svc    │ hospital-service      │  │
│  │  billing-service    │ matching-service   │ notification-svc      │  │
│  │  reporting-service  │ document-service   �� compliance-service    │  │
│  │  api-gateway        │ config-server      │                       │  │
│  └──────────┬───────────────────┬───────────────────┬───────────────┘  │
│             │                   │                   │                   │
│             ▼                   ▼                   ▼                   │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────────┐   │
│  │ PostgreSQL 17│   │   Redis 7    │   │  RabbitMQ 3.13           │   │
│  │ Single Shared│   │   Cache      │   │  Async Events (thin IDs) │   │
│  │  DB (~87 tbl)│   │              │   │  Topic Exchange + DLQ    │   │
│  └──────────────┘   └──────────────┘   └──────────────────────────┘   │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    OBSERVABILITY STACK                            │  │
│  │  Prometheus → Grafana ← Loki (logs) ← Tempo (traces)            │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### Key Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Database Strategy | Single shared PostgreSQL DB | Simplified transactions, referential integrity, no distributed data sync complexity |
| Messaging | RabbitMQ for async actions ONLY | Not for data sync — all services query shared DB directly. Events carry only IDs (thin events) |
| Identity Management | Keycloak single realm + branch groups | Branch-level isolation via group hierarchy, simpler than multi-realm |
| Data Isolation | 4-layer enforcement | Gateway → Spring Security → JPA @Filter → DB indexes |
| Code Style | No Lombok | Java 21 records for DTOs/events, explicit getters/setters for entities |
| API Versioning | URI-based (/api/v1/) | Simple, explicit, cacheable |

---

## Module Breakdown (24 Modules)

### Core Domain Modules (16)

| # | Module | Description | Key Features |
|---|---|---|---|
| 1 | **Donor Management** | Donor lifecycle management | Registration, eligibility screening, deferral management, donation history, loyalty/rewards, health records, de-duplication |
| 2 | **Blood Collection** | Blood collection process | Bag barcode/RFID, phlebotomy recording, adverse reactions, sample tube labeling, site tracking |
| 3 | **Testing & Screening** | Laboratory testing | HIV/HBV/HCV/Syphilis/Malaria screening, blood grouping (ABO/Rh), antibody screening, NAT testing, QC, dual-review approval, quarantine/release |
| 4 | **Component Processing** | Blood separation | Whole blood → Packed RBC, FFP, Platelets, Cryoprecipitate. ISBT 128 labeling, irradiation, leukoreduction, pooling, expiry management |
| 5 | **Inventory Management** | Stock management | Real-time stock dashboard, storage location tracking, FIFO/FEFO dispatch, expiry alerts, inter-branch transfers, wastage/disposal, reconciliation, reservation/hold |
| 6 | **Cross-Match & Issuing** | Blood compatibility & issuing | Cross-match requests, ABO/Rh verification, antibody screen, unit selection (FEFO), barcode scan verification, emergency O-neg issue |
| 7 | **Transfusion Management** | Transfusion recording | Start/end recording, reaction monitoring (febrile, allergic, hemolytic, TRALI, TACO), look-back investigation, hemovigilance reporting |
| 8 | **Branch Management** | Multi-branch operations | Branch profiles, operating hours, capacity, regional hierarchy (Country → Region → City → Branch), KPI dashboard, equipment registry |
| 9 | **Blood Camp Management** | Mobile collection drives | Camp planning, resource allocation, on-site registration (offline-capable), execution tracking, post-camp follow-up |
| 10 | **Hospital/Client Management** | Hospital relations | Hospital registration, contracts/SLA, request portal, demand forecasting, feedback, credit management |
| 11 | **Billing & Invoicing** | Financial management | Component-wise pricing, tiered rates, processing fees, auto-invoice generation, payment tracking, multi-currency, GST/VAT |
| 12 | **Compliance & Regulatory** | Regulatory compliance | Configurable per country (FDA/EU/WHO/AABB), SOP management, license tracking, consent management (GDPR), deviation/CAPA, recall management |
| 13 | **Reporting & Analytics** | Business intelligence | Operational reports, regulatory reports (WHO/AABB/FDA format), management dashboards, demand vs supply analytics, custom report builder, benchmarking |
| 14 | **Communication & Notifications** | Multi-channel messaging | Email, SMS, push notifications, WhatsApp, multi-language templates, per-user preferences, opt-out, broadcast campaigns |
| 15 | **User & Access Management** | Identity & authorization | 16 roles, RBAC, LDAP/AD integration, SSO (Keycloak OIDC), MFA, session management, user activity logging |
| 16 | **System Administration** | System configuration | Tenant configuration, feature flags, system settings, scheduled jobs, backup/recovery, health dashboard |

### Supporting / Cross-Cutting Modules (8)

| # | Module | Description |
|---|---|---|
| 17 | **Master Data Management** | Blood groups, component types, test panels, deferral reasons, reaction types, countries/regions/cities, ICD codes, ISBT 128 codes |
| 18 | **Audit & Traceability** | Complete chain of custody, immutable audit log, digital signatures, vein-to-vein traceability, data retention policy |
| 19 | **Document Management** | Consent forms, SOP documents, lab reports, certificates — stored in MinIO/S3 |
| 20 | **Dashboard & Real-Time Monitoring** | National overview, branch dashboard, lab dashboard, inventory dashboard, hospital portal dashboard, donor dashboard |
| 21 | **Integration & Interoperability** | HIS (HL7 FHIR R4), LIS (ASTM/HL7), SMS/email gateways, payment gateways, government ID verification, ERP export |
| 22 | **Logistics & Cold Chain** | Transport scheduling, IoT temperature logging, GPS tracking, transport box management, delivery confirmation |
| 23 | **Emergency & Disaster Response** | Emergency blood request, mass casualty protocol, donor mobilization, emergency stock rebalancing |
| 24 | **Donor Portal & Mobile App** | Self-registration, appointment booking, donation history, eligibility self-check, nearby camp finder, digital donor card, referral program |

### Complete Blood Lifecycle Flow

```
DONOR JOURNEY                          BLOOD UNIT JOURNEY
─────────────                          ──────────────────

 Registration                           Collection
     │                                      │
 Eligibility Screening                  Barcode/RFID Assignment
     │                                      │
 Appointment / Walk-in / Camp           Sample Separation
     │                                      │
 Health Check (Vitals)                  ┌────┴────┐
     │                                  │         │
 Consent & Collection                Testing   Component
     │                              (Lab Svc)  Processing
 Post-Donation Care                     │         │
     │                              Quarantine    │
 Thank You / Loyalty Points             │         │
     │                              Results ──── Release
 Next Eligible Date                     │
                                    ┌───┴────────────────────┐
                                    │                        │
                                 Hospital                 Transfer
                                 Request                 (Branch→Branch)
                                    │
                                Cross-Match
                                    │
                                 Issuing
                                    │
                                Transfusion
                                    │
                             ┌──────┴──────┐
                             │             │
                          Outcome      Reaction?
                          Tracking     → Hemovigilance
                                       → Look-Back
```

---

## Microservice Mapping (14 Services)

| Microservice | Modules Handled | Key Entities | Events |
|---|---|---|---|
| **donor-service** | 1, 2, 9, 24 | Donor, DonorHealthRecord, DonorDeferral, DonorConsent, DonorLoyalty, Collection, CollectionAdverseReaction, CollectionSample, BloodCamp, CampResource, CampDonor, CampCollection | Publishes: DonationCompleted, CampCompleted |
| **inventory-service** | 4, 5, 22 | BloodUnit, BloodComponent, ComponentProcessing, ComponentLabel, PooledComponent, StorageLocation, StockTransfer, UnitDisposal, UnitReservation, TransportRequest, ColdChainLog, TransportBox, DeliveryConfirmation | Publishes: StockUpdated, StockCritical, UnitExpiring. Listens: DonationCompleted |
| **lab-service** | 3 | TestOrder, TestResult, TestPanel, LabInstrument, QualityControlRecord | Publishes: TestResultAvailable, UnitReleased |
| **branch-service** | 8, 17 | Branch, BranchOperatingHours, BranchEquipment, BranchRegion, BloodGroup, ComponentType, DeferralReason, ReactionType, Country, Region, City, IcdCode | None (read-heavy, Redis cached) |
| **transfusion-service** | 6, 7 | CrossMatchRequest, CrossMatchResult, BloodIssue, EmergencyIssue, Transfusion, TransfusionReaction, HemovigilanceReport, LookBackInvestigation | Publishes: TransfusionCompleted, TransfusionReaction |
| **hospital-service** | 10 | Hospital, HospitalContract, HospitalRequest, HospitalFeedback | Publishes: BloodRequestCreated |
| **billing-service** | 11 | RateMaster, Invoice, InvoiceLineItem, Payment, CreditNote | Publishes: InvoiceGenerated. Listens: BloodRequestMatched |
| **request-matching-service** | 6(matching), 23 | EmergencyRequest, DisasterEvent, DonorMobilization | Publishes: BloodRequestMatched, EmergencyRequest. Listens: StockUpdated, BloodRequestCreated |
| **notification-service** | 14 | Notification, NotificationTemplate, NotificationPreference, Campaign | Listens: ALL domain events |
| **reporting-service** | 13, 18, 20 | AuditLog, DigitalSignature, ChainOfCustody, ReportMetadata, ReportSchedule, DashboardWidget | Listens: ALL events for audit trail |
| **document-service** | 19 | Document, DocumentVersion | None (MinIO integration) |
| **compliance-service** | 12 | RegulatoryFramework, SopDocument, License, Deviation, RecallRecord | Publishes: RecallInitiated |
| **api-gateway** | — | — | JWT validation, rate limiting, routing, CORS |
| **config-server** | — | — | Centralized configuration distribution |

---

## Database Design (~87 Tables)

### Single Shared Database Architecture

- **All 14 services connect to ONE PostgreSQL 17 database** (`bloodbank_db`)
- Flyway migrations centralized in `shared-libs/db-migration` (20 scripts: V1-V20)
- K8s Job runs Flyway BEFORE any service starts
- Each service: `spring.flyway.enabled=false`
- All tables include audit columns via `BaseEntity`: `id` (UUID), `created_at`, `updated_at`, `created_by`, `updated_by`, `version`
- All branch-scoped tables include `branch_id` column with Hibernate `@Filter`
- `audit_logs` table is append-only (DB trigger prevents UPDATE/DELETE)

### Table Summary by Domain

| Domain | Tables | Count |
|---|---|---|
| Master Data | blood_groups, component_types, deferral_reasons, reaction_types, countries, regions, cities, icd_codes | 8 |
| Branch | branches, branch_operating_hours, branch_equipment, branch_regions | 4 |
| Donor | donors, donor_health_records, donor_deferrals, donor_consents, donor_loyalty | 5 |
| Collection | collections, collection_adverse_reactions, collection_samples | 3 |
| Blood Camp | blood_camps, camp_resources, camp_donors, camp_collections | 4 |
| Testing | test_orders, test_results, test_panels, lab_instruments, quality_control_records | 5 |
| Inventory | blood_units, blood_components, component_processing, component_labels, pooled_components, storage_locations, stock_transfers, unit_disposals, unit_reservations | 9 |
| Transfusion | crossmatch_requests, crossmatch_results, blood_issues, emergency_issues, transfusions, transfusion_reactions, hemovigilance_reports, lookback_investigations | 8 |
| Hospital | hospitals, hospital_contracts, hospital_requests, hospital_feedback | 4 |
| Billing | rate_master, invoices, invoice_line_items, payments, credit_notes | 5 |
| Notification | notifications, notification_templates, notification_preferences, campaigns | 4 |
| Compliance | regulatory_frameworks, sop_documents, licenses, deviations, recall_records | 5 |
| Reporting | audit_logs, digital_signatures, chain_of_custody, report_metadata, report_schedules, dashboard_widgets | 6 |
| Document | documents, document_versions | 2 |
| Logistics | transport_requests, cold_chain_logs, transport_boxes, delivery_confirmations | 4 |
| Emergency | emergency_requests, disaster_events, donor_mobilizations | 3 |
| User Management | user_profiles, user_branch_assignments, user_activity_logs, user_sessions, role_change_audit | 5 |
| System | system_settings, feature_flags, scheduled_jobs, tenant_configs | 4 |
| | | **~87** |

### Flyway Migration Scripts

| Script | Description |
|---|---|
| V1 | Base master tables (blood_groups, component_types, countries, etc.) |
| V2 | Branch tables |
| V3 | Donor tables |
| V4 | Collection tables |
| V5 | Blood camp tables |
| V6 | Lab tables |
| V7 | Inventory tables |
| V8 | Transfusion tables |
| V9 | Hospital tables |
| V10 | Billing tables |
| V11 | Notification tables |
| V12 | Compliance tables |
| V13 | Reporting tables (audit_logs append-only trigger) |
| V14 | Document tables |
| V15 | Logistics tables |
| V16 | Emergency tables |
| V17 | User management tables |
| V18 | System tables |
| V19 | All indexes (FKs, branch_id, status, created_at, composites) |
| V20 | Seed reference data (blood groups, test panels, countries, templates) |

---

## User Management (16 Roles)

### Role Hierarchy

```
                        ┌─────────────────┐
                        │   SUPER_ADMIN    │
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                   ▼
     ┌────────────────┐ ┌───────────────┐  ┌─────────────────┐
     │ REGIONAL_ADMIN  │ │ SYSTEM_ADMIN  │  │  AUDITOR         │
     └───────┬────────┘ └───────────────┘  └─────────────────┘
             │
     ┌───────┴────────┐
     ▼                ▼
┌──────────────┐ ┌──────────────────┐
│ BRANCH_ADMIN │ │ BRANCH_MANAGER   │
└──────┬───────┘ └────────┬─────────┘
       │                  │
       │    ┌─────────────┼──────────────────┬──────────────────┐
       │    ▼             ▼                  ▼                  ▼
       │ ┌────────────┐ ┌───────────────┐ ┌──────────────┐ ┌──────────────┐
       │ │  DOCTOR     │ │LAB_TECHNICIAN │ │PHLEBOTOMIST  │ │INVENTORY_MGR │
       │ └────────────┘ └───────────────┘ └──────────────┘ └──────────────┘
       │
       │ ┌────────────┐ ┌───────────────┐ ┌──────────────┐ ┌──────────────┐
       └▶│  NURSE      │ │BILLING_CLERK  │ │CAMP_COORD    │ │RECEPTIONIST  │
         └────────────┘ └───────────────┘ └──────────────┘ └──────────────┘

External:  HOSPITAL_USER  │  DONOR
```

### Role Definitions

| # | Role | Type | Scope | Description |
|---|---|---|---|---|
| 1 | SUPER_ADMIN | Realm | Global | Full system access, tenant management |
| 2 | REGIONAL_ADMIN | Realm | Region | Manages all branches in assigned region |
| 3 | SYSTEM_ADMIN | Realm | Global | Technical admin — settings, flags, monitoring |
| 4 | AUDITOR | Realm | Global | Read-only access to all modules for compliance |
| 5 | BRANCH_ADMIN | Client | Branch | Full access within assigned branch |
| 6 | BRANCH_MANAGER | Client | Branch | Operational manager, approvals |
| 7 | DOCTOR | Client | Branch | Blood requests, cross-match, transfusions |
| 8 | LAB_TECHNICIAN | Client | Branch | Test orders, results, QC, quarantine/release |
| 9 | PHLEBOTOMIST | Client | Branch | Blood collection, donor vitals |
| 10 | NURSE | Client | Branch | Transfusion administration, reactions |
| 11 | INVENTORY_MANAGER | Client | Branch | Stock, storage, transfers, disposal |
| 12 | BILLING_CLERK | Client | Branch | Invoices, payments, rate master |
| 13 | CAMP_COORDINATOR | Client | Branch | Blood camp planning and execution |
| 14 | RECEPTIONIST | Client | Branch | Donor walk-in registration, appointments |
| 15 | HOSPITAL_USER | Client | Hospital | Submit blood requests, track status |
| 16 | DONOR | Client | Self | Self-service portal (own data only) |

### Keycloak Configuration

- **Realm:** `bloodbank` (single realm, branch separation via groups)
- **Clients:** `bloodbank-api` (confidential), `bloodbank-ui` (public PKCE)
- **Group Hierarchy:** `/global`, `/regions/{region}/{branch}`, `/hospitals/{hospital}`
- **LDAP:** READ_ONLY federation, LDAPS port 636, full sync daily + changed every 5 min
- **MFA:** Required for admin roles, optional for clinical, not required for donors
- **Sessions:** Admin 30min idle / 8hr max, Clinical 60min idle / 12hr max, Donor 24hr max
- **Password:** Min 12 chars, upper+lower+number+special, not in last 12, max age 90 days

### 4-Layer Branch Data Isolation

| Layer | Mechanism | Description |
|---|---|---|
| 1. API Gateway | `BranchIdExtractionFilter` | Extracts `branch_id` from JWT → sets `X-Branch-Id` header |
| 2. Spring Security | `@PreAuthorize` | Role-based access on every controller method |
| 3. JPA Data Filtering | `BranchDataFilterAspect` (AOP) | Auto-enables Hibernate `@Filter("branchFilter")` based on role scope |
| 4. Database | `branch_id` column + indexes | Composite indexes on all branch-scoped tables |

### Security Policies

| Policy | Details |
|---|---|
| Dual Authorization | Test result release, blood issuing, disposal require supervisor approval |
| Break-Glass Access | DOCTOR emergency override — fully audited, time-limited |
| Data Masking | Donor PII masked for non-authorized roles via `DataMaskingAspect` |
| Separation of Duties | Collector ≠ Tester ≠ Releaser — enforced via role separation |
| Immutable Audit Trail | `audit_logs` table INSERT only — DB trigger prevents UPDATE/DELETE |
| GDPR Erasure | Anonymization workflow — replace PII with hashed values, retain donation records |
| Access Reviews | Quarterly review, dormant accounts flagged (no login > 90 days) |

---

## Project Structure

```
BloodBank/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── docker-compose.yml
├── Jenkinsfile
├── README.md
│
├── backend/
│   ├── api-gateway/
│   ├── config-server/
│   ├── donor-service/
│   ├── inventory-service/
│   ├── lab-service/
│   ├── branch-service/
│   ├── transfusion-service/
│   ├── hospital-service/
│   ├── billing-service/
│   ├── request-matching-service/
│   ├── notification-service/
│   ├── reporting-service/
│   ├── document-service/
│   └── compliance-service/
│
├── shared-libs/
│   ├── common-dto/
│   ├── common-security/
│   ├── common-events/
│   ├── common-exceptions/
│   ├── common-model/
│   └── db-migration/
│
├── frontend/
│   └── bloodbank-ui/            # Angular 21
│
├── keycloak/
│   ├── realm-export.json
│   ├── ldap-federation-config.json
│   └── README.md
│
├── k8s/
│   ├── namespaces/
│   ├── deployments/
│   ├── services/
│   ├── ingress/
│   ├── configmaps/
│   ├── secrets/
│   ├── hpa/
│   ├── statefulsets/
│   └── jobs/
│
├── monitoring/
│   ├── prometheus/
│   ├── grafana/
│   ├── loki/
│   ├── tempo/
│   └── alertmanager/
│
└── docs/
    ├── architecture/
    ├── requirements/
    ├── database/
    ├── security/
    ├── ui-design/
    ├── api-contracts/
    ├── compliance/
    ├── sre/
    ├── release-management/
    └── runbooks/
```

---

## Code Generation Rules — No Lombok

This project does **NOT** use Lombok. The following patterns are mandatory:

### DTOs — Java 21 Records

```java
public record DonorCreateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull BloodGroupEnum bloodGroup,
    @Email String email,
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$") String phone,
    @NotNull UUID branchId
) {}

public record DonorResponse(
    UUID id, String firstName, String lastName,
    BloodGroupEnum bloodGroup, String email,
    String phone, UUID branchId, LocalDateTime createdAt
) {}
```

### Events — Java 21 Records

```java
public record DonationCompletedEvent(
    Long donationId, Long donorId, Long branchId, Instant occurredAt
) {}
```

### Entities — Explicit Getters/Setters

```java
@Entity
@Table(name = "donors")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = String.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Donor extends BranchScopedEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    protected Donor() {} // JPA required

    public Donor(String firstName, String lastName, BloodGroupEnum bloodGroup) {
        this.firstName = firstName;
        // ...
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    // ... all getters/setters explicit
}
```

### Services — Constructor Injection

```java
@Service
@Transactional(readOnly = true)
public class DonorService {
    private static final Logger log = LoggerFactory.getLogger(DonorService.class);
    private final DonorRepository donorRepository;
    private final DonorMapper donorMapper;

    public DonorService(DonorRepository donorRepository, DonorMapper donorMapper) {
        this.donorRepository = donorRepository;
        this.donorMapper = donorMapper;
    }
}
```

---

## RabbitMQ Event Architecture

### Design Principle

Since all services share one database, RabbitMQ is used **ONLY** for:
1. **Async task offloading** — notifications, audit logging, report generation
2. **Workflow triggers** — donation → inventory update, test result → unit release
3. **Emergency broadcasts** — urgent blood requests, disaster mobilization
4. **Scheduled job coordination** — expiry checks, stock rebalancing

### Configuration

| Setting | Value |
|---|---|
| Topic Exchange | `bloodbank.events` |
| Dead Letter Exchange | `bloodbank.dlx` → `bloodbank.dlq` |
| Retry | 3 attempts, 1s initial backoff, 2x multiplier |
| Serialization | Jackson JSON with type headers |
| Payload | **Thin events — IDs only, NO data duplication** |

### 15 Event Types

| Event | Publisher | Consumer(s) |
|---|---|---|
| DonationCompletedEvent | donor-service | inventory-service, notification-service, reporting-service |
| CampCompletedEvent | donor-service | notification-service, reporting-service |
| TestResultAvailableEvent | lab-service | inventory-service, notification-service |
| UnitReleasedEvent | lab-service | inventory-service, reporting-service |
| BloodStockUpdatedEvent | inventory-service | request-matching-service, reporting-service |
| StockCriticalEvent | inventory-service | notification-service, request-matching-service |
| UnitExpiringEvent | inventory-service | notification-service |
| BloodRequestCreatedEvent | hospital-service | request-matching-service, notification-service |
| BloodRequestMatchedEvent | request-matching-service | billing-service, notification-service |
| EmergencyRequestEvent | request-matching-service | notification-service, donor-service |
| TransfusionCompletedEvent | transfusion-service | reporting-service |
| TransfusionReactionEvent | transfusion-service | notification-service, reporting-service |
| InvoiceGeneratedEvent | billing-service | notification-service |
| RecallInitiatedEvent | compliance-service | notification-service, reporting-service |
| (All events) | Various | reporting-service (audit trail) |

---

## AI-Assisted Development System

This project uses AI agents for code generation, validation, and development workflow automation.

### What's Implemented Today

The following tools are **available and working** in `.claude/`:

| Tool | Location | Description |
|---|---|---|
| 13 Skills | `.claude/skills/` | Reusable code generation templates (entity, DTO, controller, service, mapper, repository, event, migration, Angular feature, unit test, integration test, Docker/K8s, scaffold-service) |
| 6 Commands | `.claude/commands/` | Higher-level CLI wrappers: `scaffold-service`, `create-entity`, `add-event`, `add-flyway-migration`, `add-angular-feature`, `validate-project` |
| 3 Hooks | `.claude/hooks/` | Automated validation: `validate-no-lombok.sh` (PostToolCall), `validate-code-patterns.sh` (PostToolCall), `pre-push-checks.sh` (PrePush) |
| Configuration | `.claude/settings.json` | Permissions, hook triggers, project metadata |

**Usage**: Invoke commands manually (e.g., `/project:scaffold-service donor-service`) or use skills as templates during AI-assisted coding sessions.

### Agent Roles (20 Consolidated Agents)

> **Note**: These are **role definitions** that guide AI agent behavior during development. They are not a running orchestration engine — they describe *what each role does* when invoked via the skills and commands listed above. To use a role, invoke the matching skill/command (e.g., run `/project:scaffold-service donor-service` to execute SA-09's Backend Dev role for donor-service).

| Phase | Agent | Role | Responsibility | Skill(s) Used |
|---|---|---|---|---|
| **PLANNING** | SA-01 | 🏗️ Architect | System architecture, C4 diagrams, ADRs | — (documentation) |
| | SA-02 | 📋 Requirements | Requirements, user stories, use cases, traceability | — (documentation) |
| | SA-03 | 🎨 UI/UX Designer | Design system, page specs, navigation, accessibility | — (documentation) |
| | SA-04 | 📐 API Designer | OpenAPI specs, event contracts, API versioning | — (documentation) |
| | SA-05 | 🗄️ DBA | Schema design, Flyway migrations, ERD, indexing | `create-flyway-migration`, `create-entity` |
| | SA-06 | 🔐 Security Architect | Threat model, RBAC matrix, compliance mapping | — (documentation) |
| **DEVELOPMENT** | SA-07 | ⚙️ Build Engineer | Gradle build system, project skeleton, code quality | `scaffold-service` |
| | SA-08 | 📦 Platform Developer | Shared libraries (DTOs, events, security, exceptions) | `create-dto-record`, `create-rabbitmq-event` |
| | SA-09 | 💻 Backend Dev | **All 12 backend services** (parameterized by service name + module list) | `scaffold-service`, `create-entity`, `create-dto-record`, `create-rest-controller`, `create-service-class`, `create-repository`, `create-mapstruct-mapper`, `create-rabbitmq-event` |
| | SA-10 | 🌐 Frontend Dev | Angular 21 — all 17 feature modules, core, shared | `create-angular-feature` |
| | SA-11 | 🔌 Integration Dev | API Gateway, Config Server, service wiring | `scaffold-service` |
| **TESTING** | SA-12 | 🧪 Unit Tester | JUnit 5 + Mockito for all services (>80% coverage) | `create-unit-test` |
| | SA-13 | 🔗 Integration Tester | Testcontainers (PostgreSQL, Redis, RabbitMQ) | `create-integration-test` |
| | SA-14 | 🌊 E2E/Perf/Security Tester | Playwright/Cypress E2E, Gatling/k6 load tests, OWASP ZAP, WCAG a11y | — (test frameworks) |
| **DEVOPS** | SA-15 | 🐳 Docker + K8s Engineer | Dockerfiles, docker-compose, K8s manifests, Helm | `create-docker-k8s` |
| | SA-16 | 🔧 CI/CD Engineer | Jenkinsfile, SonarQube, security scan pipeline | — (pipeline config) |
| | SA-17 | 📊 Monitoring Engineer | Prometheus, Grafana, Loki, Tempo, alert rules | — (config files) |
| | SA-18 | 🔑 IAM Engineer | Keycloak realm-export.json, LDAP federation config | — (Keycloak config) |
| **DOCS & OPS** | SA-19 | 📝 Technical Writer | README, developer guides, user guides, compliance docs, SOPs, runbooks | — (documentation) |
| | SA-20 | 🚀 Release + Deploy | Versioning, release process, deploy scripts, blue-green/canary, SLOs | — (scripts/config) |

#### Service Parameter Table (for SA-09 Backend Dev)

When SA-09 is invoked, it takes a `{service-name}` parameter and looks up the modules. Invoke with: `/project:scaffold-service {service-name}` or `/project:create-entity {service-name} {Entity} {table} [scope]`.

| Service Name | Modules | Key Tables |
|---|---|---|
| donor-service | 1, 2, 9, 24 | donors, donor_health_records, collections, blood_camps |
| inventory-service | 4, 5, 22 | blood_units, blood_components, storage_locations, transport_requests |
| lab-service | 3 | test_orders, test_results, test_panels, lab_instruments |
| branch-service | 8, 17 | branches, branch_operating_hours, blood_groups, component_types |
| transfusion-service | 6, 7 | crossmatch_requests, blood_issues, transfusions, hemovigilance_reports |
| hospital-service | 10 | hospitals, hospital_contracts, hospital_requests |
| billing-service | 11 | rate_master, invoices, payments, credit_notes |
| request-matching-service | 6(matching), 23 | emergency_requests, disaster_events, donor_mobilizations |
| notification-service | 14 | notifications, notification_templates, campaigns |
| reporting-service | 13, 18, 20 | audit_logs, report_metadata, dashboard_widgets |
| document-service | 19 | documents, document_versions |
| compliance-service | 12 | regulatory_frameworks, sop_documents, deviations, recall_records |

### Task Classification (Intent-Based Routing)

Instead of fragile keyword matching, tasks are classified by **intent type**:

| Intent | Description | Agent(s) Invoked | Example Input |
|---|---|---|---|
| `NEW_FEATURE` | Add a new capability, entity, or endpoint | SA-05 → SA-09 → SA-12/SA-13 | "Add donor loyalty points feature" |
| `NEW_SERVICE` | Scaffold an entire microservice | SA-07 → SA-08 → SA-05 → SA-09 | "Create the inventory-service" |
| `BUG_FIX` | Fix an existing defect | SA-09 (target service) → SA-12 | "Fix 500 error on POST /api/v1/donors" |
| `REFACTOR` | Improve existing code without changing behavior | SA-09 → SA-12/SA-13 | "Refactor donor-service to use specifications" |
| `FRONTEND` | UI components, pages, or Angular features | SA-10 | "Add donor registration page" |
| `INFRA_CHANGE` | Docker, K8s, CI/CD, monitoring | SA-15/SA-16/SA-17 | "Write Kubernetes manifests" |
| `SECURITY` | Roles, permissions, IAM, vulnerability fixes | SA-06 → SA-18 → SA-09 | "Billing returns 403 for BILLING_CLERK" |
| `DATABASE` | Schema changes, migrations, indexing | SA-05 | "Add index on donors.blood_group" |
| `TESTING` | Write or fix tests | SA-12/SA-13/SA-14 | "Add integration tests for lab-service" |
| `DOCUMENTATION` | Docs, runbooks, compliance artifacts | SA-19 | "Write developer guide for donor-service" |
| `FULL_SCAFFOLD` | Generate the entire project end-to-end | ALL agents in tier order | "Scaffold the entire project" |

### Dependency Tiers (Execution Order)

```
TIER 0: SA-01 (Architect), SA-02 (Requirements)                 ← Start immediately
TIER 1: SA-05 (DBA), SA-06 (Security), SA-03 (UI/UX), SA-04 (API) ← After Tier 0
TIER 2: SA-07 (Build), SA-08 (Platform), SA-18 (IAM)            ← After Tier 1 (IAM needs Security)
TIER 3: SA-09 (Backend Dev ×12), SA-10 (Frontend)               ← After Tier 2 (parallel)
TIER 4: SA-11 (Integration Dev)                                  ← After Tier 3
TIER 5: SA-12–SA-14 (Testing), SA-15–SA-17 (DevOps), SA-19 (Docs) ← After Tier 3-4 (parallel)
TIER 6: SA-20 (Release + Deploy)                                 ← After Tier 5
```

### Validation & Feedback Loop

Generated code is validated automatically. If validation fails, the generating agent is re-invoked with error context:

```
Agent generates code
    ↓
validate-no-lombok.sh ──→ FAIL? ──→ Re-invoke agent with: "Remove Lombok, use records/explicit getters"
    ↓ PASS
validate-code-patterns.sh ──→ FAIL? ──→ Re-invoke agent with: "Add @PreAuthorize / fix injection / etc."
    ↓ PASS
Output accepted
```

### State Tracking (Planned)

> **Status**: Not yet implemented. When built, state will be tracked in `.claude/state.json`:

```json
{
  "generated_services": ["donor-service", "branch-service"],
  "pending_services": ["inventory-service", "lab-service"],
  "failed_services": [],
  "last_migration_version": "V001",
  "completed_tiers": [0, 1, 2]
}
```

### Example Orchestration Scenarios

| User Input | Intent | Agent Flow |
|---|---|---|
| "Generate the complete project" | `FULL_SCAFFOLD` | ALL agents, Tier 0 → Tier 6 |
| "Create the inventory-service" | `NEW_SERVICE` | SA-07 → SA-08 → SA-05 → SA-09(`inventory-service`) |
| "Write Kubernetes manifests" | `INFRA_CHANGE` | SA-15 (Docker + K8s) |
| "Add Hindi language support" | `NEW_FEATURE` | SA-10 (add hi.json) → SA-09 (add messages_hi.properties ×12) |
| "Billing returns 403 for BILLING_CLERK" | `BUG_FIX` | SA-06 (review RBAC) → SA-09(`billing-service`) → SA-12 (regression test) |
| "Prepare for production" | `INFRA_CHANGE` | SA-15 → SA-20 → SA-17 → SA-19 |

---

## Development Milestones

| Milestone | Duration | Deliverable | Exit Gate |
|---|---|---|---|
| **M0:** Project Setup | 2 weeks | Architecture docs signed off, agent issues created | Stakeholder approval |
| **M1:** Foundation | 2 weeks | Gradle builds, 87 tables created, shared libs compile | Build + Flyway success |
| **M2:** Core Services | 2 weeks | donor, branch, lab, inventory services running | Unit + integration tests pass |
| **M3:** Clinical Services | 2 weeks | transfusion, hospital, matching services running | Clinical workflow test pass |
| **M4:** Support Services | 2 weeks | billing, notification, reporting, document, compliance | All events consumed correctly |
| **M5:** Gateway + Frontend | 3 weeks | Full UI with SSO, 17 features, 3 portals | All features render per role |
| **M6:** Integration + Security | 2 weeks | Full lifecycle tested, pentest completed | Security sign-off |
| **M7:** Infrastructure | 2 weeks | Docker, K8s, Jenkins, Keycloak, monitoring | Pipeline deploys to dev |
| **M8:** Performance Testing | 2 weeks | Load tested at 1000 concurrent, optimized | Performance targets met |
| **M9:** UAT + Compliance | 2 weeks | UAT signed off, compliance validated | Regulatory sign-off |
| **M10:** Pilot (1 branch) | 2 weeks | Single branch live + hypercare | Pilot sign-off |
| **M11:** Regional Rollout | 4 weeks | All branches live in batches | Regional sign-off |
| **M12:** Worldwide Launch | 1 week | Full production, public portals live | Go-live approval |
| **M13:** Post-Launch | Ongoing | Continuous improvement, monitoring | Quarterly reviews |
| | **~28 weeks** | | |

### Performance Targets

| Metric | Target |
|---|---|
| API Response Time (P95) | < 200ms |
| API Response Time (P99) | < 500ms |
| Throughput | 500 requests/sec sustained |
| Database Query (P95) | < 100ms |
| Uptime | 99.9% |
| Recovery Time (RTO) | < 15 minutes |
| Recovery Point (RPO) | < 5 minutes |
| Deployment Downtime | Zero (rolling/blue-green) |

---

## Deployment Strategy

### Environment Pipeline

```
Developer ──▶ Feature Branch ──▶ Pull Request (CI) ──▶ DEV ──▶ STAGING ──▶ UAT ──▶ PRODUCTION
                                                       (auto)    (auto)   (manual)  (manual)
```

| Environment | Namespace | Replicas | Deploy Trigger |
|---|---|---|---|
| DEV | `bloodbank-dev` | 1 per service | Auto on merge to `main` |
| STAGING | `bloodbank-staging` | 2 per service | Auto after dev tests pass |
| UAT | `bloodbank-uat` | 2 per service | Manual approval |
| PRODUCTION | `bloodbank-prod` | 2-10 per service (HPA) | Manual approval |

### Deployment Strategy per Service

| Service | Strategy | Reason |
|---|---|---|
| donor-service | 🔵 Blue-Green | High impact — registration must not fail |
| inventory-service | 🔵 Blue-Green | Critical — stock data consistency |
| lab-service | 🔵 Blue-Green | Regulatory — test results cannot be lost |
| transfusion-service | 🔵 Blue-Green | Life-critical |
| api-gateway | 🔵 Blue-Green | Entry point — zero downtime |
| frontend | 🔵 Blue-Green | User-facing — seamless switch |
| request-matching-service | 🟡 Canary (10%→50%→100%) | Algorithm changes need gradual validation |
| billing-service | 🟡 Canary (10%→50%→100%) | Financial — verify calculations first |
| All other services | 🔄 Rolling Update | Lower risk, tolerate brief inconsistency |

### Jenkins Pipeline (11 Stages)

1. Checkout source code
2. Gradle build (all modules)
3. Unit tests + JaCoCo coverage (>80% threshold)
4. SonarQube analysis (quality gate)
5. Security scan (Trivy + OWASP + Snyk)
6. Docker build & push (all images, tagged Git SHA + semver)
7. Flyway migration K8s Job
8. Deploy to DEV (automatic)
9. Integration tests against DEV
10. Deploy to STAGING (automatic)
11. Deploy to PRODUCTION (manual approval + per-service strategy)

### Disaster Recovery

| Metric | Target |
|---|---|
| RPO | < 5 minutes (streaming replication) |
| RTO | < 15 minutes (auto-failover + DNS switch) |
| DR Drills | Quarterly |
| Backup | Full daily, incremental every 6 hours, WAL archiving |
| Cross-Region | Standby cluster in separate region |

---

## GitHub Copilot Prompt

The complete GitHub Copilot prompt for scaffolding this project is maintained as part of the architecture documentation. It includes:

- Full project structure with every file path
- All 14 services with entities, controllers, services, DTOs, events
- All 6 shared libraries with complete class definitions
- All 20 Flyway migration scripts
- Angular 21 frontend with 17 feature modules
- Keycloak realm configuration
- Docker Compose for local development
- Kubernetes manifests
- Jenkinsfile
- No-Lombok code patterns with examples

Use it with Copilot Coding Agent by creating a GitHub Issue with the prompt, or iteratively in Copilot Chat.

---

## Getting Started

### Prerequisites

- Java 21 (LTS)
- Gradle 8.x
- Node.js 22.x
- Docker & Docker Compose
- kubectl (for K8s deployment)

### Local Development (Docker Compose)

```bash
# Clone the repository
git clone https://github.com/rasterwebapps/BloodBank.git
cd BloodBank

# Start all infrastructure + services
docker-compose up -d

# Services available at:
# Frontend:    http://localhost:4200
# API Gateway: http://localhost:8080
# Keycloak:    http://localhost:8180
# RabbitMQ:    http://localhost:15672
# MinIO:       http://localhost:9001
# Mailhog:     http://localhost:8025

# Default admin login (Keycloak):
# Username: admin
# Password: admin
```

### Build from Source

```bash
# Build all modules
./gradlew build

# Run a specific service
./gradlew :backend:donor-service:bootRun

# Run tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Follow the [Code Generation Rules](#code-generation-rules--no-lombok) — **no Lombok**
4. Write tests (unit + integration) with >80% coverage
5. Commit with conventional commits (`feat:`, `fix:`, `docs:`, `chore:`)
6. Open a Pull Request against `main`

### Code Standards

- Java 21 records for all DTOs and events
- No Lombok annotations — explicit getters/setters on entities
- Constructor injection — no `@RequiredArgsConstructor`
- `LoggerFactory.getLogger()` — no `@Slf4j`
- `@PreAuthorize` on every controller method
- `@Filter(name="branchFilter")` on all branch-scoped entities
- API prefix: `/api/v1/`
- Response wrapper: `ApiResponse<T>`

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ for blood banks worldwide**