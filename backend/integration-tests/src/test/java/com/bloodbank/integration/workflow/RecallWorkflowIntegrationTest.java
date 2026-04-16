package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.RecallInitiatedEvent;
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
 * M6-006: Recall → Notification → Investigation → Resolution
 *
 * Tests the blood unit recall workflow from recall initiation through notification,
 * investigation, and resolution. A recall event fans out to notification-service
 * and compliance-service for investigation and regulatory reporting.
 *
 * Event flow verified:
 *   compliance-svc    --[RecallInitiatedEvent]-------> notification-service (urgent alert)
 *   compliance-svc    --[RecallInitiatedEvent]-------> inventory-service (quarantine affected units)
 *   compliance-svc    --[RecallInitiatedEvent]-------> reporting-service (regulatory report)
 *
 * Verifications:
 * - RecallInitiatedEvent reaches notification-service for urgent alerts to staff
 * - RecallInitiatedEvent reaches inventory-service to quarantine affected units
 * - RecallInitiatedEvent reaches reporting-service for regulatory investigation record
 * - All affected unit IDs are preserved through event serialization
 * - Recall reason is preserved for investigation context
 */
@DisplayName("M6-006: Recall → Notification → Investigation → Resolution")
class RecallWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String NOTIFICATION_RECALL_INITIATED_QUEUE =
            "notification.recall.initiated.queue";
    private static final String INVENTORY_RECALL_INITIATED_QUEUE =
            "inventory.recall.initiated.queue";
    private static final String REPORTING_RECALL_INITIATED_QUEUE =
            "reporting.recall.initiated.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // notification-service listens for recall.initiated (urgent recall alerts to staff)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_RECALL_INITIATED_QUEUE, EventConstants.RECALL_INITIATED);

        // inventory-service listens for recall.initiated (to quarantine affected units)
        declareAndBindQueue(admin, exchange,
                INVENTORY_RECALL_INITIATED_QUEUE, EventConstants.RECALL_INITIATED);

        // reporting-service listens for recall.initiated (regulatory investigation record)
        declareAndBindQueue(admin, exchange,
                REPORTING_RECALL_INITIATED_QUEUE, EventConstants.RECALL_INITIATED);
    }

    @Nested
    @DisplayName("Step 1: Recall initiated reaches all downstream services")
    class RecallInitiatedReachesDownstreamServices {

        @Test
        @DisplayName("RecallInitiatedEvent should reach notification-service for urgent staff alert")
        void recallInitiatedEventShouldReachNotificationService() {
            UUID recallId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            List<UUID> affectedUnitIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            RecallInitiatedEvent event = new RecallInitiatedEvent(
                    recallId, branchId, "CONTAMINATION_SUSPECTED", affectedUnitIds, Instant.now());

            publishEvent(EventConstants.RECALL_INITIATED, event);

            RecallInitiatedEvent received = assertEventReceived(
                    NOTIFICATION_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);

            assertThat(received.recallId()).isEqualTo(recallId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.reason()).isEqualTo("CONTAMINATION_SUSPECTED");
            assertThat(received.affectedUnitIds()).hasSize(2);
        }

        @Test
        @DisplayName("RecallInitiatedEvent should reach inventory-service to quarantine affected units")
        void recallInitiatedEventShouldReachInventoryServiceForQuarantine() {
            UUID recallId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID unit1 = UUID.randomUUID();
            UUID unit2 = UUID.randomUUID();
            UUID unit3 = UUID.randomUUID();
            List<UUID> affectedUnitIds = List.of(unit1, unit2, unit3);

            RecallInitiatedEvent event = new RecallInitiatedEvent(
                    recallId, branchId, "DONOR_REACTIVE_TEST", affectedUnitIds, Instant.now());

            publishEvent(EventConstants.RECALL_INITIATED, event);

            RecallInitiatedEvent received = assertEventReceived(
                    INVENTORY_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);

            assertThat(received.recallId()).isEqualTo(recallId);
            assertThat(received.affectedUnitIds()).containsExactlyInAnyOrder(unit1, unit2, unit3);
        }

        @Test
        @DisplayName("RecallInitiatedEvent should reach reporting-service for regulatory record")
        void recallInitiatedEventShouldReachReportingServiceForRegulatoryRecord() {
            UUID recallId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            List<UUID> affectedUnitIds = List.of(UUID.randomUUID());

            RecallInitiatedEvent event = new RecallInitiatedEvent(
                    recallId, branchId, "LOOKBACK_TRIGGERED", affectedUnitIds, Instant.now());

            publishEvent(EventConstants.RECALL_INITIATED, event);

            RecallInitiatedEvent received = assertEventReceived(
                    REPORTING_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);

            assertThat(received.recallId()).isEqualTo(recallId);
            assertThat(received.reason()).isEqualTo("LOOKBACK_TRIGGERED");
        }

        @Test
        @DisplayName("RecallInitiatedEvent should fan-out to notification, inventory, and reporting")
        void recallInitiatedEventShouldFanOutToAllDownstreamServices() {
            UUID recallId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            List<UUID> affectedUnitIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            RecallInitiatedEvent event = new RecallInitiatedEvent(
                    recallId, branchId, "QUALITY_DEVIATION", affectedUnitIds, Instant.now());

            publishEvent(EventConstants.RECALL_INITIATED, event);

            RecallInitiatedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
            RecallInitiatedEvent inventoryReceived = assertEventReceived(
                    INVENTORY_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
            RecallInitiatedEvent reportingReceived = assertEventReceived(
                    REPORTING_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);

            assertThat(notificationReceived.recallId()).isEqualTo(recallId);
            assertThat(inventoryReceived.recallId()).isEqualTo(recallId);
            assertThat(reportingReceived.recallId()).isEqualTo(recallId);
        }
    }

    @Nested
    @DisplayName("Step 2: Affected unit IDs preserved for investigation")
    class AffectedUnitIdsPreservedForInvestigation {

        @Test
        @DisplayName("RecallInitiatedEvent should carry all affected unit IDs without loss")
        void recallInitiatedEventShouldCarryAllAffectedUnitIds() {
            UUID recallId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID unit1 = UUID.randomUUID();
            UUID unit2 = UUID.randomUUID();
            UUID unit3 = UUID.randomUUID();
            UUID unit4 = UUID.randomUUID();
            UUID unit5 = UUID.randomUUID();
            List<UUID> affectedUnitIds = List.of(unit1, unit2, unit3, unit4, unit5);
            Instant now = Instant.now();

            RecallInitiatedEvent event = new RecallInitiatedEvent(
                    recallId, branchId, "CONTAMINATION_SUSPECTED", affectedUnitIds, now);

            publishEvent(EventConstants.RECALL_INITIATED, event);

            RecallInitiatedEvent received = assertEventReceived(
                    NOTIFICATION_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);

            assertThat(received.affectedUnitIds())
                    .hasSize(5)
                    .containsExactlyInAnyOrder(unit1, unit2, unit3, unit4, unit5);
            assertThat(received.occurredAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("RecallInitiatedEvent with single affected unit should route correctly")
        void recallInitiatedEventWithSingleAffectedUnitShouldRouteCorrectly() {
            UUID recallId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID affectedUnitId = UUID.randomUUID();

            RecallInitiatedEvent event = new RecallInitiatedEvent(
                    recallId, branchId, "DONOR_LOOKBACK", List.of(affectedUnitId), Instant.now());

            publishEvent(EventConstants.RECALL_INITIATED, event);

            RecallInitiatedEvent received = assertEventReceived(
                    INVENTORY_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);

            assertThat(received.affectedUnitIds()).hasSize(1).contains(affectedUnitId);
        }
    }

    @Nested
    @DisplayName("Recall reason categories route correctly")
    class RecallReasonCategoriesRouteCorrectly {

        @Test
        @DisplayName("Different recall reasons should all reach notification for investigation")
        void differentRecallReasonsShouldAllReachNotificationForInvestigation() {
            String[] reasons = {
                "CONTAMINATION_SUSPECTED",
                "DONOR_REACTIVE_TEST",
                "LOOKBACK_TRIGGERED",
                "QUALITY_DEVIATION",
                "STORAGE_TEMPERATURE_BREACH"
            };

            for (String reason : reasons) {
                UUID recallId = UUID.randomUUID();

                RecallInitiatedEvent event = new RecallInitiatedEvent(
                        recallId, UUID.randomUUID(), reason,
                        List.of(UUID.randomUUID()), Instant.now());

                publishEvent(EventConstants.RECALL_INITIATED, event);

                RecallInitiatedEvent received = assertEventReceived(
                        NOTIFICATION_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
                // Drain other queues
                assertEventReceived(INVENTORY_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
                assertEventReceived(REPORTING_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);

                assertThat(received.reason()).isEqualTo(reason);
                assertThat(received.recallId()).isEqualTo(recallId);
            }
        }
    }

    @Nested
    @DisplayName("Full workflow: Recall initiation to investigation event chain")
    class FullRecallWorkflowEventChain {

        @Test
        @DisplayName("Complete recall workflow should route all events to all services")
        void completeRecallWorkflowShouldRouteAllEvents() {
            UUID recallId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID unit1 = UUID.randomUUID();
            UUID unit2 = UUID.randomUUID();
            Instant now = Instant.now();

            // Recall initiated — compliance-service triggers recall for affected units
            RecallInitiatedEvent recallInitiated = new RecallInitiatedEvent(
                    recallId, branchId, "CONTAMINATION_SUSPECTED", List.of(unit1, unit2), now);
            publishEvent(EventConstants.RECALL_INITIATED, recallInitiated);

            // Notification receives for urgent staff alerting
            RecallInitiatedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
            assertThat(notificationReceived.recallId()).isEqualTo(recallId);
            assertThat(notificationReceived.affectedUnitIds()).containsExactlyInAnyOrder(unit1, unit2);

            // Inventory receives to quarantine affected units immediately
            RecallInitiatedEvent inventoryReceived = assertEventReceived(
                    INVENTORY_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
            assertThat(inventoryReceived.recallId()).isEqualTo(recallId);
            assertThat(inventoryReceived.affectedUnitIds()).containsExactlyInAnyOrder(unit1, unit2);

            // Reporting receives to create regulatory investigation record
            RecallInitiatedEvent reportingReceived = assertEventReceived(
                    REPORTING_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
            assertThat(reportingReceived.recallId()).isEqualTo(recallId);
            assertThat(reportingReceived.reason()).isEqualTo("CONTAMINATION_SUSPECTED");
            assertThat(reportingReceived.occurredAt()).isEqualTo(now);
        }
    }
}
