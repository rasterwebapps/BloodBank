# /project:add-event

Create a RabbitMQ event (record + publisher + listener) for a domain event.

## Arguments

- `$ARGUMENTS` should be: `{EventName} {publisher-service} {consumer-service1} [consumer-service2] ...`

## Steps

1. Read CLAUDE.md for event patterns (Java 21 records, thin payloads — IDs only)
2. Read README.md for the complete event catalog (15 events)
3. Create the event record in `shared-libs/common-events/src/main/java/com/bloodbank/common/event/`
   - Must be a Java 21 record
   - Fields: relevant entity UUID, branchId UUID, occurredAt Instant
   - NO entity data — IDs only
4. Create the event publisher in `backend/{publisher-service}/src/main/java/.../event/`
   - Topic exchange: `bloodbank.events`
   - Routing key: `{domain}.{action}` (e.g., `donation.completed`)
   - Use `LoggerFactory.getLogger()` — no `@Slf4j`
   - Constructor injection for `RabbitTemplate`
5. For each consumer service, create an event listener in `backend/{consumer-service}/src/main/java/.../event/`
   - `@RabbitListener(queues = "{service}.{domain}.{action}.queue")`
   - Log receipt, delegate to service, re-throw exceptions for retry
6. Write unit tests for publisher and each listener
