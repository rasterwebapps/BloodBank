package com.bloodbank.integration.event;

import com.bloodbank.common.events.BloodRequestCreatedEvent;
import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.events.EmergencyRequestEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.InvoiceGeneratedEvent;
import com.bloodbank.common.events.TransfusionCompletedEvent;
import com.bloodbank.integration.support.AbstractWorkflowIntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-012: Idempotency — same event published twice is processed only once.
 *
 * Verifies that an idempotent consumer, which deduplicates messages using an in-memory
 * set of processed event IDs, does not process the same event a second time.
 *
 * Pattern under test:
 *   1. Consumer receives an event.
 *   2. Consumer checks whether the event's primary ID is already in a processed-IDs set.
 *   3. If yes  -> skip processing (idempotent guard triggered).
 *   4. If no   -> process event, add ID to processed-IDs set.
 *   5. Second publish of the same event is received but skipped.
 *
 * This test covers five representative event types from different domains:
 *   - DonationCompletedEvent    (donor domain)
 *   - BloodRequestCreatedEvent  (hospital domain)
 *   - TransfusionCompletedEvent (transfusion domain)
 *   - InvoiceGeneratedEvent     (billing domain)
 *   - EmergencyRequestEvent     (emergency domain)
 */
@DisplayName("M6-012: Event Idempotency — same event published twice, processed only once")
class EventIdempotencyIntegrationTest extends AbstractWorkflowIntegrationTest {

    // One idempotency queue per event type tested
    private static final String DONATION_IDEMPOTENCY_QUEUE      = "idempotency.donation.completed.queue";
    private static final String REQUEST_IDEMPOTENCY_QUEUE       = "idempotency.blood.request.created.queue";
    private static final String TRANSFUSION_IDEMPOTENCY_QUEUE   = "idempotency.transfusion.completed.queue";
    private static final String INVOICE_IDEMPOTENCY_QUEUE       = "idempotency.invoice.generated.queue";
    private static final String EMERGENCY_IDEMPOTENCY_QUEUE     = "idempotency.emergency.request.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        declareAndBindQueue(admin, exchange, DONATION_IDEMPOTENCY_QUEUE,    EventConstants.DONATION_COMPLETED);
        declareAndBindQueue(admin, exchange, REQUEST_IDEMPOTENCY_QUEUE,     EventConstants.BLOOD_REQUEST_CREATED);
        declareAndBindQueue(admin, exchange, TRANSFUSION_IDEMPOTENCY_QUEUE, EventConstants.TRANSFUSION_COMPLETED);
        declareAndBindQueue(admin, exchange, INVOICE_IDEMPOTENCY_QUEUE,     EventConstants.INVOICE_GENERATED);
        declareAndBindQueue(admin, exchange, EMERGENCY_IDEMPOTENCY_QUEUE,   EventConstants.EMERGENCY_REQUEST);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper: idempotent listener factory
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates a SimpleMessageListenerContainer whose listener implements an
     * in-memory idempotency guard based on a per-message ID extracted by
     * {@code idExtractor}.
     *
     * @param queueName   queue to listen on
     * @param processedIds shared set of already-processed IDs (must be thread-safe)
     * @param processedCount counter incremented when a message is actually processed
     * @param skippedCount   counter incremented when a duplicate is detected
     * @param expectedTotal  number of messages expected before latch releases
     * @param latch          released when expectedTotal messages have been seen
     * @param idExtractor    function to extract the deduplication ID from a raw message body
     */
    private SimpleMessageListenerContainer buildIdempotentContainer(
            String queueName,
            Set<String> processedIds,
            AtomicInteger processedCount,
            AtomicInteger skippedCount,
            int expectedTotal,
            CountDownLatch latch,
            java.util.function.Function<byte[], String> idExtractor) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener((Message message) -> {
            String id = idExtractor.apply(message.getBody());
            if (processedIds.contains(id)) {
                skippedCount.incrementAndGet();
            } else {
                processedIds.add(id);
                processedCount.incrementAndGet();
            }
            latch.countDown();
        });
        container.setPrefetchCount(1);
        return container;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DonationCompletedEvent idempotency
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DonationCompletedEvent idempotency")
    class DonationCompletedIdempotency {

        @Test
        @DisplayName("Same DonationCompletedEvent published twice should be processed only once")
        void sameDonationEventPublishedTwiceShouldBeProcessedOnlyOnce() throws InterruptedException {
            UUID donationId = UUID.randomUUID();
            UUID donorId    = UUID.randomUUID();
            UUID branchId   = UUID.randomUUID();

            Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            AtomicInteger processed  = new AtomicInteger(0);
            AtomicInteger skipped    = new AtomicInteger(0);
            CountDownLatch latch     = new CountDownLatch(2); // two deliveries expected

            SimpleMessageListenerContainer container = buildIdempotentContainer(
                    DONATION_IDEMPOTENCY_QUEUE,
                    processedIds, processed, skipped,
                    2, latch,
                    body -> extractFieldValue(body, "donationId"));
            container.start();

            try {
                DonationCompletedEvent event =
                        new DonationCompletedEvent(donationId, donorId, branchId, Instant.now());

                // Publish the same event twice (simulating at-least-once delivery)
                publishEvent(EventConstants.DONATION_COMPLETED, event);
                publishEvent(EventConstants.DONATION_COMPLETED, event);

                boolean allReceived = latch.await(10, TimeUnit.SECONDS);
                assertThat(allReceived)
                        .as("Both deliveries should arrive within timeout")
                        .isTrue();

                assertThat(processed.get())
                        .as("Event should be processed exactly once")
                        .isEqualTo(1);
                assertThat(skipped.get())
                        .as("Duplicate delivery should be skipped exactly once")
                        .isEqualTo(1);
                assertThat(processedIds)
                        .as("Processed-IDs set should contain exactly the donation ID")
                        .containsExactly(donationId.toString());
            } finally {
                container.stop();
            }
        }

        @Test
        @DisplayName("Two distinct DonationCompletedEvents should both be processed")
        void twoDistinctDonationEventsShouldBothBeProcessed() throws InterruptedException {
            UUID donationId1 = UUID.randomUUID();
            UUID donationId2 = UUID.randomUUID();
            UUID branchId    = UUID.randomUUID();

            Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            AtomicInteger processed  = new AtomicInteger(0);
            AtomicInteger skipped    = new AtomicInteger(0);
            CountDownLatch latch     = new CountDownLatch(2);

            SimpleMessageListenerContainer container = buildIdempotentContainer(
                    DONATION_IDEMPOTENCY_QUEUE,
                    processedIds, processed, skipped,
                    2, latch,
                    body -> extractFieldValue(body, "donationId"));
            container.start();

            try {
                publishEvent(EventConstants.DONATION_COMPLETED,
                        new DonationCompletedEvent(donationId1, UUID.randomUUID(), branchId, Instant.now()));
                publishEvent(EventConstants.DONATION_COMPLETED,
                        new DonationCompletedEvent(donationId2, UUID.randomUUID(), branchId, Instant.now()));

                boolean allReceived = latch.await(10, TimeUnit.SECONDS);
                assertThat(allReceived).isTrue();

                assertThat(processed.get())
                        .as("Both distinct events should be processed")
                        .isEqualTo(2);
                assertThat(skipped.get())
                        .as("No events should be skipped")
                        .isEqualTo(0);
                assertThat(processedIds).containsExactlyInAnyOrder(
                        donationId1.toString(), donationId2.toString());
            } finally {
                container.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BloodRequestCreatedEvent idempotency
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("BloodRequestCreatedEvent idempotency")
    class BloodRequestCreatedIdempotency {

        @Test
        @DisplayName("Same BloodRequestCreatedEvent published twice should be processed only once")
        void sameBloodRequestEventPublishedTwiceShouldBeProcessedOnlyOnce() throws InterruptedException {
            UUID requestId  = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId   = UUID.randomUUID();

            Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            AtomicInteger processed  = new AtomicInteger(0);
            AtomicInteger skipped    = new AtomicInteger(0);
            CountDownLatch latch     = new CountDownLatch(2);

            SimpleMessageListenerContainer container = buildIdempotentContainer(
                    REQUEST_IDEMPOTENCY_QUEUE,
                    processedIds, processed, skipped,
                    2, latch,
                    body -> extractFieldValue(body, "requestId"));
            container.start();

            try {
                BloodRequestCreatedEvent event =
                        new BloodRequestCreatedEvent(requestId, hospitalId, branchId, "A_POSITIVE", 2, Instant.now());

                publishEvent(EventConstants.BLOOD_REQUEST_CREATED, event);
                publishEvent(EventConstants.BLOOD_REQUEST_CREATED, event);

                boolean allReceived = latch.await(10, TimeUnit.SECONDS);
                assertThat(allReceived).isTrue();

                assertThat(processed.get()).isEqualTo(1);
                assertThat(skipped.get()).isEqualTo(1);
                assertThat(processedIds).containsExactly(requestId.toString());
            } finally {
                container.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // TransfusionCompletedEvent idempotency
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("TransfusionCompletedEvent idempotency")
    class TransfusionCompletedIdempotency {

        @Test
        @DisplayName("Same TransfusionCompletedEvent published twice should be processed only once")
        void sameTransfusionEventPublishedTwiceShouldBeProcessedOnlyOnce() throws InterruptedException {
            UUID transfusionId = UUID.randomUUID();
            UUID bloodUnitId   = UUID.randomUUID();
            UUID branchId      = UUID.randomUUID();

            Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            AtomicInteger processed  = new AtomicInteger(0);
            AtomicInteger skipped    = new AtomicInteger(0);
            CountDownLatch latch     = new CountDownLatch(2);

            SimpleMessageListenerContainer container = buildIdempotentContainer(
                    TRANSFUSION_IDEMPOTENCY_QUEUE,
                    processedIds, processed, skipped,
                    2, latch,
                    body -> extractFieldValue(body, "transfusionId"));
            container.start();

            try {
                TransfusionCompletedEvent event =
                        new TransfusionCompletedEvent(transfusionId, bloodUnitId, branchId, Instant.now());

                publishEvent(EventConstants.TRANSFUSION_COMPLETED, event);
                publishEvent(EventConstants.TRANSFUSION_COMPLETED, event);

                boolean allReceived = latch.await(10, TimeUnit.SECONDS);
                assertThat(allReceived).isTrue();

                assertThat(processed.get()).isEqualTo(1);
                assertThat(skipped.get()).isEqualTo(1);
            } finally {
                container.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // InvoiceGeneratedEvent idempotency
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("InvoiceGeneratedEvent idempotency")
    class InvoiceGeneratedIdempotency {

        @Test
        @DisplayName("Same InvoiceGeneratedEvent published twice should be processed only once")
        void sameInvoiceEventPublishedTwiceShouldBeProcessedOnlyOnce() throws InterruptedException {
            UUID invoiceId  = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId   = UUID.randomUUID();

            Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            AtomicInteger processed  = new AtomicInteger(0);
            AtomicInteger skipped    = new AtomicInteger(0);
            CountDownLatch latch     = new CountDownLatch(2);

            SimpleMessageListenerContainer container = buildIdempotentContainer(
                    INVOICE_IDEMPOTENCY_QUEUE,
                    processedIds, processed, skipped,
                    2, latch,
                    body -> extractFieldValue(body, "invoiceId"));
            container.start();

            try {
                InvoiceGeneratedEvent event =
                        new InvoiceGeneratedEvent(invoiceId, hospitalId, branchId, Instant.now());

                publishEvent(EventConstants.INVOICE_GENERATED, event);
                publishEvent(EventConstants.INVOICE_GENERATED, event);

                boolean allReceived = latch.await(10, TimeUnit.SECONDS);
                assertThat(allReceived).isTrue();

                assertThat(processed.get()).isEqualTo(1);
                assertThat(skipped.get()).isEqualTo(1);
                assertThat(processedIds).containsExactly(invoiceId.toString());
            } finally {
                container.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // EmergencyRequestEvent idempotency
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("EmergencyRequestEvent idempotency")
    class EmergencyRequestIdempotency {

        @Test
        @DisplayName("Same EmergencyRequestEvent published twice should be processed only once")
        void sameEmergencyEventPublishedTwiceShouldBeProcessedOnlyOnce() throws InterruptedException {
            UUID requestId = UUID.randomUUID();
            UUID branchId  = UUID.randomUUID();

            Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            AtomicInteger processed  = new AtomicInteger(0);
            AtomicInteger skipped    = new AtomicInteger(0);
            CountDownLatch latch     = new CountDownLatch(2);

            SimpleMessageListenerContainer container = buildIdempotentContainer(
                    EMERGENCY_IDEMPOTENCY_QUEUE,
                    processedIds, processed, skipped,
                    2, latch,
                    body -> extractFieldValue(body, "requestId"));
            container.start();

            try {
                EmergencyRequestEvent event =
                        new EmergencyRequestEvent(requestId, branchId, "O_NEGATIVE", "CRITICAL", Instant.now());

                publishEvent(EventConstants.EMERGENCY_REQUEST, event);
                publishEvent(EventConstants.EMERGENCY_REQUEST, event);

                boolean allReceived = latch.await(10, TimeUnit.SECONDS);
                assertThat(allReceived).isTrue();

                assertThat(processed.get()).isEqualTo(1);
                assertThat(skipped.get()).isEqualTo(1);
                assertThat(processedIds).containsExactly(requestId.toString());
            } finally {
                container.stop();
            }
        }

        @Test
        @DisplayName("Three publishes of the same emergency event should still process only once")
        void triplePublishShouldProcessOnlyOnce() throws InterruptedException {
            UUID requestId = UUID.randomUUID();
            UUID branchId  = UUID.randomUUID();

            Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            AtomicInteger processed  = new AtomicInteger(0);
            AtomicInteger skipped    = new AtomicInteger(0);
            CountDownLatch latch     = new CountDownLatch(3);

            SimpleMessageListenerContainer container = buildIdempotentContainer(
                    EMERGENCY_IDEMPOTENCY_QUEUE,
                    processedIds, processed, skipped,
                    3, latch,
                    body -> extractFieldValue(body, "requestId"));
            container.start();

            try {
                EmergencyRequestEvent event =
                        new EmergencyRequestEvent(requestId, branchId, "O_NEGATIVE", "LIFE_THREATENING", Instant.now());

                publishEvent(EventConstants.EMERGENCY_REQUEST, event);
                publishEvent(EventConstants.EMERGENCY_REQUEST, event);
                publishEvent(EventConstants.EMERGENCY_REQUEST, event);

                boolean allReceived = latch.await(15, TimeUnit.SECONDS);
                assertThat(allReceived).isTrue();

                assertThat(processed.get())
                        .as("Event processed exactly once regardless of how many times it is published")
                        .isEqualTo(1);
                assertThat(skipped.get())
                        .as("Two duplicate deliveries should be skipped")
                        .isEqualTo(2);
            } finally {
                container.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Cross-event-type idempotency
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cross-event-type: idempotency does not bleed between different event types")
    class CrossEventTypeIdempotency {

        @Test
        @DisplayName("Duplicate IDs across different event types should each be processed independently")
        void duplicateIdsAcrossDifferentEventTypesShouldBeProcessedIndependently()
                throws InterruptedException {
            // Use the SAME UUID for two completely different event types.
            // Each consumer tracks its own processed-IDs set so there is no cross-contamination.
            UUID sharedId = UUID.randomUUID();

            Set<String> donationProcessedIds  = Collections.newSetFromMap(new ConcurrentHashMap<>());
            Set<String> invoiceProcessedIds   = Collections.newSetFromMap(new ConcurrentHashMap<>());
            AtomicInteger donationProcessed   = new AtomicInteger(0);
            AtomicInteger invoiceProcessed    = new AtomicInteger(0);
            AtomicInteger donationSkipped     = new AtomicInteger(0);
            AtomicInteger invoiceSkipped      = new AtomicInteger(0);
            CountDownLatch donationLatch      = new CountDownLatch(2);
            CountDownLatch invoiceLatch       = new CountDownLatch(2);

            SimpleMessageListenerContainer donationContainer = buildIdempotentContainer(
                    DONATION_IDEMPOTENCY_QUEUE,
                    donationProcessedIds, donationProcessed, donationSkipped,
                    2, donationLatch,
                    body -> extractFieldValue(body, "donationId"));

            SimpleMessageListenerContainer invoiceContainer = buildIdempotentContainer(
                    INVOICE_IDEMPOTENCY_QUEUE,
                    invoiceProcessedIds, invoiceProcessed, invoiceSkipped,
                    2, invoiceLatch,
                    body -> extractFieldValue(body, "invoiceId"));

            donationContainer.start();
            invoiceContainer.start();

            try {
                // Publish donation event twice with sharedId as donationId
                DonationCompletedEvent donationEvent =
                        new DonationCompletedEvent(sharedId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());
                publishEvent(EventConstants.DONATION_COMPLETED, donationEvent);
                publishEvent(EventConstants.DONATION_COMPLETED, donationEvent);

                // Publish invoice event twice with sharedId as invoiceId
                InvoiceGeneratedEvent invoiceEvent =
                        new InvoiceGeneratedEvent(sharedId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());
                publishEvent(EventConstants.INVOICE_GENERATED, invoiceEvent);
                publishEvent(EventConstants.INVOICE_GENERATED, invoiceEvent);

                boolean donationsReceived = donationLatch.await(10, TimeUnit.SECONDS);
                boolean invoicesReceived  = invoiceLatch.await(10, TimeUnit.SECONDS);
                assertThat(donationsReceived).isTrue();
                assertThat(invoicesReceived).isTrue();

                // Each consumer should independently process its event once
                assertThat(donationProcessed.get())
                        .as("Donation consumer should process exactly once")
                        .isEqualTo(1);
                assertThat(donationSkipped.get())
                        .as("Donation consumer should skip the duplicate once")
                        .isEqualTo(1);

                assertThat(invoiceProcessed.get())
                        .as("Invoice consumer should process exactly once")
                        .isEqualTo(1);
                assertThat(invoiceSkipped.get())
                        .as("Invoice consumer should skip the duplicate once")
                        .isEqualTo(1);
            } finally {
                donationContainer.stop();
                invoiceContainer.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Utility: naive UUID extraction from JSON body
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Extracts the string value for {@code fieldName} from a minimal JSON byte array.
     *
     * For example, given {@code {"donationId":"abc-123",...}}, calling
     * {@code extractFieldValue(bytes, "donationId")} returns {@code "abc-123"}.
     *
     * This uses simple substring search — sufficient for the well-structured JSON
     * serialized by Jackson for UUID-bearing event records.
     */
    private String extractFieldValue(byte[] body, String fieldName) {
        String json = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        String key  = "\"" + fieldName + "\":\"";
        int start   = json.indexOf(key);
        if (start == -1) {
            return json; // return raw body as fallback so test can still detect duplicates
        }
        start += key.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
