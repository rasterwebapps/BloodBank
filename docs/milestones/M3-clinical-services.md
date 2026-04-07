# M3: Clinical Services — transfusion, hospital, request-matching

**Duration:** 2 weeks
**Dependencies:** M2 (Core Services)
**Exit Gate:** Clinical workflow end-to-end test passes

## 📊 Development Status: 🟡 IN PROGRESS (~35%)

**Started:** 2026-04-06 | **PRs:** #11 (merged 2026-04-07)
**Issues Completed:** 15/43
- transfusion-service: ✅ Complete (64 main files, 8 test files — merged to main via PR #11)
- hospital-service: ⚠️ Partial scaffold (2 of 4 entities, 6 enums, no DTOs/services/controllers/tests)
- request-matching-service: ❌ Minimal (Application class only)
- Cross-service tests: ❌ Not started

## 🔧 FIX REQUIRED

| # | Issue | Severity | Description |
|---|---|---|---|
| 1 | **hospital-service incomplete** | 🔴 HIGH | Missing 2 entities (HospitalRequest, HospitalFeedback), all DTOs, mappers, repos, services, controllers, RabbitMQ (BloodRequestCreatedEvent publisher), and all tests. Only Hospital + HospitalContract entities + 6 enums exist. |
| 2 | **request-matching-service barely started** | 🔴 HIGH | Only Application.java exists. Missing all 3 entities (EmergencyRequest, DisasterEvent, DonorMobilization), compatibility matching algorithm, emergency/disaster workflows, all controllers/tests, RabbitMQ listeners (BloodStockUpdatedEvent, BloodRequestCreatedEvent) + publishers (BloodRequestMatchedEvent, EmergencyRequestEvent). |
| 3 | **Cross-service clinical tests not started** | 🟡 MEDIUM | M3-041, M3-042, M3-043 integration tests are required for exit gate. |
| 4 | **Blocks M5 frontend clinical features** | 🔴 BLOCKER | M5 API Gateway and Config Server are done (PR #15). But frontend clinical feature modules cannot be built until these services exist. |

---

## Objective

Implement the clinical workflow services: blood transfusion management, hospital integration, and emergency request matching.

## Issues

### transfusion-service (Modules 6, 7)
- [x] **M3-001**: Scaffold transfusion-service project structure *(PR #11)*
- [x] **M3-002**: Create entities — CrossMatchRequest, CrossMatchResult, BloodIssue, EmergencyIssue *(PR #11)*
- [x] **M3-003**: Create entities — Transfusion, TransfusionReaction, HemovigilanceReport, LookBackInvestigation *(PR #11)*
- [x] **M3-004**: Create DTOs (records) for all transfusion entities *(PR #11 — 18 records)*
- [x] **M3-005**: Create MapStruct mappers *(PR #11 — 8 mappers)*
- [x] **M3-006**: Create repositories with cross-match query methods *(PR #11 — 8 repos)*
- [x] **M3-007**: Create services — CrossMatchService, BloodIssueService, TransfusionService, HemovigilanceService *(PR #11)*
- [x] **M3-008**: Implement ABO/Rh compatibility checking algorithm *(PR #11 — BloodCompatibilityUtil)*
- [x] **M3-009**: Implement emergency O-negative issue protocol *(PR #11)*
- [x] **M3-010**: Create controllers with @PreAuthorize (DOCTOR, NURSE roles) *(PR #11 — 4 controllers)*
- [x] **M3-011**: Create RabbitMQ publishers — TransfusionCompletedEvent, TransfusionReactionEvent *(PR #11)*
- [x] **M3-012**: Implement look-back investigation workflow *(PR #11)*
- [x] **M3-013**: Write unit tests (>80% coverage) *(PR #11 — 4 service test classes)*
- [x] **M3-014**: Write integration tests with Testcontainers *(PR #11 — 4 controller test classes)*

### hospital-service (Module 10)
- [x] **M3-015**: Scaffold hospital-service project structure *(PR #11)*
- [ ] **M3-016**: Create entities — Hospital, HospitalContract, HospitalRequest, HospitalFeedback *(⚠️ only Hospital + HospitalContract done)*
- [ ] **M3-017**: Create DTOs (records) for all hospital entities
- [ ] **M3-018**: Create MapStruct mappers
- [ ] **M3-019**: Create repositories
- [ ] **M3-020**: Create services — HospitalService, ContractService, BloodRequestService, FeedbackService
- [ ] **M3-021**: Create controllers with @PreAuthorize (HOSPITAL_USER, BRANCH_MANAGER roles)
- [ ] **M3-022**: Create RabbitMQ publisher — BloodRequestCreatedEvent
- [ ] **M3-023**: Implement hospital credit management
- [ ] **M3-024**: Write unit tests (>80% coverage)
- [ ] **M3-025**: Write integration tests

### request-matching-service (Modules 6-matching, 23)
- [ ] **M3-026**: Scaffold request-matching-service project structure *(⚠️ Application class only)*
- [ ] **M3-027**: Create entities — EmergencyRequest, DisasterEvent, DonorMobilization
- [ ] **M3-028**: Create DTOs (records) for matching entities
- [ ] **M3-029**: Create MapStruct mappers
- [ ] **M3-030**: Create repositories
- [ ] **M3-031**: Create services — RequestMatchingService, EmergencyService, DisasterResponseService
- [ ] **M3-032**: Implement blood compatibility matching algorithm (ABO/Rh, FEFO selection)
- [ ] **M3-033**: Implement emergency blood request workflow
- [ ] **M3-034**: Implement mass casualty protocol
- [ ] **M3-035**: Implement donor mobilization workflow
- [ ] **M3-036**: Create controllers with @PreAuthorize
- [ ] **M3-037**: Create RabbitMQ publishers — BloodRequestMatchedEvent, EmergencyRequestEvent
- [ ] **M3-038**: Create RabbitMQ listeners — BloodStockUpdatedEvent, BloodRequestCreatedEvent
- [ ] **M3-039**: Write unit tests (>80% coverage)
- [ ] **M3-040**: Write integration tests

### Cross-Service Clinical Workflow Test
- [ ] **M3-041**: Integration test: Hospital Request → Match → Cross-Match → Issue → Transfuse
- [ ] **M3-042**: Integration test: Emergency Request → O-Neg Issue → Transfusion → Reaction → Hemovigilance
- [ ] **M3-043**: Integration test: Disaster Event → Mass Mobilization → Emergency Stock Rebalancing

## Deliverables

1. 3 running clinical services
2. Blood compatibility matching algorithm
3. Emergency and disaster response workflows
4. End-to-end clinical workflow integration tests
