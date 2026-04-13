package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.EmergencyRequestEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.TransfusionCompletedEvent;
import com.bloodbank.common.events.TransfusionReactionEvent;
import com.bloodbank.common.events.UnitReleasedEvent;
import com.bloodbank.integration.support.AbstractWorkflowIntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-005: Emergency → O-Neg Issue → Transfusion → Reaction → Hemovigilance
 *
 * Tests the full emergency blood issue workflow where an emergency request bypasses
 * normal matching to directly issue O-negative units, followed by transfusion,
 * potential reaction, and hemovigilance reporting.
 *
 * Event flow verified:
 *   request-matching  --[EmergencyRequestEvent]------> notification (broadcast to all branches)
 *   inventory-service --[UnitReleasedEvent]-----------> inventory (O-neg stock deduction)
 *   transfusion-svc   --[TransfusionCompletedEvent]--> notification-service
 *   transfusion-svc   --[TransfusionReactionEvent]---> notification (hemovigilance trigger)
 *
 * Verifications:
 * - Emergency O-neg protocol bypasses normal matching (EmergencyRequestEvent, NOT BloodRequestMatchedEvent)
 * - UnitReleasedEvent for O-neg reaches inventory to update stock
 * - TransfusionReactionEvent reaches notification queue for hemovigilance report filing
 * - All severity levels of reactions route correctly
 */
@DisplayName("M6-005: Emergency → O-Neg Issue → Transfusion → Reaction → Hemovigilance")
class EmergencyONegWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String NOTIFICATION_EMERGENCY_REQUEST_QUEUE =
            "notification.emergency.request.queue";
    private static final String INVENTORY_UNIT_RELEASED_QUEUE =
            "inventory.unit.released.queue";
    private static final String NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE =
            "notification.transfusion.completed.queue";
    private static final String NOTIFICATION_TRANSFUSION_REACTION_QUEUE =
            "notification.transfusion.reaction.queue";
    private static final String BILLING_BLOOD_REQUEST_MATCHED_QUEUE =
            "billing.blood.request.matched.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // notification-service listens for emergency.request (broadcast to all branches)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EventConstants.EMERGENCY_REQUEST);

        // inventory-service listens for unit.released (O-neg stock deduction)
        declareAndBindQueue(admin, exchange,
                INVENTORY_UNIT_RELEASED_QUEUE, EventConstants.UNIT_RELEASED);

        // notification-service listens for transfusion.completed (outcome notification)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, EventConstants.TRANSFUSION_COMPLETED);

        // notification-service listens for transfusion.reaction (hemovigilance trigger)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_TRANSFUSION_REACTION_QUEUE, EventConstants.TRANSFUSION_REACTION);

        // billing-service listens for blood.request.matched (should NOT receive emergency events)
        declareAndBindQueue(admin, exchange,
                BILLING_BLOOD_REQUEST_MATCHED_QUEUE, EventConstants.BLOOD_REQUEST_MATCHED);
    }

    @Nested
    @DisplayName("Step 1: Emergency request triggers O-negative protocol")
    class EmergencyRequestTriggersONegProtocol {

        @Test
        @DisplayName("EmergencyRequestEvent for O-negative should reach notification for broadcast")
        void emergencyONegRequestShouldReachNotificationForBroadcast() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            EmergencyRequestEvent event = new EmergencyRequestEvent(
                    requestId, branchId, "O_NEGATIVE", "CRITICAL", Instant.now());

            publishEvent(EventConstants.EMERGENCY_REQUEST, event);

            EmergencyRequestEvent received = assertEventReceived(
                    NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

            assertThat(received.requestId()).isEqualTo(requestId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(received.severity()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("EmergencyRequestEvent should NOT trigger normal billing flow")
        void emergencyRequestShouldNotTriggerNormalBillingFlow() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            EmergencyRequestEvent event = new EmergencyRequestEvent(
                    requestId, branchId, "O_NEGATIVE", "CRITICAL", Instant.now());

            publishEvent(EventConstants.EMERGENCY_REQUEST, event);

            // Drain notification queue
            assertEventReceived(NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

            // Billing must NOT receive emergency request events (no matching step)
            assertNoEventReceived(BILLING_BLOOD_REQUEST_MATCHED_QUEUE);
        }

        @Test
        @DisplayName("Emergency request severity levels should all route to notification")
        void emergencyRequestSeverityLevelsShouldAllRouteToNotification() {
            String[] severities = {"URGENT", "CRITICAL", "LIFE_THREATENING"};

            for (String severity : severities) {
                UUID requestId = UUID.randomUUID();
                UUID branchId = UUID.randomUUID();

                EmergencyRequestEvent event = new EmergencyRequestEvent(
                        requestId, branchId, "O_NEGATIVE", severity, Instant.now());

                publishEvent(EventConstants.EMERGENCY_REQUEST, event);

                EmergencyRequestEvent received = assertEventReceived(
                        NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

                assertThat(received.severity()).isEqualTo(severity);
                assertThat(received.requestId()).isEqualTo(requestId);
            }
        }
    }

    @Nested
    @DisplayName("Step 2: O-negative unit issued directly")
    class ONegUnitIssuedDirectly {

        @Test
        @DisplayName("UnitReleasedEvent should reach inventory-service for O-neg stock deduction")
        void unitReleasedForONegShouldReachInventoryService() {
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
    @DisplayName("Step 3: Transfusion completed")
    class TransfusionCompleted {

        @Test
        @DisplayName("TransfusionCompletedEvent should reach notification for post-transfusion record")
        void transfusionCompletedEventShouldReachNotification() {
            UUID transfusionId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            TransfusionCompletedEvent event = new TransfusionCompletedEvent(
                    transfusionId, bloodUnitId, branchId, now);

            publishEvent(EventConstants.TRANSFUSION_COMPLETED, event);

            TransfusionCompletedEvent received = assertEventReceived(
                    NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);

            assertThat(received.transfusionId()).isEqualTo(transfusionId);
            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Step 4: Transfusion reaction triggers hemovigilance")
    class TransfusionReactionTriggersHemovigilance {

        @Test
        @DisplayName("TransfusionReactionEvent should reach notification for hemovigilance report")
        void transfusionReactionEventShouldReachNotificationForHemovigilance() {
            UUID transfusionId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            TransfusionReactionEvent event = new TransfusionReactionEvent(
                    transfusionId, bloodUnitId, branchId, "SEVERE", Instant.now());

            publishEvent(EventConstants.TRANSFUSION_REACTION, event);

            TransfusionReactionEvent received = assertEventReceived(
                    NOTIFICATION_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);

            assertThat(received.transfusionId()).isEqualTo(transfusionId);
            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.severity()).isEqualTo("SEVERE");
        }

        @Test
        @DisplayName("All transfusion reaction severity levels should route to hemovigilance queue")
        void allReactionSeveritiesShouldRouteToHemovigilanceQueue() {
            String[] severities = {"MILD", "MODERATE", "SEVERE", "LIFE_THREATENING"};

            for (String severity : severities) {
                UUID transfusionId = UUID.randomUUID();
                UUID bloodUnitId = UUID.randomUUID();
                UUID branchId = UUID.randomUUID();

                TransfusionReactionEvent event = new TransfusionReactionEvent(
                        transfusionId, bloodUnitId, branchId, severity, Instant.now());

                publishEvent(EventConstants.TRANSFUSION_REACTION, event);

                TransfusionReactionEvent received = assertEventReceived(
                        NOTIFICATION_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);

                assertThat(received.severity()).isEqualTo(severity);
                assertThat(received.transfusionId()).isEqualTo(transfusionId);
            }
        }

        @Test
        @DisplayName("TransfusionReactionEvent should preserve all fields for investigation")
        void transfusionReactionEventShouldPreserveAllFieldsForInvestigation() {
            UUID transfusionId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            TransfusionReactionEvent event = new TransfusionReactionEvent(
                    transfusionId, bloodUnitId, branchId, "LIFE_THREATENING", now);

            publishEvent(EventConstants.TRANSFUSION_REACTION, event);

            TransfusionReactionEvent received = assertEventReceived(
                    NOTIFICATION_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);

            assertThat(received.transfusionId()).isEqualTo(transfusionId);
            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.severity()).isEqualTo("LIFE_THREATENING");
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Full workflow: Emergency O-neg end-to-end event chain")
    class FullEmergencyONegWorkflowEventChain {

        @Test
        @DisplayName("Complete emergency O-neg workflow should route all events in correct sequence")
        void completeEmergencyONegWorkflowShouldRouteAllEvents() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID transfusionId = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Emergency request — O-neg protocol (no matching, no billing)
            EmergencyRequestEvent emergencyRequest = new EmergencyRequestEvent(
                    requestId, branchId, "O_NEGATIVE", "CRITICAL", now);
            publishEvent(EventConstants.EMERGENCY_REQUEST, emergencyRequest);

            EmergencyRequestEvent receivedEmergency = assertEventReceived(
                    NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);
            assertThat(receivedEmergency.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(receivedEmergency.severity()).isEqualTo("CRITICAL");

            // Emergency bypasses billing
            assertNoEventReceived(BILLING_BLOOD_REQUEST_MATCHED_QUEUE);

            // Step 2: O-neg unit issued directly (no cross-match for emergency)
            UnitReleasedEvent unitReleased = new UnitReleasedEvent(bloodUnitId, branchId, now);
            publishEvent(EventConstants.UNIT_RELEASED, unitReleased);

            assertEventReceived(INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);

            // Step 3: Transfusion completed
            TransfusionCompletedEvent transfusionCompleted = new TransfusionCompletedEvent(
                    transfusionId, bloodUnitId, branchId, now);
            publishEvent(EventConstants.TRANSFUSION_COMPLETED, transfusionCompleted);

            TransfusionCompletedEvent receivedTransfusion = assertEventReceived(
                    NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);
            assertThat(receivedTransfusion.transfusionId()).isEqualTo(transfusionId);

            // Step 4: Transfusion reaction — triggers hemovigilance report
            TransfusionReactionEvent reaction = new TransfusionReactionEvent(
                    transfusionId, bloodUnitId, branchId, "SEVERE", now);
            publishEvent(EventConstants.TRANSFUSION_REACTION, reaction);

            TransfusionReactionEvent receivedReaction = assertEventReceived(
                    NOTIFICATION_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);
            assertThat(receivedReaction.severity()).isEqualTo("SEVERE");
            assertThat(receivedReaction.transfusionId()).isEqualTo(transfusionId);
        }
    }
}
