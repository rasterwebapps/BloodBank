package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.BloodRequestCreatedEvent;
import com.bloodbank.common.events.BloodRequestMatchedEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.InvoiceGeneratedEvent;
import com.bloodbank.common.events.TransfusionCompletedEvent;
import com.bloodbank.common.events.UnitReleasedEvent;
import com.bloodbank.integration.support.AbstractWorkflowIntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M3-041: Hospital Request → Match → Cross-Match → Issue → Transfuse
 *
 * Tests the complete clinical workflow where a hospital creates a blood request,
 * which triggers matching, cross-matching, unit issuance, and transfusion.
 *
 * Event flow verified:
 *   hospital-service  --[BloodRequestCreatedEvent]--> request-matching-service
 *   request-matching  --[BloodRequestMatchedEvent]--> billing-service, notification-service
 *   inventory-service --[UnitReleasedEvent]----------> inventory (stock update)
 *   transfusion-svc   --[TransfusionCompletedEvent]-> notification-service
 *
 * Verifications:
 * - BloodRequestCreatedEvent triggers matching (reaches request-matching queue)
 * - BloodRequestMatchedEvent triggers billing (reaches billing queue)
 * - TransfusionCompletedEvent reaches notification queue
 */
@DisplayName("M3-041: Hospital Request → Match → Cross-Match → Issue → Transfuse")
class HospitalRequestWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    // Queue names matching service-specific RabbitMQ configs
    private static final String REQUEST_MATCHING_BLOOD_REQUEST_QUEUE =
            "request-matching.blood.request.created.queue";
    private static final String BILLING_BLOOD_REQUEST_MATCHED_QUEUE =
            "billing.blood.request.matched.queue";
    private static final String NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE =
            "notification.blood.request.created.queue";
    private static final String NOTIFICATION_BLOOD_REQUEST_MATCHED_QUEUE =
            "notification.blood.request.matched.queue";
    private static final String NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE =
            "notification.transfusion.completed.queue";
    private static final String NOTIFICATION_INVOICE_GENERATED_QUEUE =
            "notification.invoice.generated.queue";
    private static final String INVENTORY_UNIT_RELEASED_QUEUE =
            "inventory.unit.released.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // request-matching-service listens for blood.request.created
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, EventConstants.BLOOD_REQUEST_CREATED);

        // billing-service listens for blood.request.matched
        declareAndBindQueue(admin, exchange,
                BILLING_BLOOD_REQUEST_MATCHED_QUEUE, EventConstants.BLOOD_REQUEST_MATCHED);

        // notification-service listens for blood.request.created
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE, EventConstants.BLOOD_REQUEST_CREATED);

        // notification-service listens for blood.request.matched
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_BLOOD_REQUEST_MATCHED_QUEUE, EventConstants.BLOOD_REQUEST_MATCHED);

        // notification-service listens for transfusion.completed
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, EventConstants.TRANSFUSION_COMPLETED);

        // notification-service listens for invoice.generated
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_INVOICE_GENERATED_QUEUE, EventConstants.INVOICE_GENERATED);

        // inventory-service listens for unit.released
        declareAndBindQueue(admin, exchange,
                INVENTORY_UNIT_RELEASED_QUEUE, EventConstants.UNIT_RELEASED);
    }

    @Nested
    @DisplayName("Step 1: Hospital creates blood request")
    class HospitalRequestCreation {

        @Test
        @DisplayName("BloodRequestCreatedEvent should reach request-matching-service queue")
        void bloodRequestCreatedEventShouldReachRequestMatchingService() {
            UUID requestId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            BloodRequestCreatedEvent event = new BloodRequestCreatedEvent(
                    requestId, hospitalId, branchId, "A_POSITIVE", 3, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_CREATED, event);

            BloodRequestCreatedEvent received = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, BloodRequestCreatedEvent.class);

            assertThat(received.requestId()).isEqualTo(requestId);
            assertThat(received.hospitalId()).isEqualTo(hospitalId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("A_POSITIVE");
            assertThat(received.quantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("BloodRequestCreatedEvent should also reach notification-service queue")
        void bloodRequestCreatedEventShouldReachNotificationService() {
            UUID requestId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            BloodRequestCreatedEvent event = new BloodRequestCreatedEvent(
                    requestId, hospitalId, branchId, "O_NEGATIVE", 2, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_CREATED, event);

            // Should fan-out to both request-matching and notification queues
            BloodRequestCreatedEvent matchingReceived = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, BloodRequestCreatedEvent.class);
            BloodRequestCreatedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);

            assertThat(matchingReceived.requestId()).isEqualTo(requestId);
            assertThat(notificationReceived.requestId()).isEqualTo(requestId);
        }

        @Test
        @DisplayName("BloodRequestCreatedEvent should preserve all fields through serialization")
        void bloodRequestCreatedEventShouldPreserveAllFields() {
            UUID requestId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            BloodRequestCreatedEvent event = new BloodRequestCreatedEvent(
                    requestId, hospitalId, branchId, "B_NEGATIVE", 5, now);

            publishEvent(EventConstants.BLOOD_REQUEST_CREATED, event);

            BloodRequestCreatedEvent received = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, BloodRequestCreatedEvent.class);

            assertThat(received.requestId()).isEqualTo(requestId);
            assertThat(received.hospitalId()).isEqualTo(hospitalId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("B_NEGATIVE");
            assertThat(received.quantity()).isEqualTo(5);
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Step 2: Request matching triggers billing")
    class RequestMatchingTriggersBilling {

        @Test
        @DisplayName("BloodRequestMatchedEvent should reach billing-service queue")
        void bloodRequestMatchedEventShouldReachBillingService() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            List<UUID> matchedUnitIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            BloodRequestMatchedEvent event = new BloodRequestMatchedEvent(
                    requestId, branchId, matchedUnitIds, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED, event);

            BloodRequestMatchedEvent received = assertEventReceived(
                    BILLING_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);

            assertThat(received.requestId()).isEqualTo(requestId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.matchedUnitIds()).hasSize(3);
            assertThat(received.matchedUnitIds()).containsExactlyElementsOf(matchedUnitIds);
        }

        @Test
        @DisplayName("BloodRequestMatchedEvent should also reach notification-service queue")
        void bloodRequestMatchedEventShouldReachNotificationService() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            List<UUID> matchedUnitIds = List.of(UUID.randomUUID());

            BloodRequestMatchedEvent event = new BloodRequestMatchedEvent(
                    requestId, branchId, matchedUnitIds, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED, event);

            // Should fan-out to both billing and notification queues
            BloodRequestMatchedEvent billingReceived = assertEventReceived(
                    BILLING_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            BloodRequestMatchedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);

            assertThat(billingReceived.requestId()).isEqualTo(requestId);
            assertThat(notificationReceived.requestId()).isEqualTo(requestId);
        }
    }

    @Nested
    @DisplayName("Step 3: Unit release updates inventory")
    class UnitReleaseUpdatesInventory {

        @Test
        @DisplayName("UnitReleasedEvent should reach inventory-service queue")
        void unitReleasedEventShouldReachInventoryService() {
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            UnitReleasedEvent event = new UnitReleasedEvent(bloodUnitId, branchId, Instant.now());

            publishEvent(EventConstants.UNIT_RELEASED, event);

            UnitReleasedEvent received = assertEventReceived(
                    INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);

            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.branchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("Step 4: Transfusion completion notification")
    class TransfusionCompletion {

        @Test
        @DisplayName("TransfusionCompletedEvent should reach notification-service queue")
        void transfusionCompletedEventShouldReachNotificationService() {
            UUID transfusionId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            TransfusionCompletedEvent event = new TransfusionCompletedEvent(
                    transfusionId, bloodUnitId, branchId, Instant.now());

            publishEvent(EventConstants.TRANSFUSION_COMPLETED, event);

            TransfusionCompletedEvent received = assertEventReceived(
                    NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);

            assertThat(received.transfusionId()).isEqualTo(transfusionId);
            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.branchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("Step 5: Invoice generation notification")
    class InvoiceGeneration {

        @Test
        @DisplayName("InvoiceGeneratedEvent should reach notification-service queue")
        void invoiceGeneratedEventShouldReachNotificationService() {
            UUID invoiceId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            InvoiceGeneratedEvent event = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, Instant.now());

            publishEvent(EventConstants.INVOICE_GENERATED, event);

            InvoiceGeneratedEvent received = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(received.invoiceId()).isEqualTo(invoiceId);
            assertThat(received.hospitalId()).isEqualTo(hospitalId);
            assertThat(received.branchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("Full workflow: End-to-end event chain")
    class FullWorkflowEventChain {

        @Test
        @DisplayName("Complete hospital request workflow should route all events correctly")
        void completeHospitalRequestWorkflowShouldRouteAllEvents() {
            UUID requestId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID transfusionId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Hospital creates blood request
            BloodRequestCreatedEvent requestCreated = new BloodRequestCreatedEvent(
                    requestId, hospitalId, branchId, "A_POSITIVE", 2, now);
            publishEvent(EventConstants.BLOOD_REQUEST_CREATED, requestCreated);

            // Verify: request-matching and notification both receive it
            assertEventReceived(REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, BloodRequestCreatedEvent.class);
            assertEventReceived(NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);

            // Step 2: Request matching produces matched event
            BloodRequestMatchedEvent requestMatched = new BloodRequestMatchedEvent(
                    requestId, branchId, List.of(bloodUnitId), now);
            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED, requestMatched);

            // Verify: billing and notification both receive it
            assertEventReceived(BILLING_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            assertEventReceived(NOTIFICATION_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);

            // Step 3: Unit released for cross-match/issue
            UnitReleasedEvent unitReleased = new UnitReleasedEvent(bloodUnitId, branchId, now);
            publishEvent(EventConstants.UNIT_RELEASED, unitReleased);

            assertEventReceived(INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);

            // Step 4: Transfusion completed
            TransfusionCompletedEvent transfusionCompleted = new TransfusionCompletedEvent(
                    transfusionId, bloodUnitId, branchId, now);
            publishEvent(EventConstants.TRANSFUSION_COMPLETED, transfusionCompleted);

            assertEventReceived(NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);

            // Step 5: Invoice generated
            InvoiceGeneratedEvent invoiceGenerated = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, now);
            publishEvent(EventConstants.INVOICE_GENERATED, invoiceGenerated);

            assertEventReceived(NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
        }
    }
}
