# BloodBank — Future Enhancement Roadmap

**Last Updated**: 2026-04-24
**Milestone**: M13 — Post-Launch & Continuous Improvement
**Issues**: M13-017 to M13-028
**Review Cadence**: Quarterly

---

## Overview

This document captures the prioritised backlog of post-launch feature enhancements for
BloodBank. Each initiative includes a brief scope summary, key technical considerations,
estimated effort tier (S / M / L / XL), and a target quarter for planning purposes.

Effort tiers are defined as:

| Tier | Engineering weeks (single full-stack pair) |
|------|--------------------------------------------|
| S    | ≤ 4                                        |
| M    | 5 – 12                                     |
| L    | 13 – 26                                    |
| XL   | > 26 (requires separate project charter)   |

---

## M13-017 — Mobile App (React Native / Flutter)

**Priority**: High | **Effort**: XL | **Target**: Q3 2026 (planning), Q1 2027 (GA)

### Scope

A native-feeling cross-platform mobile application for the three existing portals:

| Portal | Primary Users | Key Screens |
|--------|--------------|-------------|
| Donor App | DONOR role | Eligibility check, appointment booking, donation history, loyalty points, digital card |
| Staff App | PHLEBOTOMIST, NURSE, RECEPTIONIST | Donor check-in, quick vitals, adverse reaction recording |
| Camp App | CAMP_COORDINATOR | Camp roster, offline-capable collection forms, barcode scan |

### Technology Decision Points

- **React Native** — preferred if the frontend team already uses TypeScript/React skills;
  large ecosystem, Expo managed workflow reduces native build overhead.
- **Flutter** — preferred if performance on low-end Android devices (common at field camps)
  is the primary driver; single codebase compiles to true native ARM code.
- **Recommendation**: Conduct a 2-week spike on Flutter targeting the Donor App; evaluate
  rendering performance, Keycloak OAuth2 PKCE flow, and offline sync before committing.

### Technical Considerations

1. **Authentication** — Keycloak PKCE flow via `flutter_appauth` or `react-native-app-auth`.
   Refresh tokens stored in the OS secure keystore (Keychain / Android Keystore), never
   in shared preferences or AsyncStorage.
2. **API surface** — existing REST APIs (`/api/v1/*`) are fully usable. Add push notification
   token registration endpoint to `notification-service`.
3. **Push notifications** — Firebase Cloud Messaging (FCM) for Android; APNs for iOS.
   Integrate with `notification-service` as a new channel alongside email/SMS.
4. **Offline sync** — see M13-018 for the camp-specific PWA approach; the mobile app uses
   SQLite (via `drift` for Flutter or `react-native-sqlite-storage`) with a conflict-
   resolution strategy of "last-write-wins on UUID primary keys".
5. **Barcode / QR scanning** — blood unit barcodes, donor ID QR codes; use
   `mobile_scanner` (Flutter) or `react-native-vision-camera`.
6. **CI/CD** — Fastlane for signing and deployment to TestFlight / Google Play internal
   track; integrate into the existing Jenkins pipeline as a new stage.

### Timeline Estimate

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| Planning & spike | 4 weeks | Technology choice ADR, wireframes, API gap analysis |
| Donor App MVP | 8 weeks | Login, eligibility quiz, appointment booking, donation history |
| Staff App MVP | 8 weeks | Check-in flow, adverse reaction form |
| Camp App MVP | 6 weeks | Offline forms, barcode scan, sync |
| Security review & pen test | 4 weeks | Penetration test report, fixes |
| Beta / TestFlight | 4 weeks | Closed beta with 3 pilot branches |
| App Store submission | 2 weeks | GA release |

**Total**: ~36 weeks (XL)

---

## M13-018 — Offline-Capable Blood Camp PWA

**Priority**: High | **Effort**: M | **Target**: Q4 2026

### Scope

A Progressive Web App embedded in the existing Angular 21 frontend that allows
CAMP_COORDINATOR and PHLEBOTOMIST users to collect blood at field camps with **no
persistent internet connection**. Data is queued locally and synced when connectivity
is restored.

### Functional Requirements

| # | Requirement |
|---|-------------|
| FR-01 | The PWA must be installable on Android Chrome and iOS Safari (manifest + service worker) |
| FR-02 | Camp roster (pre-downloaded) must be readable offline |
| FR-03 | New donor registration form must work offline and queue to sync |
| FR-04 | Blood collection form (volume, bag number, barcode) must work offline |
| FR-05 | Adverse reaction recording must work offline |
| FR-06 | All offline forms must validate locally before queuing |
| FR-07 | Sync must be idempotent — duplicate submissions must be rejected by the server |
| FR-08 | Conflict resolution: server record wins; user is notified of overwritten data |
| FR-09 | Offline queue must be encrypted at rest (IndexedDB + SubtleCrypto AES-GCM) |
| FR-10 | Sync status indicator must show pending record count and last-sync timestamp |

### Non-Functional Requirements

| # | Requirement |
|---|-------------|
| NFR-01 | Works on Android 10+ and iOS 15+ |
| NFR-02 | Cache must not exceed 50 MB (camp rosters, form assets) |
| NFR-03 | Sync must complete within 60 seconds for ≤ 500 queued records on 3G |
| NFR-04 | App shell must load in < 3 seconds on 3G from cache |
| NFR-05 | Offline queue must survive browser restart |

### Technical Architecture

```
┌──────────────────────────────────────────────────────────────┐
│ Angular 21 PWA (Camp Feature Module)                         │
│                                                              │
│  ┌─────────────┐   ┌──────────────────┐   ┌──────────────┐ │
│  │ Camp Forms  │──▶│ OfflineQueueSvc  │──▶│  IndexedDB   │ │
│  │ (Angular)   │   │ (signals-based)  │   │  (Dexie.js)  │ │
│  └─────────────┘   └────────┬─────────┘   └──────────────┘ │
│                             │ when online                    │
│                    ┌────────▼─────────┐                      │
│                    │  SyncWorker      │                      │
│                    │  (Web Worker)    │                      │
│                    └────────┬─────────┘                      │
└─────────────────────────────┼────────────────────────────────┘
                              │ HTTPS REST
                    ┌─────────▼─────────┐
                    │  donor-service    │
                    │  /api/v1/camps/*  │
                    └───────────────────┘
```

### Key Libraries

| Library | Purpose |
|---------|---------|
| `@angular/service-worker` | Service worker, cache strategies |
| `Dexie.js` | Typed IndexedDB wrapper |
| `idb-keyval` | Simple key-value store for settings |
| `Workbox` (via Angular SW) | Precaching, runtime caching strategies |

### Sync Protocol

1. `SyncWorker` polls for connectivity using `navigator.onLine` and a lightweight
   heartbeat `HEAD /api/v1/health` every 30 seconds.
2. On reconnect, the worker reads all pending records from IndexedDB ordered by
   `queuedAt ASC`.
3. Each record is `POST`-ed with `Idempotency-Key: {clientGeneratedUUID}` header.
4. The server stores the idempotency key for 24 hours and returns the same response
   for duplicate submissions.
5. Successfully synced records are removed from the queue; failed records remain and
   are retried with exponential back-off (1 s, 2 s, 4 s, max 5 attempts).

---

## M13-019 — AI/ML Blood Demand Forecasting

**Priority**: Medium | **Effort**: L | **Target**: Q1 2027

### Scope

A demand forecasting module that predicts blood unit requirements per blood group,
per branch, over a 7-day and 30-day horizon. Predictions feed into inventory alerts
and camp scheduling recommendations.

### Data Requirements

| Dataset | Source | Minimum History |
|---------|--------|-----------------|
| Daily blood unit issues by group | `blood_issues`, `transfusions` | 24 months |
| Blood unit intake by group | `collections`, `blood_units` | 24 months |
| Camp collection records | `camp_collections` | 24 months |
| Hospital request volumes | `hospital_requests` | 12 months |
| Seasonal events calendar | Manual / public holiday API | Ongoing |
| Local disease incidence | Integration with national health authority API (optional) | Ongoing |
| Demographic data per branch | `branches`, census data | Static |

**Minimum viable training set**: 18 months of daily `blood_issues` and `collections`
per branch, covering at least one complete seasonal cycle.

### Model Approach

#### Phase 1 — Statistical Baseline (Q1 2027)

Use classical time-series methods before moving to ML:

1. **SARIMA** (Seasonal ARIMA) per blood group × branch — good for small datasets,
   interpretable, no GPU required.
2. **Holt-Winters** exponential smoothing — simple, fast, handles trend + seasonality.
3. **Evaluation metric**: MAPE (Mean Absolute Percentage Error) target < 15% on
   30-day horizon, < 10% on 7-day horizon.

Tooling: Python, `statsmodels`, served via a FastAPI micro-endpoint deployed alongside
`reporting-service`.

#### Phase 2 — ML Models (Q3 2027, if Phase 1 MAPE targets are not met)

| Model | Library | Notes |
|-------|---------|-------|
| LightGBM | `lightgbm` | Gradient boosting; best for tabular with categorical features |
| Prophet | `prophet` | Facebook's forecasting library; handles holidays, missing data |
| Temporal Fusion Transformer | `pytorch-forecasting` | State-of-art for multi-horizon; requires GPU |

#### Feature Engineering

- Rolling 7/14/30-day averages of issues and collections
- Day-of-week, month, public holiday flags
- Stock level at start of period
- Pending hospital requests
- Trend indicator (linear slope over last 30 days)

### Integration Architecture

```
┌──────────────────────────────────────┐
│  reporting-service                   │
│  (scheduled Quartz job — 02:00 UTC)  │
│                                      │
│  1. Extract features from bloodbank_db│
│  2. POST /forecast to ML Service     │
│  3. Store predictions in             │
│     forecast_predictions table       │
└──────────────┬───────────────────────┘
               │ REST
┌──────────────▼───────────────────────┐
│  forecast-service (Python FastAPI)   │
│  - Loads pre-trained SARIMA models   │
│  - Returns JSON predictions          │
│  - Exposes /retrain endpoint         │
└──────────────────────────────────────┘
```

New table required: `forecast_predictions (id, branch_id, blood_group, horizon_days,
predicted_units, confidence_lower, confidence_upper, model_version, generated_at)`.

### Privacy & Compliance

- Predictions are derived from aggregate counts only — no PHI is used in the
  feature set.
- GDPR Article 22 (automated decision-making): predictions are advisory only;
  no individual donor or patient decisions are automated.

---

## M13-020 — IoT Cold Chain Monitoring

**Priority**: High | **Effort**: L | **Target**: Q2 2027

### Scope

Real-time temperature and humidity monitoring for blood storage units and transport
boxes using IoT sensors. Alerts are raised when readings fall outside WHO-mandated
ranges, and excursions are logged for regulatory traceability.

### WHO Temperature Ranges

| Component | Storage Temp | Action Threshold |
|-----------|-------------|-----------------|
| Whole blood / Red cells | +2°C to +6°C | < 1°C or > 8°C |
| Platelets | +20°C to +24°C (agitated) | < 18°C or > 26°C |
| Fresh Frozen Plasma | ≤ −25°C | > −18°C |
| Cryoprecipitate | ≤ −25°C | > −18°C |
| Granulocytes | +20°C to +24°C | < 18°C or > 26°C |

### Sensor Integration Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Edge Layer (Blood Bank Branch)                                 │
│                                                                 │
│  ┌──────────────┐     ┌─────────────────┐                      │
│  │ BLE / Zigbee │────▶│ Edge Gateway    │                      │
│  │ Temp Sensors │     │ (Raspberry Pi 4 │                      │
│  │ (per fridge) │     │  or equiv.)     │                      │
│  └──────────────┘     └────────┬────────┘                      │
│                                │ MQTT over TLS                  │
└────────────────────────────────┼────────────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  MQTT Broker            │
                    │  (Eclipse Mosquitto or  │
                    │   AWS IoT Core)         │
                    └────────────┬────────────┘
                                 │ AMQP / SQS bridge
                    ┌────────────▼────────────┐
                    │  inventory-service       │
                    │  Cold Chain Consumer     │
                    │  (RabbitMQ listener or  │
                    │   SQS consumer)          │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  cold_chain_logs table   │
                    │  (already in schema V14) │
                    └─────────────────────────┘
```

### Sensor Specifications (Recommended)

| Attribute | Requirement |
|-----------|------------|
| Protocol | BLE 5.0 or Zigbee 3.0 |
| Accuracy | ± 0.3°C |
| Resolution | 0.1°C |
| Sampling interval | 1 minute (configurable) |
| Battery life | ≥ 12 months |
| Certification | CE, FDA 21 CFR Part 820 (medical device quality) |
| Data retention on device | ≥ 30 days (for disconnected logging) |

### Alerting Rules

| Condition | Action | Recipients |
|-----------|--------|-----------|
| Temp out of range > 5 min | Push notification + RabbitMQ `ColdChainExcursionEvent` | INVENTORY_MANAGER, BRANCH_MANAGER |
| Temp out of range > 30 min | Email + SMS | BRANCH_ADMIN + on-call engineer |
| Sensor offline > 15 min | Alert (sensor or gateway failure) | INVENTORY_MANAGER |
| Transport box door open > 10 min | Alert | PHLEBOTOMIST |

### New Events

```java
public record ColdChainExcursionEvent(
    UUID storageLocationId,
    UUID branchId,
    double recordedTempCelsius,
    double minThresholdCelsius,
    double maxThresholdCelsius,
    int durationMinutes,
    Instant occurredAt
) {}
```

---

## M13-021 — WhatsApp Business API Integration

**Priority**: Medium | **Effort**: M | **Target**: Q3 2026

### Scope

Add WhatsApp Business API as a notification channel in `notification-service`,
enabling appointment reminders, donation acknowledgements, and emergency donor
mobilisation messages.

### Integration Points

```
notification-service
└── channel/
    ├── EmailChannel.java          (existing)
    ├── SmsChannel.java            (existing)
    └── WhatsAppChannel.java       (NEW)
        └── Uses Meta Cloud API v19+
            POST https://graph.facebook.com/v19.0/{phone-number-id}/messages
```

### Message Templates

WhatsApp requires pre-approved message templates for business-initiated messages.
Templates to register with Meta:

| Template Name | Category | Parameters |
|---------------|----------|-----------|
| `appointment_reminder` | UTILITY | `{{donor_name}}`, `{{date}}`, `{{time}}`, `{{branch_name}}` |
| `donation_thank_you` | UTILITY | `{{donor_name}}`, `{{units_donated}}`, `{{blood_group}}` |
| `eligibility_result` | UTILITY | `{{donor_name}}`, `{{result}}`, `{{next_eligible_date}}` |
| `emergency_request` | UTILITY | `{{blood_group}}`, `{{branch_name}}`, `{{contact_number}}` |
| `camp_reminder` | UTILITY | `{{donor_name}}`, `{{camp_name}}`, `{{date}}`, `{{location}}` |

### Configuration (application.yml addition)

```yaml
bloodbank:
  notifications:
    whatsapp:
      enabled: ${WHATSAPP_ENABLED:false}
      api-url: https://graph.facebook.com/v19.0
      phone-number-id: ${WHATSAPP_PHONE_NUMBER_ID}
      access-token: ${WHATSAPP_ACCESS_TOKEN}
      webhook-verify-token: ${WHATSAPP_WEBHOOK_VERIFY_TOKEN}
```

### Webhook (Inbound)

Register a webhook with Meta to receive delivery status updates and opt-out requests:
`POST /api/v1/notifications/whatsapp/webhook`

The `notification-service` must handle:
- `messages.status = delivered/read/failed` — update `notifications.status`
- `messages.type = text` where body is `STOP` — set `notification_preferences.whatsapp_opt_out = true`

### Compliance

- **GDPR**: WhatsApp consent must be captured separately from email/SMS consent.
  Add `whatsapp_consent` boolean column to `notification_preferences`.
- **Rate limits**: Meta limits to 1,000 unique recipients per 24 hours on tier 1;
  upgrade to tier 2 (10,000/day) after 1,000 successful conversations.
- **DPDP Act (India)**: Explicit consent required; maintained in `donor_consents` table.

---

## M13-022 — Government ID Verification Integration

**Priority**: Medium | **Effort**: M | **Target**: Q4 2026

### Scope

Verify donor identity at registration using government-issued ID APIs, reducing
fraudulent donation attempts and duplicate donor records.

### Integration Points by Region

| Region | API / System | Notes |
|--------|-------------|-------|
| India | UIDAI Aadhaar OTP / DigiLocker | Requires UIDAI authorised entity licence |
| EU | eIDAS — national eID schemes | Via EUDI Wallet API (2024+ rollout) |
| USA | NIST 800-63-3 IAL2 (SSA / state DMV APIs) | Via identity broker (e.g. ID.me, Experian) |
| Generic fallback | Manual document scan + AI OCR (AWS Rekognition / Google Vision) | Operator-assisted verification |

### Architecture

```
donor-service
└── identity/
    ├── IdentityVerificationService.java
    │   └── routes to correct provider based on branch country code
    ├── provider/
    │   ├── AadhaarVerificationProvider.java
    │   ├── EidasVerificationProvider.java
    │   └── ManualOcrVerificationProvider.java
    └── dto/
        ├── VerificationRequest.java (record)
        └── VerificationResult.java  (record)
```

### Data Model Addition

```sql
ALTER TABLE donors ADD COLUMN id_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE donors ADD COLUMN id_verification_method VARCHAR(50);
ALTER TABLE donors ADD COLUMN id_verified_at TIMESTAMPTZ;
-- id_number is NOT stored; only verification outcome and method are persisted
```

**Critical**: The government ID number itself is **never stored** in the database.
Only the verification outcome (`verified = true/false`), method, and timestamp are
retained to comply with data minimisation (GDPR Art. 5) and HIPAA minimum necessary
principles.

### Flow

1. Donor enters ID type and number on registration form.
2. `donor-service` calls the appropriate provider; the provider returns a
   `verified: boolean` and `name_match_confidence: float`.
3. If `verified && name_match_confidence ≥ 0.85`, registration proceeds automatically.
4. If `0.70 ≤ name_match_confidence < 0.85`, a RECEPTIONIST review task is created.
5. If `verified == false`, registration is blocked and the donor is directed to the branch.
6. ID number is discarded after the API call; it is never logged or persisted.

---

## M13-023 — Additional Language Support (Hindi, Arabic, Chinese)

**Priority**: Medium | **Effort**: M | **Target**: Q3 2026

### Scope

Extend the existing i18n infrastructure (Angular `@ngx-translate` or Angular built-in
`$localize`) to support Hindi (`hi`), Arabic (`ar`), and Simplified Chinese (`zh-CN`),
bringing the total supported languages to at least 6 (English, French, Spanish + 3 new).

### Current i18n State

- Backend: `messages.properties`, `messages_es.properties`, `messages_fr.properties`
  present in all services.
- Frontend: `src/assets/i18n/en.json`, `es.json`, `fr.json` (assumed).

### Expansion Steps

| Step | Task |
|------|------|
| 1 | Extract all hardcoded strings from Angular templates into i18n keys (audit) |
| 2 | Add `hi.json`, `ar.json`, `zh-CN.json` to `src/assets/i18n/` |
| 3 | Add `messages_hi.properties`, `messages_ar.properties`, `messages_zh.properties` to all services |
| 4 | Add RTL layout support in Angular for Arabic (`dir="rtl"` + CSS logical properties) |
| 5 | Add language selector to the user profile menu |
| 6 | Store preferred language in Keycloak user attributes |
| 7 | Configure content negotiation: `Accept-Language` header → backend message locale |
| 8 | Professional translation review (MT + human post-edit) |

### RTL Considerations for Arabic

- Use CSS logical properties (`margin-inline-start` not `margin-left`) throughout.
- Material 3 has built-in RTL support; verify all custom layout components with
  `dir="rtl"` applied to the `<html>` element.
- Tailwind CSS 4 supports `rtl:` variant — use `rtl:text-right` etc. for layout classes.
- Numbers and dates: use `Intl.NumberFormat` and `Intl.DateTimeFormat` with the `ar`
  locale; do not hardcode number/date formats.

### Backend Spring Boot Locale Support

```yaml
# application.yml addition
spring:
  messages:
    basename: messages
    encoding: UTF-8
    cache-duration: 3600
```

The `GlobalExceptionHandler` already passes `Locale` from `Accept-Language`; ensure
all `MessageSource.getMessage()` calls use the request locale.

---

## M13-024 — HL7 FHIR R4 Integration

**Priority**: Medium | **Effort**: L | **Target**: Q2 2027

### Scope

Expose BloodBank data to external hospital information systems (HIS) and electronic
health record (EHR) platforms using the HL7 FHIR R4 standard, enabling bidirectional
interoperability.

### FHIR Resources Mapped

| BloodBank Entity | FHIR Resource | Direction |
|------------------|--------------|-----------|
| Donor | `Patient` | Outbound |
| Blood collection | `Procedure` | Outbound |
| Lab test result | `Observation`, `DiagnosticReport` | Outbound |
| Blood unit | `BiologicallyDerivedProduct` | Outbound |
| Transfusion | `MedicationAdministration` | Outbound |
| Hospital blood request | `ServiceRequest` | Inbound |
| Crossmatch result | `Observation` | Outbound |

### Architecture

A new lightweight `fhir-adapter-service` acts as a translation layer:

```
EHR System
    │  FHIR R4 REST (JSON)
    ▼
fhir-adapter-service  (Spring Boot, HAPI FHIR library)
    │  Internal REST (/api/v1/*)
    ├──▶ donor-service
    ├──▶ lab-service
    ├──▶ inventory-service
    └──▶ transfusion-service
```

**Library**: HAPI FHIR 7.x (`ca.uhn.hapi.fhir:hapi-fhir-server`,
`hapi-fhir-structures-r4`) — Apache 2.0 licensed, widely adopted in production.

### FHIR Capability Statement

The adapter exposes a `GET /fhir/metadata` FHIR CapabilityStatement listing all
supported resource types, search parameters, and operations.

### Authentication

SMART on FHIR (`OAuth2 with PKCE`) using the existing Keycloak realm. Hospital
systems register as Keycloak confidential clients with `fhir:read` / `fhir:write`
scopes.

### Compliance

- **IHE XD-LAB** profile for laboratory result sharing.
- **GDPR**: FHIR resource access logged in `audit_logs` with actor, resource, and action.
- **HIPAA**: PHI transmitted over FHIR must be encrypted (TLS 1.3 enforced at gateway).

---

## M13-025 — Payment Gateway Integration

**Priority**: Medium | **Effort**: M | **Target**: Q4 2026

### Scope

Enable online payment collection for hospital blood requests and component processing
fees via the `billing-service`.

### Integration Points

| Gateway | Region | Notes |
|---------|--------|-------|
| Razorpay | India | UPI, NEFT, cards; excellent webhook support |
| Stripe | Global | Cards, bank transfers; HIPAA Business Associate Agreement available |
| PayPal | Americas, EU | Alternative for international hospitals |

### Architecture

```
billing-service
└── payment/
    ├── PaymentGatewayService.java          (interface)
    ├── RazorpayPaymentGateway.java         (India impl)
    ├── StripePaymentGateway.java           (global impl)
    └── dto/
        ├── PaymentInitiateRequest.java     (record)
        ├── PaymentInitiateResponse.java    (record — contains gateway order ID, redirect URL)
        └── PaymentWebhookEvent.java        (record)
```

### Payment Flow

```
1. Hospital user clicks "Pay Invoice" in hospital portal
2. billing-service creates gateway order (POST to gateway API)
3. Frontend redirects user to hosted payment page (iframe or redirect)
4. User completes payment on gateway
5. Gateway fires webhook to billing-service /api/v1/billing/payments/webhook
6. billing-service verifies HMAC signature of webhook payload
7. Invoice status updated to PAID; InvoiceGeneratedEvent published with status=PAID
8. notification-service sends payment receipt
```

### Database Additions

```sql
ALTER TABLE payments ADD COLUMN gateway_provider VARCHAR(50);
ALTER TABLE payments ADD COLUMN gateway_order_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN gateway_payment_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN gateway_signature VARCHAR(512);
```

### PCI DSS Compliance

- BloodBank **never handles raw card data** — all card processing is delegated to
  the PCI-DSS certified gateway.
- Webhook payloads are verified by HMAC-SHA256 (Razorpay / Stripe standard).
- Store only `gateway_payment_id` and masked card metadata (last 4 digits, brand) —
  never store CVV or full PAN.

---

## M13-026 — ERP Export Integration

**Priority**: Low | **Effort**: S | **Target**: Q1 2027

### Scope

Scheduled export of financial and inventory data to external ERP systems (SAP, Oracle
Financials, Tally) to eliminate manual data re-entry.

### Supported Export Formats

| Format | Use Case | ERP Systems |
|--------|----------|------------|
| CSV | Universal fallback | All |
| XLSX | Finance teams | Tally, manual import |
| SAP IDOC XML | Direct SAP integration | SAP S/4HANA, SAP Business One |
| JSON REST webhook | Modern ERP push | Oracle Fusion, NetSuite |

### Data Domains Exported

| Domain | Source Tables | Frequency |
|--------|-------------|-----------|
| Invoices | `invoices`, `invoice_line_items` | Daily (previous day) |
| Payments received | `payments` | Daily |
| Credit notes | `credit_notes` | Daily |
| Blood unit consumption (CoGS) | `blood_issues`, `blood_units` | Daily |
| Inventory valuation | `blood_units`, `component_types` | Weekly |
| Accounts receivable | `invoices` where `status = UNPAID` | Weekly |

### Scheduling

Exports run as Quartz jobs in `reporting-service`:

```yaml
bloodbank:
  export:
    erp:
      enabled: ${ERP_EXPORT_ENABLED:false}
      format: ${ERP_EXPORT_FORMAT:CSV}       # CSV | XLSX | SAP_IDOC | JSON_WEBHOOK
      destination: ${ERP_EXPORT_DESTINATION} # SFTP path or webhook URL
      cron: "0 0 3 * * *"                   # 03:00 UTC daily
      sftp:
        host: ${ERP_SFTP_HOST}
        username: ${ERP_SFTP_USER}
        private-key-path: ${ERP_SFTP_KEY_PATH}
```

### New Endpoint

`POST /api/v1/reporting/exports/erp` — triggers an on-demand export (SYSTEM_ADMIN only).
`GET /api/v1/reporting/exports/erp/history` — lists export job history with status.

---

## M13-027 — Advanced Analytics & BI Dashboards

**Priority**: Medium | **Effort**: L | **Target**: Q3 2027

### Scope

Replace the current Chart.js dashboards with an embedded BI solution offering
ad-hoc querying, drill-down, cross-branch comparison, and scheduled report delivery.

### Dashboard Requirements

#### Executive Dashboard (SUPER_ADMIN, REGIONAL_ADMIN)

| Widget | Metric |
|--------|--------|
| Blood availability heat map | Units by group × branch on a geographic map |
| Donation trend | 12-month rolling donation volume with YoY comparison |
| Hospital fulfilment rate | % requests fulfilled within SLA |
| Revenue summary | Monthly billing totals with variance |
| Top 10 donor branches | By volume |
| Wastage rate | Units expired / discarded as % of total received |

#### Branch Dashboard (BRANCH_ADMIN, BRANCH_MANAGER)

| Widget | Metric |
|--------|--------|
| Stock by blood group | Real-time units on hand vs. par level |
| Donations this month | vs. target |
| Pending crossmatch requests | Count + SLA status |
| Adverse reaction rate | Trailing 30 days |
| Collection efficiency | Collections per phlebotomist-hour |

### Technology Options

| Option | Licence | Embed Model | Notes |
|--------|---------|-------------|-------|
| Apache Superset | Apache 2.0 | iFrame / Guest token API | Fully open-source; requires separate container |
| Metabase | SSPL (Community) / Commercial | Embedding API | Easy setup; commercial licence for production embedding |
| Grafana | AGPLv3 (OSS) / Commercial | Panel embedding | Already used for infrastructure metrics |
| Custom Chart.js v2 | MIT | Native Angular | Extends current implementation; limited ad-hoc |

**Recommendation**: Use Apache Superset for ad-hoc analytics (SUPER_ADMIN /
REGIONAL_ADMIN) and extend existing Chart.js panels for branch-level dashboards.
Superset connects directly to `bloodbank_db` with a read-only reporting user.

### Reporting User (PostgreSQL)

```sql
CREATE ROLE bloodbank_reporting WITH LOGIN PASSWORD '${REPORTING_DB_PASSWORD}';
GRANT CONNECT ON DATABASE bloodbank_db TO bloodbank_reporting;
GRANT USAGE ON SCHEMA public TO bloodbank_reporting;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO bloodbank_reporting;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO bloodbank_reporting;
-- Revoke access to PHI-heavy columns
REVOKE SELECT (email, phone, national_id_hash) ON donors FROM bloodbank_reporting;
```

---

## M13-028 — Donor Gamification & Social Sharing

**Priority**: Low | **Effort**: M | **Target**: Q2 2027

### Scope

Increase donor retention and recruitment through a gamification layer within the
Donor Portal, including achievements, loyalty tiers, shareable donation milestones,
and referral tracking.

### Achievement System

| Achievement | Trigger | Badge |
|-------------|---------|-------|
| First Donation | 1 completed donation | 🩸 First Drop |
| Regular Donor | 5 donations in 12 months | ⭐ Regular |
| Champion | 10 donations in 12 months | 🏆 Champion |
| Life Saver | 25 total donations | 💙 Life Saver |
| Super Donor | 50 total donations | 🦸 Super Donor |
| Rare Type | Any donation with AB−, B−, A− | 🔬 Rare Type |
| Camp Hero | 3+ donations at blood camps | ⛺ Camp Hero |
| Early Bird | Donated within 7 days of becoming eligible | ⏰ Early Bird |

Achievements are unlocked by a `donor-service` scheduled job that evaluates eligibility
daily and publishes `DonorAchievementUnlockedEvent` to trigger push notifications.

### Loyalty Tiers

| Tier | Requirement | Benefits |
|------|------------|---------|
| Bronze | 1–4 lifetime donations | Digital certificate |
| Silver | 5–14 lifetime donations | Priority appointment booking |
| Gold | 15–29 lifetime donations | Priority + physical certificate |
| Platinum | 30+ lifetime donations | Priority + annual recognition event invite |

Tier data stored in `donor_loyalty` table (already in schema).

### Social Sharing

A donation milestone generates a shareable card image (PNG) via a server-side renderer
(`puppeteer` or `playwright` in a lightweight Node.js sidecar, or `wkhtmltopdf`):

- Card design: blood drop graphic, donor name, milestone, blood group, date.
- Shareable link: `https://bloodbank.org/share/donation/{shareToken}`.
- The share token maps to a public endpoint returning the image; no PHI is exposed
  beyond what the donor explicitly chooses to share.
- Pre-built sharing buttons: WhatsApp, X (Twitter), LinkedIn, copy-link.
- Opt-in: Social sharing is disabled by default; donor must explicitly enable it in
  the Donor Portal settings.

### Referral Tracking

| Item | Detail |
|------|--------|
| Referral link format | `https://bloodbank.org/register?ref={donorCode}` |
| `donorCode` | 8-character alphanumeric, unique per donor |
| Reward | +1 "referral point" per successful first-time donation by referee |
| Referral points | Displayed in Donor Portal; redeemable for priority booking slots |
| DB change | Add `referral_code VARCHAR(8)`, `referred_by_donor_id UUID` to `donors` |

---

## Quarterly Review Process

The enhancement roadmap is reviewed at the start of each quarter by the product
and engineering leads. Each item is assessed against:

1. **Impact** — How many donors / hospitals / branches benefit?
2. **Effort** — Updated estimate based on team velocity.
3. **Dependencies** — External API readiness, regulatory approvals, infrastructure.
4. **Revenue / Cost** — Does the enhancement generate revenue or reduce operational cost?

Items may be promoted to an active sprint, deferred to a later quarter, or removed
from the backlog based on this assessment. The roadmap file is updated and a new
commit is tagged with the review date.
