# M2: Core Services — donor, branch, lab, inventory

**Duration:** 2 weeks
**Dependencies:** M1 (Foundation)
**Exit Gate:** All 4 core services running with unit + integration tests passing

---

## Objective

Implement the 4 foundational microservices that handle the core blood bank workflow: donor registration, branch management, laboratory testing, and inventory management.

## Issues

### branch-service (Modules 8, 17) — Start First (others depend on it)
- [ ] **M2-001**: Scaffold branch-service project structure
- [ ] **M2-002**: Create entities — Branch, BranchOperatingHours, BranchEquipment, BranchRegion
- [ ] **M2-003**: Create entities — BloodGroup, ComponentType, DeferralReason, ReactionType, Country, Region, City, IcdCode
- [ ] **M2-004**: Create DTOs (records) for all branch entities
- [ ] **M2-005**: Create MapStruct mappers for all branch entities
- [ ] **M2-006**: Create repositories with custom query methods
- [ ] **M2-007**: Create services — BranchService, MasterDataService
- [ ] **M2-008**: Create controllers — BranchController, MasterDataController (with @PreAuthorize)
- [ ] **M2-009**: Configure Redis caching for master data (blood groups, countries, component types)
- [ ] **M2-010**: Create application.yml configs (dev, prod)
- [ ] **M2-011**: Write unit tests (>80% coverage)
- [ ] **M2-012**: Write integration tests with Testcontainers

### donor-service (Modules 1, 2, 9, 24)
- [ ] **M2-013**: Scaffold donor-service project structure
- [ ] **M2-014**: Create entities — Donor, DonorHealthRecord, DonorDeferral, DonorConsent, DonorLoyalty
- [ ] **M2-015**: Create entities — Collection, CollectionAdverseReaction, CollectionSample
- [ ] **M2-016**: Create entities — BloodCamp, CampResource, CampDonor, CampCollection
- [ ] **M2-017**: Create DTOs (records) for all donor entities
- [ ] **M2-018**: Create MapStruct mappers
- [ ] **M2-019**: Create repositories
- [ ] **M2-020**: Create services — DonorService, CollectionService, BloodCampService, DonorLoyaltyService
- [ ] **M2-021**: Create controllers with @PreAuthorize per role matrix
- [ ] **M2-022**: Create RabbitMQ publishers — DonationCompletedEvent, CampCompletedEvent
- [ ] **M2-023**: Create RabbitMQ listener — EmergencyRequestEvent
- [ ] **M2-024**: Create application.yml configs
- [ ] **M2-025**: Write unit tests (>80% coverage)
- [ ] **M2-026**: Write integration tests with Testcontainers

### lab-service (Module 3)
- [ ] **M2-027**: Scaffold lab-service project structure
- [ ] **M2-028**: Create entities — TestOrder, TestResult, TestPanel, LabInstrument, QualityControlRecord
- [ ] **M2-029**: Create DTOs (records) for all lab entities
- [ ] **M2-030**: Create MapStruct mappers
- [ ] **M2-031**: Create repositories
- [ ] **M2-032**: Create services — TestOrderService, TestResultService, QualityControlService
- [ ] **M2-033**: Create controllers with @PreAuthorize (LAB_TECHNICIAN role)
- [ ] **M2-034**: Create RabbitMQ publishers — TestResultAvailableEvent, UnitReleasedEvent
- [ ] **M2-035**: Implement dual-review approval workflow for test results
- [ ] **M2-036**: Create application.yml configs
- [ ] **M2-037**: Write unit tests (>80% coverage)
- [ ] **M2-038**: Write integration tests with Testcontainers

### inventory-service (Modules 4, 5, 22)
- [ ] **M2-039**: Scaffold inventory-service project structure
- [ ] **M2-040**: Create entities — BloodUnit, BloodComponent, ComponentProcessing, ComponentLabel, PooledComponent
- [ ] **M2-041**: Create entities — StorageLocation, StockTransfer, UnitDisposal, UnitReservation
- [ ] **M2-042**: Create entities — TransportRequest, ColdChainLog, TransportBox, DeliveryConfirmation
- [ ] **M2-043**: Create DTOs (records) for all inventory entities
- [ ] **M2-044**: Create MapStruct mappers
- [ ] **M2-045**: Create repositories
- [ ] **M2-046**: Create services — BloodUnitService, ComponentService, StockService, TransferService, LogisticsService
- [ ] **M2-047**: Create controllers with @PreAuthorize (INVENTORY_MANAGER role)
- [ ] **M2-048**: Create RabbitMQ publishers — BloodStockUpdatedEvent, StockCriticalEvent, UnitExpiringEvent
- [ ] **M2-049**: Create RabbitMQ listener — DonationCompletedEvent, TestResultAvailableEvent, UnitReleasedEvent
- [ ] **M2-050**: Implement FIFO/FEFO dispatch logic
- [ ] **M2-051**: Implement real-time stock dashboard data endpoints
- [ ] **M2-052**: Create application.yml configs
- [ ] **M2-053**: Write unit tests (>80% coverage)
- [ ] **M2-054**: Write integration tests with Testcontainers

## Deliverables

1. 4 running microservices with full CRUD APIs
2. RabbitMQ event publishing/consuming between services
3. Redis caching for branch/master data
4. >80% unit test coverage per service
5. Integration tests with Testcontainers passing
