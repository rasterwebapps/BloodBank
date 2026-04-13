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
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-002: Hospital Request → Match → Cross-Match → Issue → Transfusion → Outcome
 *
 * Tests the full hospital blood request workflow including request creation,
 * matching, cross-match, unit issuance, transfusion, and billing outcome.
 *
 * Event flow verified:
 *   hospital-service  --[BloodRequestCreatedEvent]---> request-matching, notification
 *   request-matching  --[BloodRequestMatchedEvent]---> billing-service, notification
 *   inventory-service --[UnitReleasedEvent]----------> inventory (cross-match / issue)
 *   transfusion-svc   --[TransfusionCompletedEvent]--> notification-service
 *   billing-service   --[InvoiceGeneratedEvent]------> notification-service
 *
 * Verifications:
 * - BloodRequestCreatedEvent fans out to request-matching and notification
 * - BloodRequestMatchedEvent fans out to billing and notification
 * - UnitReleasedEvent reaches inventory queue for stock update
 * - TransfusionCompletedEvent reaches notification for post-transfusion outcome
 * - InvoiceGeneratedEvent reaches notification after transfusion billing
 */
@DisplayName("M6-002: Hospital Request → Match → Cross-Match → Issue → Transfusion → Outcome")
class HospitalRequestFullWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String REQUEST_MATCHING_BLOOD_REQUEST_QUEUE =
            "request-matching.blood.request.created.queue";
    private static final String NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE =
            "notification.blood.request.created.queue";
    private static final String BILLING_BLOOD_REQUEST_MATCHED_QUEUE =
            "billing.blood.request.matched.queue";
    private static final String NOTIFICATION_BLOOD_REQUEST_MATCHED_QUEUE =
            "notification.blood.request.matched.queue";
    private static final String INVENTORY_UNIT_RELEASED_QUEUE =
            "inventory.unit.released.queue";
    private static final String NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE =
            "notification.transfusion.completed.queue";
    private static final String NOTIFICATION_INVOICE_GENERATED_QUEUE =
            "notification.invoice.generated.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // request-matching-service listens for blood.request.created
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, EventConstants.BLOOD_REQUEST_CREATED);

        // notification-service listens for blood.request.created
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE, EventConstants.BLOOD_REQUEST_CREATED);

        // billing-service listens for blood.request.matched
        declareAndBindQueue(admin, exchange,
                BILLING_BLOOD_REQUEST_MATCHED_QUEUE, EventConstants.BLOOD_REQUEST_MATCHED);

        // notification-service listens for blood.request.matched
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_BLOOD_REQUEST_MATCHED_QUEUE, EventConstants.BLOOD_REQUEST_MATCHED);

        // inventory-service listens for unit.released (cross-match cleared / issued)
        declareAndBindQueue(admin, exchange,
                INVENTORY_UNIT_RELEASED_QUEUE, EventConstants.UNIT_RELEASED);

        // notification-service listens for transfusion.completed
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, EventConstants.TRANSFUSION_COMPLETED);

        // notification-service listens for invoice.generated
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_INVOICE_GENERATED_QUEUE, EventConstants.INVOICE_GENERATED);
    }

    @Nested
    @DisplayName("Step 1: Hospital creates blood request")
    class HospitalCreatesBloodRequest {

        @Test
        @DisplayName("BloodRequestCreatedEvent should reach request-matching-service for matching")
        void bloodRequestCreatedEventShouldReachRequestMatchingService() {
            UUID requestId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            BloodRequestCreatedEvent event = new BloodRequestCreatedEvent(
                    requestId, hospitalId, branchId, "A_POSITIVE", 2, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_CREATED, event);

            BloodRequestCreatedEvent received = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, BloodRequestCreatedEvent.class);

            assertThat(received.requestId()).isEqualTo(requestId);
            assertThat(received.hospitalId()).isEqualTo(hospitalId);
            assertThat(received.bloodGroup()).isEqualTo("A_POSITIVE");
            assertThat(received.quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("BloodRequestCreatedEvent should fan-out to request-matching and notification")
        void bloodRequestCreatedEventShouldFanOutToMatchingAndNotification() {
            UUID requestId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            BloodRequestCreatedEvent event = new BloodRequestCreatedEvent(
                    requestId, hospitalId, branchId, "B_POSITIVE", 1, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_CREATED, event);

            BloodRequestCreatedEvent matchingReceived = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, BloodRequestCreatedEvent.class);
            BloodRequestCreatedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);

            assertThat(matchingReceived.requestId()).isEqualTo(requestId);
            assertThat(notificationReceived.requestId()).isEqualTo(requestId);
        }

        @Test
        @DisplayName("BloodRequestCreatedEvent should support multiple blood group types")
        void bloodRequestCreatedEventShouldSupportMultipleBloodGroups() {
            String[] bloodGroups = {"A_POSITIVE", "A_NEGATIVE", "B_POSITIVE", "B_NEGATIVE",
                    "AB_POSITIVE", "AB_NEGATIVE", "O_POSITIVE", "O_NEGATIVE"};

            for (String bloodGroup : bloodGroups) {
                UUID requestId = UUID.randomUUID();

                BloodRequestCreatedEvent event = new BloodRequestCreatedEvent(
                        requestId, UUID.randomUUID(), UUID.randomUUID(), bloodGroup, 1, Instant.now());

                publishEvent(EventConstants.BLOOD_REQUEST_CREATED, event);

                BloodRequestCreatedEvent received = assertEventReceived(
                        REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, BloodRequestCreatedEvent.class);
                // Drain notification queue
                assertEventReceived(NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);

                assertThat(received.bloodGroup()).isEqualTo(bloodGroup);
            }
        }
    }

    @Nested
    @DisplayName("Step 2: Matching completes — cross-match and billing triggered")
    class MatchingTriggersCrossmatchAndBilling {

        @Test
        @DisplayName("BloodRequestMatchedEvent should reach billing-service to open invoice")
        void bloodRequestMatchedEventShouldReachBillingService() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            List<UUID> matchedUnitIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            BloodRequestMatchedEvent event = new BloodRequestMatchedEvent(
                    requestId, branchId, matchedUnitIds, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED, event);

            BloodRequestMatchedEvent received = assertEventReceived(
                    BILLING_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);

            assertThat(received.requestId()).isEqualTo(requestId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.matchedUnitIds()).hasSize(2).containsExactlyElementsOf(matchedUnitIds);
        }

        @Test
        @DisplayName("BloodRequestMatchedEvent should fan-out to billing and notification")
        void bloodRequestMatchedEventShouldFanOutToBillingAndNotification() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            List<UUID> matchedUnitIds = List.of(UUID.randomUUID());

            BloodRequestMatchedEvent event = new BloodRequestMatchedEvent(
                    requestId, branchId, matchedUnitIds, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED, event);

            BloodRequestMatchedEvent billingReceived = assertEventReceived(
                    BILLING_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            BloodRequestMatchedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);

            assertThat(billingReceived.requestId()).isEqualTo(requestId);
            assertThat(notificationReceived.requestId()).isEqualTo(requestId);
        }

        @Test
        @DisplayName("BloodRequestMatchedEvent should carry all matched unit IDs for cross-match")
        void bloodRequestMatchedEventShouldCarryAllMatchedUnitIds() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID unit1 = UUID.randomUUID();
            UUID unit2 = UUID.randomUUID();
            UUID unit3 = UUID.randomUUID();
            List<UUID> matchedUnitIds = List.of(unit1, unit2, unit3);

            BloodRequestMatchedEvent event = new BloodRequestMatchedEvent(
                    requestId, branchId, matchedUnitIds, Instant.now());

            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED, event);

            BloodRequestMatchedEvent received = assertEventReceived(
                    BILLING_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);

            assertThat(received.matchedUnitIds()).containsExactlyInAnyOrder(unit1, unit2, unit3);
        }
    }

    @Nested
    @DisplayName("Step 3: Unit issued after cross-match clears")
    class UnitIssuedAfterCrossmatch {

        @Test
        @DisplayName("UnitReleasedEvent should reach inventory-service for stock adjustment")
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
    @DisplayName("Step 4: Transfusion completed — outcome recorded")
    class TransfusionCompletedOutcomeRecorded {

        @Test
        @DisplayName("TransfusionCompletedEvent should reach notification-service for outcome alert")
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
    @DisplayName("Step 5: Invoice generated after transfusion")
    class InvoiceGeneratedAfterTransfusion {

        @Test
        @DisplayName("InvoiceGeneratedEvent should reach notification-service for billing alert")
        void invoiceGeneratedEventShouldReachNotificationService() {
            UUID invoiceId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            InvoiceGeneratedEvent event = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, now);

            publishEvent(EventConstants.INVOICE_GENERATED, event);

            InvoiceGeneratedEvent received = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(received.invoiceId()).isEqualTo(invoiceId);
            assertThat(received.hospitalId()).isEqualTo(hospitalId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Full workflow: Hospital request end-to-end event chain")
    class FullHospitalRequestWorkflowEventChain {

        @Test
        @DisplayName("Complete hospital request lifecycle should route all events correctly")
        void completeHospitalRequestLifecycleShouldRouteAllEvents() {
            UUID requestId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID transfusionId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Hospital creates blood request
            BloodRequestCreatedEvent requestCreated = new BloodRequestCreatedEvent(
                    requestId, hospitalId, branchId, "AB_POSITIVE", 1, now);
            publishEvent(EventConstants.BLOOD_REQUEST_CREATED, requestCreated);

            assertEventReceived(REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, BloodRequestCreatedEvent.class);
            assertEventReceived(NOTIFICATION_BLOOD_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);

            // Step 2: Request matched (cross-match initiated)
            BloodRequestMatchedEvent requestMatched = new BloodRequestMatchedEvent(
                    requestId, branchId, List.of(bloodUnitId), now);
            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED, requestMatched);

            BloodRequestMatchedEvent billingEvent = assertEventReceived(
                    BILLING_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            assertEventReceived(NOTIFICATION_BLOOD_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            assertThat(billingEvent.matchedUnitIds()).contains(bloodUnitId);

            // Step 3: Unit released after cross-match clears
            UnitReleasedEvent unitReleased = new UnitReleasedEvent(bloodUnitId, branchId, now);
            publishEvent(EventConstants.UNIT_RELEASED, unitReleased);

            assertEventReceived(INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);

            // Step 4: Transfusion completed
            TransfusionCompletedEvent transfusionCompleted = new TransfusionCompletedEvent(
                    transfusionId, bloodUnitId, branchId, now);
            publishEvent(EventConstants.TRANSFUSION_COMPLETED, transfusionCompleted);

            TransfusionCompletedEvent receivedTransfusion = assertEventReceived(
                    NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);
            assertThat(receivedTransfusion.transfusionId()).isEqualTo(transfusionId);

            // Step 5: Invoice generated
            InvoiceGeneratedEvent invoiceGenerated = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, now);
            publishEvent(EventConstants.INVOICE_GENERATED, invoiceGenerated);

            InvoiceGeneratedEvent receivedInvoice = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            assertThat(receivedInvoice.invoiceId()).isEqualTo(invoiceId);
        }
    }
}
