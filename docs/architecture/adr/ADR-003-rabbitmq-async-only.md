# ADR-003: RabbitMQ for Async Actions Only — Thin Events (IDs Only)

**Status:** Accepted
**Date:** 2026-04-04
**Decision Makers:** Architecture Team

## Context

In a database-per-service architecture, messaging systems like RabbitMQ are often used for data synchronization between services (event-carried state transfer). Since BloodBank uses a single shared database (see ADR-001), all services can query the database directly — eliminating the need for data synchronization via messaging.

However, there are still valid use cases for asynchronous communication:

- **Notifications** — Email, SMS, and push alerts triggered by domain events.
- **Audit logging** — Immutable audit trail written asynchronously to avoid blocking operations.
- **Workflow triggers** — Donation completion triggers inventory update; test result triggers unit release.
- **Emergency broadcasts** — Urgent blood requests and disaster mobilization alerts.
- **Scheduled job coordination** — Expiry checks, stock rebalancing.

## Decision

RabbitMQ 3.13+ is used **exclusively for async action triggers**. It is **not** used for data synchronization.

### Configuration

| Setting | Value |
|---|---|
| Topic Exchange | `bloodbank.events` |
| Dead Letter Exchange | `bloodbank.dlx` |
| Dead Letter Queue | `bloodbank.dlq` |
| Retry Policy | 3 attempts, 1s initial backoff, 2x multiplier |
| Serialization | Jackson JSON with type headers |

### Event Payload Rule

All events carry **IDs only** — never full entity data. Consumers query the shared database for details.

```java
// ✅ CORRECT — thin event with IDs only
public record DonationCompletedEvent(
    UUID donationId,
    UUID donorId,
    UUID branchId,
    Instant occurredAt
) {}

// ❌ WRONG — never embed entity data in events
public record DonationCompletedEvent(
    UUID donationId,
    String donorFirstName,    // NO
    String donorLastName,     // NO
    BloodGroupEnum bloodGroup // NO
) {}
```

### 15 Event Types

The system defines 15 domain events across 7 publisher services:

| Event | Publisher | Consumers |
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

## Consequences

### Positive

- **No data duplication** — Events carry IDs; consumers query the single source of truth (database).
- **No eventual consistency issues** — Data is always current when consumers read from the DB.
- **Small event payloads** — UUIDs and timestamps are tiny, reducing RabbitMQ memory and network usage.
- **Simple event schema evolution** — Adding an ID field is non-breaking; adding entity fields creates coupling.
- **Dead letter handling** — Failed messages go to DLQ for investigation without data loss.

### Negative

- **Consumer DB queries** — Each consumer must query the database after receiving an event. Mitigated by Redis caching for hot data and the database being local (low latency).
- **Event ordering** — RabbitMQ does not guarantee strict ordering across queues. Mitigated by idempotent consumers and timestamp-based deduplication.

### Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| Event-carried state transfer (fat events) | Unnecessary — shared DB means no data sync needed; fat events create coupling and consistency issues |
| Apache Kafka | Over-engineered for action triggers; Kafka is optimized for event streaming/replay, not request-response workflows |
| REST-based sync communication | Synchronous inter-service calls create tight coupling and cascade failures; async is more resilient |
| No messaging at all | Notifications and audit logging benefit from async decoupling; polling-based alternatives are less efficient |

## References

- CLAUDE.md — Architecture Rules → RabbitMQ — Async Actions Only
- ADR-001 — Single Shared Database (eliminates data sync use case)
