# ADR-001: Single Shared Database

**Status:** Accepted
**Date:** 2026-04-04
**Decision Makers:** Architecture Team

## Context

BloodBank is a 14-microservice system managing the complete blood lifecycle. A fundamental architectural decision is whether each service should own its own database (database-per-service) or all services should share a single database.

Blood bank operations have strong referential integrity requirements:

- A blood unit links to a donor, a collection, test results, a storage location, a cross-match, an issue record, and a transfusion — spanning 6+ services.
- Regulatory compliance (FDA 21 CFR Part 11, AABB) demands vein-to-vein traceability with zero data inconsistency.
- Cross-match and issuing are time-critical operations that cannot tolerate eventual consistency.
- Audit trails must be atomic with the operations they record.

## Decision

All 14 microservices connect to **one PostgreSQL 17 database** (`bloodbank_db`) containing ~87 tables.

- Flyway migrations are centralized in `shared-libs/db-migration/`.
- A Kubernetes Job runs Flyway **before** any service starts.
- Each service sets `spring.flyway.enabled=false` in its `application.yml`.
- Services access **only their own domain tables** (enforced by convention and code review).

## Consequences

### Positive

- **Referential integrity** — Foreign keys enforced at the database level across all domain boundaries.
- **Transactional consistency** — Cross-domain operations (e.g., donation → inventory update) are ACID-compliant without distributed transactions (no Saga pattern needed).
- **Simplified querying** — Reporting and analytics can join across domains without data replication.
- **Regulatory compliance** — Vein-to-vein traceability is a simple SQL join, not a distributed query.
- **Reduced operational complexity** — One database to back up, monitor, tune, and replicate.
- **No data synchronization** — No need for CDC, event sourcing, or eventual consistency patterns for data access.

### Negative

- **Coupling risk** — Schema changes can theoretically affect multiple services. Mitigated by: (a) centralized Flyway migrations, (b) each service accesses only its own tables, (c) shared migration review process.
- **Scaling ceiling** — A single database may become a bottleneck at extreme scale. Mitigated by: PostgreSQL 17 partitioning, read replicas, Redis caching for hot data (branch info, stock levels, master data).
- **Deployment coordination** — Flyway must run before services start. Mitigated by Kubernetes Job with init containers ensuring migration completes first.

### Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| Database-per-service | Unacceptable for healthcare — distributed transactions introduce data inconsistency risk; regulatory compliance demands atomic cross-domain traceability |
| Event sourcing + CQRS | Over-engineered for a system that needs strong consistency; adds complexity without benefit given the shared DB |
| Polyglot persistence | No domain requires a non-relational model; PostgreSQL 17 handles JSON, full-text search, and geospatial data natively |

## References

- CLAUDE.md — Architecture Rules → Single Shared Database
- AABB Standards — Vein-to-vein traceability requirements
- FDA 21 CFR Part 11 — Electronic records integrity
