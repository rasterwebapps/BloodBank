# Skill: Create RabbitMQ Event

Generate RabbitMQ event records, publishers, and listeners following BloodBank patterns.

## Rules

1. Events are **Java 21 records** — thin payloads carrying IDs ONLY
2. Topic Exchange: `bloodbank.events`
3. Routing key pattern: `{domain}.{action}` (e.g., `donation.completed`)
4. Dead Letter: `bloodbank.dlx` → `bloodbank.dlq`
5. Retry: 3 attempts, 1s backoff, 2x multiplier
6. Events go in `shared-libs/common-events/` for shared definitions
7. Publishers/listeners go in each service's `event/` package

## Event Record Template (in common-events)

```java
package com.bloodbank.common.event;

import java.time.Instant;
import java.util.UUID;

public record {Domain}{Action}Event(
    UUID {entityId},
    UUID {relatedId},
    UUID branchId,
    Instant occurredAt
) {}
```

## Event Publisher Template

```java
package com.bloodbank.{servicename}.event;

import com.bloodbank.common.event.{Domain}{Action}Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class {Domain}EventPublisher {

    private static final Logger log = LoggerFactory.getLogger({Domain}EventPublisher.class);

    private static final String EXCHANGE = "bloodbank.events";

    private final RabbitTemplate rabbitTemplate;

    public {Domain}EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish{Action}({Domain}{Action}Event event) {
        String routingKey = "{domain}.{action}";
        log.info("Publishing {}: routingKey={}, entityId={}", 
                event.getClass().getSimpleName(), routingKey, event.{entityId}());
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
    }
}
```

## Event Listener Template

```java
package com.bloodbank.{servicename}.event;

import com.bloodbank.common.event.{Domain}{Action}Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class {Domain}EventListener {

    private static final Logger log = LoggerFactory.getLogger({Domain}EventListener.class);

    private final {HandlerService} handlerService;

    public {Domain}EventListener({HandlerService} handlerService) {
        this.handlerService = handlerService;
    }

    @RabbitListener(queues = "{servicename}.{domain}.{action}.queue")
    public void handle{Action}({Domain}{Action}Event event) {
        log.info("Received {}: entityId={}, branchId={}",
                event.getClass().getSimpleName(), event.{entityId}(), event.branchId());
        try {
            handlerService.process{Action}(event);
        } catch (Exception e) {
            log.error("Failed to process {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            throw e; // Let RabbitMQ retry
        }
    }
}
```

## 15 Event Types Reference

| Event | Routing Key | Publisher | Consumers |
|---|---|---|---|
| DonationCompletedEvent | donation.completed | donor-service | inventory, notification, reporting |
| CampCompletedEvent | camp.completed | donor-service | notification, reporting |
| TestResultAvailableEvent | test.result.available | lab-service | inventory, notification |
| UnitReleasedEvent | unit.released | lab-service | inventory, reporting |
| BloodStockUpdatedEvent | stock.updated | inventory-service | matching, reporting |
| StockCriticalEvent | stock.critical | inventory-service | notification, matching |
| UnitExpiringEvent | unit.expiring | inventory-service | notification |
| BloodRequestCreatedEvent | request.created | hospital-service | matching, notification |
| BloodRequestMatchedEvent | request.matched | matching-service | billing, notification |
| EmergencyRequestEvent | emergency.request | matching-service | notification, donor |
| TransfusionCompletedEvent | transfusion.completed | transfusion-service | reporting |
| TransfusionReactionEvent | transfusion.reaction | transfusion-service | notification, reporting |
| InvoiceGeneratedEvent | invoice.generated | billing-service | notification |
| RecallInitiatedEvent | recall.initiated | compliance-service | notification, reporting |

## Validation

- [ ] Events are Java 21 records (not classes)
- [ ] Events carry IDs only — NO embedded entity data
- [ ] Events include `branchId` and `occurredAt` fields
- [ ] Publisher uses topic exchange `bloodbank.events`
- [ ] Listener uses explicit queue name
- [ ] Both publisher and listener use `LoggerFactory.getLogger()`
- [ ] Listener re-throws exceptions for RabbitMQ retry
- [ ] No Lombok annotations
