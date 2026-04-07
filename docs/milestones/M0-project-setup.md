# M0: Project Setup & Architecture

**Duration:** 2 weeks
**Dependencies:** None
**Exit Gate:** Stakeholder approval on architecture docs

## 📊 Development Status: ✅ COMPLETE (100%)

**Completed:** 2026-04-04 | **PRs:** #1 (skills/hooks/milestones), #2 (agent consolidation), #3 (ADRs/ERD/RBAC/templates)
**Issues Completed:** 24/24

---

## Objective

Establish project foundation — architecture decisions, documentation, tooling, repository structure, and development workflow.

## Issues

### Architecture & Design
- [x] **M0-001**: Define system architecture and create C4 diagrams (Context, Container, Component)
- [x] **M0-002**: Write Architecture Decision Records (ADRs) for all key decisions *(PR #3 — 6 ADRs)*
  - Single shared database vs database-per-service
  - No Lombok — Java 21 records instead
  - RabbitMQ for async actions only
  - 4-layer branch data isolation
  - URI-based API versioning
  - Keycloak single realm with group hierarchy
- [x] **M0-003**: Create detailed Entity Relationship Diagram (ERD) for all ~87 tables *(PR #3)*
- [x] **M0-004**: Define all 15 RabbitMQ event contracts (routing keys, payloads, consumers) *(PR #3)*
- [x] **M0-005**: Design API contracts for all 14 services (OpenAPI 3.0 specs)

### Requirements & Planning
- [x] **M0-006**: Write functional requirements for all 24 modules
- [x] **M0-007**: Write non-functional requirements (performance, security, compliance)
- [x] **M0-008**: Create user stories for all 16 roles
- [x] **M0-009**: Define acceptance criteria for each module
- [x] **M0-010**: Map regulatory compliance requirements (HIPAA, GDPR, FDA, AABB, WHO)

### Security Design
- [x] **M0-011**: Create threat model for blood bank system
- [x] **M0-012**: Define RBAC matrix — all 16 roles × all API endpoints *(PR #3)*
- [x] **M0-013**: Design 4-layer branch data isolation architecture *(PR #3)*
- [x] **M0-014**: Define security policies (dual authorization, break-glass, data masking)

### UI/UX Design
- [x] **M0-015**: Design system — color palette, typography, spacing, components
- [x] **M0-016**: Create wireframes for all 17 frontend feature modules
- [x] **M0-017**: Define navigation structure for 3 portals (Staff, Hospital, Donor)
- [x] **M0-018**: Define role-based UI filtering rules

### Repository Setup
- [x] **M0-019**: Initialize Git repository with branch protection rules
- [x] **M0-020**: Create CLAUDE.md with all project rules and patterns *(PR #1)*
- [x] **M0-021**: Create .claude/ directory with skills, hooks, and commands *(PR #1)*
- [x] **M0-022**: Create GitHub issue templates and PR templates *(PR #3)*
- [x] **M0-023**: Set up GitHub milestones matching M0–M13 *(PR #1)*
- [x] **M0-024**: Create development branch strategy documentation *(PR #2)*

## Deliverables

1. Architecture documentation (C4 diagrams, ADRs, ERD)
2. Requirements document (functional + non-functional)
3. Security design document (threat model, RBAC matrix)
4. UI/UX design system and wireframes
5. API contract specifications (OpenAPI)
6. Event contract specifications
7. Repository with CLAUDE.md, README.md, .claude/ configuration
