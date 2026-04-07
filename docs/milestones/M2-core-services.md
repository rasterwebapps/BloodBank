# M2: Core Services — donor, branch, lab, inventory

**Duration:** 2 weeks
**Dependencies:** M1 (Foundation)
**Exit Gate:** All 4 core services running with unit + integration tests passing

## 📊 Development Status: ✅ COMPLETE (100%)

**Completed:** 2026-04-06 | **PRs:** #7 (branch-service), #8 (donor-service), #9 (lab-service), #10 (inventory-service)
**Issues Completed:** 54/54 | **Total Tests:** 383+

---

## Objective

Implement the 4 foundational microservices that handle the core blood bank workflow: donor registration, branch management, laboratory testing, and inventory management.

## Issues

### branch-service (Modules 8, 17) — Start First (others depend on it)
- [x] **M2-001**: Scaffold branch-service project structure *(PR #7)*
- [x] **M2-002**: Create entities — Branch, BranchOperatingHours, BranchEquipment, BranchRegion *(PR #7)*
- [x] **M2-003**: Create entities — BloodGroup, ComponentType, DeferralReason, ReactionType, Country, Region, City, IcdCode *(PR #7)*
- [x] **M2-004**: Create DTOs (records) for all branch entities *(PR #7 — 17 records)*
- [x] **M2-005**: Create MapStruct mappers for all branch entities *(PR #7)*
- [x] **M2-006**: Create repositories with custom query methods *(PR #7 — 12 repos)*
- [x] **M2-007**: Create services — BranchService, MasterDataService *(PR #7)*
- [x] **M2-008**: Create controllers — BranchController, MasterDataController (with @PreAuthorize) *(PR #7 — 39 endpoints)*
- [x] **M2-009**: Configure Redis caching for master data (blood groups, countries, component types) *(PR #7 — 24h TTL)*
- [x] **M2-010**: Create application.yml configs (dev, prod) *(PR #7)*
- [x] **M2-011**: Write unit tests (>80% coverage) *(PR #7 — 180 tests)*
- [x] **M2-012**: Write integration tests with Testcontainers *(PR #7)*

### donor-service (Modules 1, 2, 9, 24)
- [x] **M2-013**: Scaffold donor-service project structure *(PR #8)*
- [x] **M2-014**: Create entities — Donor, DonorHealthRecord, DonorDeferral, DonorConsent, DonorLoyalty *(PR #8)*
- [x] **M2-015**: Create entities — Collection, CollectionAdverseReaction, CollectionSample *(PR #8)*
- [x] **M2-016**: Create entities — BloodCamp, CampResource, CampDonor, CampCollection *(PR #8)*
- [x] **M2-017**: Create DTOs (records) for all donor entities *(PR #8 — 26 records)*
- [x] **M2-018**: Create MapStruct mappers *(PR #8 — 8 mappers)*
- [x] **M2-019**: Create repositories *(PR #8 — 12 repos)*
- [x] **M2-020**: Create services — DonorService, CollectionService, BloodCampService, DonorLoyaltyService *(PR #8)*
- [x] **M2-021**: Create controllers with @PreAuthorize per role matrix *(PR #8 — 35 endpoints)*
- [x] **M2-022**: Create RabbitMQ publishers — DonationCompletedEvent, CampCompletedEvent *(PR #8)*
- [x] **M2-023**: Create RabbitMQ listener — EmergencyRequestEvent *(PR #8)*
- [x] **M2-024**: Create application.yml configs *(PR #8)*
- [x] **M2-025**: Write unit tests (>80% coverage) *(PR #8 — 113 tests)*
- [x] **M2-026**: Write integration tests with Testcontainers *(PR #8)*

### lab-service (Module 3)
- [x] **M2-027**: Scaffold lab-service project structure *(PR #9)*
- [x] **M2-028**: Create entities — TestOrder, TestResult, TestPanel, LabInstrument, QualityControlRecord *(PR #9)*
- [x] **M2-029**: Create DTOs (records) for all lab entities *(PR #9 — 11 records)*
- [x] **M2-030**: Create MapStruct mappers *(PR #9 — 5 mappers)*
- [x] **M2-031**: Create repositories *(PR #9 — 5 repos)*
- [x] **M2-032**: Create services — TestOrderService, TestResultService, QualityControlService *(PR #9)*
- [x] **M2-033**: Create controllers with @PreAuthorize (LAB_TECHNICIAN role) *(PR #9 — 5 controllers)*
- [x] **M2-034**: Create RabbitMQ publishers — TestResultAvailableEvent, UnitReleasedEvent *(PR #9)*
- [x] **M2-035**: Implement dual-review approval workflow for test results *(PR #9 — verifiedBy != testedBy)*
- [x] **M2-036**: Create application.yml configs *(PR #9)*
- [x] **M2-037**: Write unit tests (>80% coverage) *(PR #9 — 90 tests)*
- [x] **M2-038**: Write integration tests with Testcontainers *(PR #9)*

### inventory-service (Modules 4, 5, 22)
- [x] **M2-039**: Scaffold inventory-service project structure *(PR #10)*
- [x] **M2-040**: Create entities — BloodUnit, BloodComponent, ComponentProcessing, ComponentLabel, PooledComponent *(PR #10)*
- [x] **M2-041**: Create entities — StorageLocation, StockTransfer, UnitDisposal, UnitReservation *(PR #10)*
- [x] **M2-042**: Create entities — TransportRequest, ColdChainLog, TransportBox, DeliveryConfirmation *(PR #10)*
- [x] **M2-043**: Create DTOs (records) for all inventory entities *(PR #10 — 28 records)*
- [x] **M2-044**: Create MapStruct mappers *(PR #10 — 13 mappers)*
- [x] **M2-045**: Create repositories *(PR #10 — 13 repos)*
- [x] **M2-046**: Create services — BloodUnitService, ComponentService, StockService, TransferService, LogisticsService *(PR #10 — 6 services incl. ExpirySchedulerService)*
- [x] **M2-047**: Create controllers with @PreAuthorize (INVENTORY_MANAGER role) *(PR #10 — 5 controllers)*
- [x] **M2-048**: Create RabbitMQ publishers — BloodStockUpdatedEvent, StockCriticalEvent, UnitExpiringEvent *(PR #10)*
- [x] **M2-049**: Create RabbitMQ listener — DonationCompletedEvent, TestResultAvailableEvent, UnitReleasedEvent *(PR #10)*
- [x] **M2-050**: Implement FIFO/FEFO dispatch logic *(PR #10 — expiryDateAsc ordering)*
- [x] **M2-051**: Implement real-time stock dashboard data endpoints *(PR #10)*
- [x] **M2-052**: Create application.yml configs *(PR #10)*
- [x] **M2-053**: Write unit tests (>80% coverage) *(PR #10)*
- [x] **M2-054**: Write integration tests with Testcontainers *(PR #10)*

## Deliverables

1. 4 running microservices with full CRUD APIs
2. RabbitMQ event publishing/consuming between services
3. Redis caching for branch/master data
4. >80% unit test coverage per service
5. Integration tests with Testcontainers passing
