# RabbitMQ Event Contracts

This document defines all 15 RabbitMQ domain events in the BloodBank system.

## Design Principles

1. **Async actions only** — RabbitMQ is not used for data synchronization (see [ADR-003](adr/ADR-003-rabbitmq-async-only.md)).
2. **Thin payloads** — Events carry IDs only. Consumers query the shared database for full entity data.
3. **Java 21 records** — All events are defined as immutable Java records in `shared-libs/common-events/`.
4. **Topic exchange** — All events are published to the `bloodbank.events` topic exchange with routing keys.

## Infrastructure Configuration

| Setting | Value |
|---|---|
| Exchange Name | `bloodbank.events` |
| Exchange Type | Topic |
| Dead Letter Exchange | `bloodbank.dlx` |
| Dead Letter Queue | `bloodbank.dlq` |
| Retry Policy | 3 attempts, 1s initial backoff, 2x multiplier |
| Serialization | Jackson JSON with `__TypeId__` header |
| Durable | Yes (exchange and all queues) |

## Routing Key Convention

```
bloodbank.{domain}.{action}
```

Examples: `bloodbank.donation.completed`, `bloodbank.stock.critical`

---

## Event Definitions

### 1. DonationCompletedEvent

| Field | Value |
|---|---|
| **Record Name** | `DonationCompletedEvent` |
| **Routing Key** | `bloodbank.donation.completed` |
| **Publisher** | donor-service |
| **Consumers** | inventory-service, notification-service, reporting-service |
| **Trigger** | A blood donation/collection is completed successfully |

```java
public record DonationCompletedEvent(
    UUID donationId,
    UUID donorId,
    UUID branchId,
    Instant occurredAt
) {}
```

---

### 2. CampCompletedEvent

| Field | Value |
|---|---|
| **Record Name** | `CampCompletedEvent` |
| **Routing Key** | `bloodbank.camp.completed` |
| **Publisher** | donor-service |
| **Consumers** | notification-service, reporting-service |
| **Trigger** | A blood camp event is marked as completed |

```java
public record CampCompletedEvent(
    UUID campId,
    UUID branchId,
    int totalCollections,
    Instant occurredAt
) {}
```

---

### 3. TestResultAvailableEvent

| Field | Value |
|---|---|
| **Record Name** | `TestResultAvailableEvent` |
| **Routing Key** | `bloodbank.test.result-available` |
| **Publisher** | lab-service |
| **Consumers** | inventory-service, notification-service |
| **Trigger** | Lab test results are finalized for a collection sample |

```java
public record TestResultAvailableEvent(
    UUID testOrderId,
    UUID collectionId,
    UUID branchId,
    Instant occurredAt
) {}
```

---

### 4. UnitReleasedEvent

| Field | Value |
|---|---|
| **Record Name** | `UnitReleasedEvent` |
| **Routing Key** | `bloodbank.unit.released` |
| **Publisher** | lab-service |
| **Consumers** | inventory-service, reporting-service |
| **Trigger** | A blood unit passes all tests and is released from quarantine |

```java
public record UnitReleasedEvent(
    UUID bloodUnitId,
    UUID collectionId,
    UUID branchId,
    Instant occurredAt
) {}
```

---

### 5. BloodStockUpdatedEvent

| Field | Value |
|---|---|
| **Record Name** | `BloodStockUpdatedEvent` |
| **Routing Key** | `bloodbank.stock.updated` |
| **Publisher** | inventory-service |
| **Consumers** | request-matching-service, reporting-service |
| **Trigger** | Stock levels change (unit added, issued, expired, transferred, or disposed) |

```java
public record BloodStockUpdatedEvent(
    UUID bloodComponentId,
    UUID branchId,
    String updateType,
    Instant occurredAt
) {}
```

---

### 6. StockCriticalEvent

| Field | Value |
|---|---|
| **Record Name** | `StockCriticalEvent` |
| **Routing Key** | `bloodbank.stock.critical` |
| **Publisher** | inventory-service |
| **Consumers** | notification-service, request-matching-service |
| **Trigger** | Stock for a blood group/component type falls below the configured threshold at a branch |

```java
public record StockCriticalEvent(
    UUID branchId,
    UUID bloodGroupId,
    UUID componentTypeId,
    int currentStock,
    int threshold,
    Instant occurredAt
) {}
```

---

### 7. UnitExpiringEvent

| Field | Value |
|---|---|
| **Record Name** | `UnitExpiringEvent` |
| **Routing Key** | `bloodbank.unit.expiring` |
| **Publisher** | inventory-service |
| **Consumers** | notification-service |
| **Trigger** | A blood component is approaching its expiry date (configurable threshold, e.g., 3 days) |

```java
public record UnitExpiringEvent(
    UUID bloodComponentId,
    UUID branchId,
    Instant expiryDate,
    Instant occurredAt
) {}
```

---

### 8. BloodRequestCreatedEvent

| Field | Value |
|---|---|
| **Record Name** | `BloodRequestCreatedEvent` |
| **Routing Key** | `bloodbank.request.created` |
| **Publisher** | hospital-service |
| **Consumers** | request-matching-service, notification-service |
| **Trigger** | A hospital submits a new blood request |

```java
public record BloodRequestCreatedEvent(
    UUID requestId,
    UUID hospitalId,
    UUID branchId,
    Instant occurredAt
) {}
```

---

### 9. BloodRequestMatchedEvent

| Field | Value |
|---|---|
| **Record Name** | `BloodRequestMatchedEvent` |
| **Routing Key** | `bloodbank.request.matched` |
| **Publisher** | request-matching-service |
| **Consumers** | billing-service, notification-service |
| **Trigger** | A blood request is matched with available inventory |

```java
public record BloodRequestMatchedEvent(
    UUID requestId,
    UUID bloodComponentId,
    UUID branchId,
    Instant occurredAt
) {}
```

---

### 10. EmergencyRequestEvent

| Field | Value |
|---|---|
| **Record Name** | `EmergencyRequestEvent` |
| **Routing Key** | `bloodbank.emergency.request` |
| **Publisher** | request-matching-service |
| **Consumers** | notification-service, donor-service |
| **Trigger** | A critical/emergency blood request is created that cannot be fulfilled from current stock |

```java
public record EmergencyRequestEvent(
    UUID emergencyRequestId,
    UUID branchId,
    UUID bloodGroupId,
    int unitsNeeded,
    Instant occurredAt
) {}
```

---

### 11. TransfusionCompletedEvent

| Field | Value |
|---|---|
| **Record Name** | `TransfusionCompletedEvent` |
| **Routing Key** | `bloodbank.transfusion.completed` |
| **Publisher** | transfusion-service |
| **Consumers** | reporting-service |
| **Trigger** | A blood transfusion is completed |

```java
public record TransfusionCompletedEvent(
    UUID transfusionId,
    UUID bloodIssueId,
    UUID branchId,
    Instant occurredAt
) {}
```

---

### 12. TransfusionReactionEvent

| Field | Value |
|---|---|
| **Record Name** | `TransfusionReactionEvent` |
| **Routing Key** | `bloodbank.transfusion.reaction` |
| **Publisher** | transfusion-service |
| **Consumers** | notification-service, reporting-service |
| **Trigger** | An adverse transfusion reaction is reported |

```java
public record TransfusionReactionEvent(
    UUID transfusionReactionId,
    UUID transfusionId,
    UUID branchId,
    Instant occurredAt
) {}
```

---

### 13. InvoiceGeneratedEvent

| Field | Value |
|---|---|
| **Record Name** | `InvoiceGeneratedEvent` |
| **Routing Key** | `bloodbank.invoice.generated` |
| **Publisher** | billing-service |
| **Consumers** | notification-service |
| **Trigger** | An invoice is generated and ready for delivery to a hospital |

```java
public record InvoiceGeneratedEvent(
    UUID invoiceId,
    UUID hospitalId,
    UUID branchId,
    Instant occurredAt
) {}
```

---

### 14. RecallInitiatedEvent

| Field | Value |
|---|---|
| **Record Name** | `RecallInitiatedEvent` |
| **Routing Key** | `bloodbank.recall.initiated` |
| **Publisher** | compliance-service |
| **Consumers** | notification-service, reporting-service |
| **Trigger** | A product recall is initiated (e.g., donor tested positive post-donation) |

```java
public record RecallInitiatedEvent(
    UUID recallId,
    UUID branchId,
    String recallType,
    Instant occurredAt
) {}
```

---

## Event Flow Summary

```
donor-service ──┬── DonationCompletedEvent ──> inventory-service, notification-service, reporting-service
                └── CampCompletedEvent ──────> notification-service, reporting-service

lab-service ────┬── TestResultAvailableEvent ─> inventory-service, notification-service
                └── UnitReleasedEvent ────────> inventory-service, reporting-service

inventory-service ┬── BloodStockUpdatedEvent ──> request-matching-service, reporting-service
                  ├── StockCriticalEvent ──────> notification-service, request-matching-service
                  └── UnitExpiringEvent ───────> notification-service

hospital-service ─── BloodRequestCreatedEvent ─> request-matching-service, notification-service

request-matching  ┬── BloodRequestMatchedEvent ─> billing-service, notification-service
                  └── EmergencyRequestEvent ────> notification-service, donor-service

transfusion-svc ──┬── TransfusionCompletedEvent > reporting-service
                  └── TransfusionReactionEvent ─> notification-service, reporting-service

billing-service ──── InvoiceGeneratedEvent ────> notification-service

compliance-svc ───── RecallInitiatedEvent ─────> notification-service, reporting-service
```

## Queue Naming Convention

Each consumer binds a queue named:

```
{consumer-service}.{event-name}
```

Examples:
- `inventory-service.donation-completed`
- `notification-service.stock-critical`
- `reporting-service.transfusion-completed`

## Dead Letter Handling

Failed messages (after 3 retry attempts) are routed to `bloodbank.dlq` with the original routing key preserved in the `x-death` header. A monitoring alert triggers when DLQ depth exceeds a configured threshold.

## Consumer Idempotency

All consumers must be idempotent. Use the event's unique ID fields (e.g., `donationId`, `invoiceId`) to detect and skip duplicate processing.
