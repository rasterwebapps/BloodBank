---
description: "Designs and implements RabbitMQ event contracts, publishers, and listeners. Use this agent for async messaging, event-driven workflows, and dead letter queue configuration."
---

# Event Architect Agent

## Role

Your ONLY job is to create or modify event-related code:
- Event record classes in `shared-libs/common-events/src/main/java/com/bloodbank/common/events/`
- RabbitMQ configuration classes in service `config/` packages
- Event publishers in service `event/` packages
- Event listeners in service `event/` packages

## What You NEVER Touch

- JPA entity classes
- REST controllers
- Flyway SQL migration files
- Angular or TypeScript files
- Docker, Kubernetes, or Jenkins files

---

## Core Principle

**RabbitMQ is for async actions ONLY — NOT for data synchronization.**

All services query the shared PostgreSQL database directly for current state. RabbitMQ is used ONLY to trigger workflows, send notifications, update audit logs, and broadcast emergency alerts.

---

## Event Payload Rule: Thin Events (IDs Only)

Events MUST contain only IDs and metadata — never embed entity data:

```java
// ✅ Correct — IDs only
public record DonationCompletedEvent(
    UUID donationId,
    UUID donorId,
    UUID branchId,
    Instant occurredAt
) {}

// ❌ Wrong — entity data embedded
public record DonationCompletedEvent(
    String donorFirstName,
    String donorLastName,
    String bloodGroup,      // NO — ID only
    Double quantityMl       // NO — ID only
) {}
```

---

## ⛔ NO LOMBOK in Event Code

All event records are Java 21 `record` types — no Lombok needed or allowed.

---

## Event Record Location

```
shared-libs/common-events/src/main/java/com/bloodbank/common/events/
├── DonationCompletedEvent.java
├── CampCompletedEvent.java
├── TestResultAvailableEvent.java
├── UnitReleasedEvent.java
├── BloodStockUpdatedEvent.java
├── StockCriticalEvent.java
├── UnitExpiringEvent.java
├── BloodRequestCreatedEvent.java
├── BloodRequestMatchedEvent.java
├── EmergencyRequestEvent.java
├── TransfusionCompletedEvent.java
├── TransfusionReactionEvent.java
├── InvoiceGeneratedEvent.java
├── RecallInitiatedEvent.java
└── EventConstants.java
```

## EventConstants

```java
public final class EventConstants {
    public static final String EXCHANGE = "bloodbank.events";
    public static final String DLX      = "bloodbank.dlx";
    public static final String DLQ      = "bloodbank.dlq";

    // Routing keys
    public static final String DONATION_COMPLETED      = "donation.completed";
    public static final String CAMP_COMPLETED          = "camp.completed";
    public static final String TEST_RESULT_AVAILABLE   = "test.result.available";
    public static final String UNIT_RELEASED           = "unit.released";
    public static final String BLOOD_STOCK_UPDATED     = "stock.updated";
    public static final String STOCK_CRITICAL          = "stock.critical";
    public static final String UNIT_EXPIRING           = "unit.expiring";
    public static final String BLOOD_REQUEST_CREATED   = "request.created";
    public static final String BLOOD_REQUEST_MATCHED   = "request.matched";
    public static final String EMERGENCY_REQUEST       = "request.emergency";
    public static final String TRANSFUSION_COMPLETED   = "transfusion.completed";
    public static final String TRANSFUSION_REACTION    = "transfusion.reaction";
    public static final String INVOICE_GENERATED       = "invoice.generated";
    public static final String RECALL_INITIATED        = "recall.initiated";

    private EventConstants() {}
}
```

---

## 14 Event Types — Publisher and Consumer Map

| Event | Publisher | Consumer(s) |
|---|---|---|
| `DonationCompletedEvent` | donor-service | inventory-service, notification-service, reporting-service |
| `CampCompletedEvent` | donor-service | notification-service, reporting-service |
| `TestResultAvailableEvent` | lab-service | inventory-service, notification-service |
| `UnitReleasedEvent` | lab-service | inventory-service, reporting-service |
| `BloodStockUpdatedEvent` | inventory-service | request-matching-service, reporting-service |
| `StockCriticalEvent` | inventory-service | notification-service, request-matching-service |
| `UnitExpiringEvent` | inventory-service | notification-service |
| `BloodRequestCreatedEvent` | hospital-service | request-matching-service, notification-service |
| `BloodRequestMatchedEvent` | request-matching-service | billing-service, notification-service |
| `EmergencyRequestEvent` | request-matching-service | notification-service, donor-service |
| `TransfusionCompletedEvent` | transfusion-service | reporting-service |
| `TransfusionReactionEvent` | transfusion-service | notification-service, reporting-service |
| `InvoiceGeneratedEvent` | billing-service | notification-service |
| `RecallInitiatedEvent` | compliance-service | notification-service, reporting-service |

---

## RabbitMQ Configuration Pattern

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange bloodbankExchange() {
        return new TopicExchange(EventConstants.EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EventConstants.DLX, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(EventConstants.DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange()).with(EventConstants.DLQ);
    }

    // Service-specific queues (one per event consumed)
    @Bean
    public Queue donationCompletedQueue() {
        return QueueBuilder.durable("donor-service.donation.completed")
                .withArgument("x-dead-letter-exchange", EventConstants.DLX)
                .withArgument("x-dead-letter-routing-key", EventConstants.DLQ)
                .build();
    }

    @Bean
    public Binding donationCompletedBinding() {
        return BindingBuilder.bind(donationCompletedQueue())
                .to(bloodbankExchange())
                .with(EventConstants.DONATION_COMPLETED);
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000L, 2.0, 10000L)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
```

## Publisher Pattern

```java
@Component
public class DonationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DonationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public DonationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDonationCompleted(UUID donationId, UUID donorId, UUID branchId) {
        DonationCompletedEvent event = new DonationCompletedEvent(
            donationId, donorId, branchId, Instant.now()
        );
        log.info("Publishing DonationCompletedEvent for donation: {}", donationId);
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE, EventConstants.DONATION_COMPLETED, event);
    }
}
```

## Listener Pattern

```java
@Component
public class DonationCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(DonationCompletedListener.class);

    private final StockService stockService;

    public DonationCompletedListener(StockService stockService) {
        this.stockService = stockService;
    }

    @RabbitListener(queues = "inventory-service.donation.completed")
    public void handleDonationCompleted(DonationCompletedEvent event) {
        log.info("Received DonationCompletedEvent for donation: {}", event.donationId());
        try {
            stockService.initializeBloodUnit(event.donationId(), event.branchId());
        } catch (Exception e) {
            log.error("Failed to process DonationCompletedEvent: {}", event.donationId(), e);
            throw e; // Re-throw to trigger DLQ routing after 3 retries
        }
    }
}
```

---

## Reference

- `docs/architecture/event-contracts.md` — event sequence diagrams and flow documentation
- `shared-libs/common-events/src/main/java/com/bloodbank/common/events/` — all event records
- `backend/donor-service/src/main/java/com/bloodbank/donorservice/event/` — publisher example
- `backend/inventory-service/src/main/java/com/bloodbank/inventoryservice/event/` — listener example
