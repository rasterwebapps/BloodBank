# M0: Project Setup & Architecture

**Duration:** 2 weeks
**Dependencies:** None
**Exit Gate:** Stakeholder approval on architecture docs

---

## Objective

Establish project foundation — architecture decisions, documentation, tooling, repository structure, and development workflow.

## Issues

### Architecture & Design
- [ ] **M0-001**: Define system architecture and create C4 diagrams (Context, Container, Component)
- [ ] **M0-002**: Write Architecture Decision Records (ADRs) for all key decisions
  - Single shared database vs database-per-service
  - No Lombok — Java 21 records instead
  - RabbitMQ for async actions only
  - 4-layer branch data isolation
  - URI-based API versioning
  - Keycloak single realm with group hierarchy
- [ ] **M0-003**: Create detailed Entity Relationship Diagram (ERD) for all ~87 tables
- [ ] **M0-004**: Define all 15 RabbitMQ event contracts (routing keys, payloads, consumers)
- [ ] **M0-005**: Design API contracts for all 14 services (OpenAPI 3.0 specs)

### Requirements & Planning
- [ ] **M0-006**: Write functional requirements for all 24 modules
- [ ] **M0-007**: Write non-functional requirements (performance, security, compliance)
- [ ] **M0-008**: Create user stories for all 16 roles
- [ ] **M0-009**: Define acceptance criteria for each module
- [ ] **M0-010**: Map regulatory compliance requirements (HIPAA, GDPR, FDA, AABB, WHO)

### Security Design
- [ ] **M0-011**: Create threat model for blood bank system
- [ ] **M0-012**: Define RBAC matrix — all 16 roles × all API endpoints
- [ ] **M0-013**: Design 4-layer branch data isolation architecture
- [ ] **M0-014**: Define security policies (dual authorization, break-glass, data masking)

### UI/UX Design
- [ ] **M0-015**: Design system — color palette, typography, spacing, components
- [ ] **M0-016**: Create wireframes for all 17 frontend feature modules
- [ ] **M0-017**: Define navigation structure for 3 portals (Staff, Hospital, Donor)
- [ ] **M0-018**: Define role-based UI filtering rules

### Repository Setup
- [ ] **M0-019**: Initialize Git repository with branch protection rules
- [ ] **M0-020**: Create CLAUDE.md with all project rules and patterns
- [ ] **M0-021**: Create .claude/ directory with skills, hooks, and commands
- [ ] **M0-022**: Create GitHub issue templates and PR templates
- [ ] **M0-023**: Set up GitHub milestones matching M0–M13
- [ ] **M0-024**: Create development branch strategy documentation

## Deliverables

1. Architecture documentation (C4 diagrams, ADRs, ERD)
2. Requirements document (functional + non-functional)
3. Security design document (threat model, RBAC matrix)
4. UI/UX design system and wireframes
5. API contract specifications (OpenAPI)
6. Event contract specifications
7. Repository with CLAUDE.md, README.md, .claude/ configuration
