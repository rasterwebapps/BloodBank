package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.BloodRequestCreatedEvent;
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
 * M3-042: Emergency Request → O-Neg Issue → Transfusion → Reaction → Hemovigilance
 *
 * Tests the emergency clinical workflow where an emergency blood request bypasses
 * normal matching, issues O-negative units directly, and handles transfusion reactions
 * triggering hemovigilance reports.
 *
 * Event flow verified:
 *   request-matching --[EmergencyRequestEvent]-------> notification-service (all branches)
 *   inventory-svc    --[UnitReleasedEvent]------------> inventory (O-neg stock update)
 *   transfusion-svc  --[TransfusionCompletedEvent]----> notification-service
 *   transfusion-svc  --[TransfusionReactionEvent]-----> notification-service (hemovigilance)
 *
 * Verifications:
 * - Emergency O-negative protocol bypasses normal matching (EmergencyRequestEvent, not BloodRequestMatchedEvent)
 * - TransfusionReactionEvent triggers hemovigilance report (reaches notification queue)
 * - EmergencyRequestEvent reaches all branches via notification-service
 */
@DisplayName("M3-042: Emergency Request → O-Neg Issue → Transfusion → Reaction → Hemovigilance")
class EmergencyTransfusionWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    // Queue names matching service-specific RabbitMQ configs
    private static final String NOTIFICATION_EMERGENCY_REQUEST_QUEUE =
            "notification.emergency.request.queue";
    private static final String NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE =
            "notification.transfusion.completed.queue";
    private static final String NOTIFICATION_TRANSFUSION_REACTION_QUEUE =
            "notification.transfusion.reaction.queue";
    private static final String INVENTORY_UNIT_RELEASED_QUEUE =
            "inventory.unit.released.queue";
    private static final String BILLING_BLOOD_REQUEST_MATCHED_QUEUE =
            "billing.blood.request.matched.queue";
    private static final String REQUEST_MATCHING_BLOOD_REQUEST_QUEUE =
            "request-matching.blood.request.created.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // notification-service listens for emergency.request (broadcasts to all branches)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EventConstants.EMERGENCY_REQUEST);

        // notification-service listens for transfusion.completed
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, EventConstants.TRANSFUSION_COMPLETED);

        // notification-service listens for transfusion.reaction (triggers hemovigilance)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_TRANSFUSION_REACTION_QUEUE, EventConstants.TRANSFUSION_REACTION);

        // inventory-service listens for unit.released
        declareAndBindQueue(admin, exchange,
                INVENTORY_UNIT_RELEASED_QUEUE, EventConstants.UNIT_RELEASED);

        // billing-service listens for blood.request.matched (should NOT receive emergency events)
        declareAndBindQueue(admin, exchange,
                BILLING_BLOOD_REQUEST_MATCHED_QUEUE, EventConstants.BLOOD_REQUEST_MATCHED);

        // request-matching-service listens for blood.request.created
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_BLOOD_REQUEST_QUEUE, EventConstants.BLOOD_REQUEST_CREATED);
    }

    @Nested
    @DisplayName("Emergency O-negative protocol bypasses normal matching")
    class EmergencyProtocolBypassesMatching {

        @Test
        @DisplayName("EmergencyRequestEvent should reach notification-service for broadcast")
        void emergencyRequestEventShouldReachNotificationService() {
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
        void emergencyRequestEventShouldNotTriggerBillingFlow() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            EmergencyRequestEvent event = new EmergencyRequestEvent(
                    requestId, branchId, "O_NEGATIVE", "CRITICAL", Instant.now());

            publishEvent(EventConstants.EMERGENCY_REQUEST, event);

            // Drain the notification queue
            assertEventReceived(NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

            // Billing queue should NOT receive this emergency event
            // (billing only listens for blood.request.matched, not emergency.request)
            assertNoEventReceived(BILLING_BLOOD_REQUEST_MATCHED_QUEUE);
        }

        @Test
        @DisplayName("EmergencyRequestEvent should NOT go through request-matching queue")
        void emergencyRequestEventShouldNotGoThroughRequestMatching() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            EmergencyRequestEvent event = new EmergencyRequestEvent(
                    requestId, branchId, "O_NEGATIVE", "URGENT", Instant.now());

            publishEvent(EventConstants.EMERGENCY_REQUEST, event);

            // Drain notification queue
            assertEventReceived(NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

            // Request-matching queue should NOT receive emergency events
            // (request-matching listens for blood.request.created, not emergency.request)
            assertNoEventReceived(REQUEST_MATCHING_BLOOD_REQUEST_QUEUE);
        }

        @Test
        @DisplayName("Emergency event should preserve severity field for prioritization")
        void emergencyEventShouldPreserveSeverityField() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            EmergencyRequestEvent event = new EmergencyRequestEvent(
                    requestId, branchId, "O_NEGATIVE", "LIFE_THREATENING", now);

            publishEvent(EventConstants.EMERGENCY_REQUEST, event);

            EmergencyRequestEvent received = assertEventReceived(
                    NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

            assertThat(received.severity()).isEqualTo("LIFE_THREATENING");
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("O-negative unit release in emergency")
    class ONegativeUnitRelease {

        @Test
        @DisplayName("UnitReleasedEvent for O-neg emergency should reach inventory-service")
        void unitReleasedEventForEmergencyShouldReachInventoryService() {
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
    @DisplayName("Transfusion reaction triggers hemovigilance")
    class TransfusionReactionTriggersHemovigilance {

        @Test
        @DisplayName("TransfusionReactionEvent should reach notification-service for hemovigilance report")
        void transfusionReactionEventShouldReachNotificationService() {
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
        @DisplayName("TransfusionReactionEvent with different severities should all route correctly")
        void transfusionReactionEventWithDifferentSeveritiesShouldRouteCorrectly() {
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
    }

    @Nested
    @DisplayName("Full workflow: Emergency end-to-end event chain")
    class FullEmergencyWorkflowEventChain {

        @Test
        @DisplayName("Complete emergency workflow should route events without normal matching")
        void completeEmergencyWorkflowShouldRouteEventsWithoutNormalMatching() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID transfusionId = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Emergency request — bypasses normal matching
            EmergencyRequestEvent emergencyRequest = new EmergencyRequestEvent(
                    requestId, branchId, "O_NEGATIVE", "CRITICAL", now);
            publishEvent(EventConstants.EMERGENCY_REQUEST, emergencyRequest);

            // Verify: notification receives it (for broadcast to all branches)
            EmergencyRequestEvent receivedEmergency = assertEventReceived(
                    NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);
            assertThat(receivedEmergency.bloodGroup()).isEqualTo("O_NEGATIVE");

            // Verify: billing does NOT receive it (emergency bypasses normal matching/billing)
            assertNoEventReceived(BILLING_BLOOD_REQUEST_MATCHED_QUEUE);

            // Verify: request-matching queue does NOT receive it
            assertNoEventReceived(REQUEST_MATCHING_BLOOD_REQUEST_QUEUE);

            // Step 2: O-negative unit released directly (no cross-match for emergency)
            UnitReleasedEvent unitReleased = new UnitReleasedEvent(bloodUnitId, branchId, now);
            publishEvent(EventConstants.UNIT_RELEASED, unitReleased);

            assertEventReceived(INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);

            // Step 3: Transfusion completed
            TransfusionCompletedEvent transfusionCompleted = new TransfusionCompletedEvent(
                    transfusionId, bloodUnitId, branchId, now);
            publishEvent(EventConstants.TRANSFUSION_COMPLETED, transfusionCompleted);

            assertEventReceived(NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);

            // Step 4: Transfusion reaction — triggers hemovigilance
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
