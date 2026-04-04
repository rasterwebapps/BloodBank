# M3: Clinical Services — transfusion, hospital, request-matching

**Duration:** 2 weeks
**Dependencies:** M2 (Core Services)
**Exit Gate:** Clinical workflow end-to-end test passes

---

## Objective

Implement the clinical workflow services: blood transfusion management, hospital integration, and emergency request matching.

## Issues

### transfusion-service (Modules 6, 7)
- [ ] **M3-001**: Scaffold transfusion-service project structure
- [ ] **M3-002**: Create entities — CrossMatchRequest, CrossMatchResult, BloodIssue, EmergencyIssue
- [ ] **M3-003**: Create entities — Transfusion, TransfusionReaction, HemovigilanceReport, LookBackInvestigation
- [ ] **M3-004**: Create DTOs (records) for all transfusion entities
- [ ] **M3-005**: Create MapStruct mappers
- [ ] **M3-006**: Create repositories with cross-match query methods
- [ ] **M3-007**: Create services — CrossMatchService, BloodIssueService, TransfusionService, HemovigilanceService
- [ ] **M3-008**: Implement ABO/Rh compatibility checking algorithm
- [ ] **M3-009**: Implement emergency O-negative issue protocol
- [ ] **M3-010**: Create controllers with @PreAuthorize (DOCTOR, NURSE roles)
- [ ] **M3-011**: Create RabbitMQ publishers — TransfusionCompletedEvent, TransfusionReactionEvent
- [ ] **M3-012**: Implement look-back investigation workflow
- [ ] **M3-013**: Write unit tests (>80% coverage)
- [ ] **M3-014**: Write integration tests with Testcontainers

### hospital-service (Module 10)
- [ ] **M3-015**: Scaffold hospital-service project structure
- [ ] **M3-016**: Create entities — Hospital, HospitalContract, HospitalRequest, HospitalFeedback
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
- [ ] **M3-026**: Scaffold request-matching-service project structure
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
