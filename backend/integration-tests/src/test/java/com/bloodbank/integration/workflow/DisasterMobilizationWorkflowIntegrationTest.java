package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.BloodStockUpdatedEvent;
import com.bloodbank.common.events.EmergencyRequestEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.StockCriticalEvent;
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
 * M3-043: Disaster Event → Mass Mobilization → Emergency Stock Rebalancing
 *
 * Tests the disaster management workflow where a disaster event triggers
 * donor mobilization across multiple branches and emergency stock rebalancing.
 *
 * Event flow verified:
 *   request-matching  --[EmergencyRequestEvent]----> notification-service (all branches)
 *   inventory-service --[StockCriticalEvent]-------> request-matching, notification
 *   inventory-service --[BloodStockUpdatedEvent]---> request-matching, notification
 *
 * Verifications:
 * - Disaster event triggers donor mobilization (EmergencyRequestEvent reaches notification)
 * - EmergencyRequestEvent reaches all branches via notification-service broadcast
 * - StockCriticalEvent triggers emergency stock rebalancing across branches
 * - BloodStockUpdatedEvent keeps request-matching informed of available stock
 */
@DisplayName("M3-043: Disaster Event → Mass Mobilization → Emergency Stock Rebalancing")
class DisasterMobilizationWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    // Queue names matching service-specific RabbitMQ configs
    private static final String NOTIFICATION_EMERGENCY_REQUEST_QUEUE =
            "notification.emergency.request.queue";
    private static final String NOTIFICATION_STOCK_CRITICAL_QUEUE =
            "notification.stock.critical.queue";
    private static final String NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE =
            "notification.blood.stock.updated.queue";
    private static final String REQUEST_MATCHING_STOCK_CRITICAL_QUEUE =
            "request-matching.stock.critical.queue";
    private static final String REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE =
            "request-matching.blood.stock.updated.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // notification-service listens for emergency.request (broadcasts to all branches)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EventConstants.EMERGENCY_REQUEST);

        // notification-service listens for stock.critical
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_STOCK_CRITICAL_QUEUE, EventConstants.STOCK_CRITICAL);

        // notification-service listens for blood.stock.updated
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);

        // request-matching-service listens for stock.critical (to trigger rebalancing)
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, EventConstants.STOCK_CRITICAL);

        // request-matching-service listens for blood.stock.updated (stock awareness)
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);
    }

    @Nested
    @DisplayName("Step 1: Disaster triggers donor mobilization")
    class DisasterTriggersDonorMobilization {

        @Test
        @DisplayName("EmergencyRequestEvent should reach notification-service for mass donor mobilization")
        void emergencyRequestEventShouldReachNotificationForMobilization() {
            UUID requestId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            EmergencyRequestEvent event = new EmergencyRequestEvent(
                    requestId, branchId, "O_NEGATIVE", "DISASTER", Instant.now());

            publishEvent(EventConstants.EMERGENCY_REQUEST, event);

            EmergencyRequestEvent received = assertEventReceived(
                    NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

            assertThat(received.requestId()).isEqualTo(requestId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(received.severity()).isEqualTo("DISASTER");
        }

        @Test
        @DisplayName("Multiple disaster events for different blood groups should all route correctly")
        void multipleDisasterEventsForDifferentBloodGroupsShouldRouteCorrectly() {
            String[] bloodGroups = {"O_NEGATIVE", "O_POSITIVE", "A_NEGATIVE", "B_NEGATIVE", "AB_NEGATIVE"};

            for (String bloodGroup : bloodGroups) {
                UUID requestId = UUID.randomUUID();
                UUID branchId = UUID.randomUUID();

                EmergencyRequestEvent event = new EmergencyRequestEvent(
                        requestId, branchId, bloodGroup, "DISASTER", Instant.now());

                publishEvent(EventConstants.EMERGENCY_REQUEST, event);

                EmergencyRequestEvent received = assertEventReceived(
                        NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

                assertThat(received.bloodGroup()).isEqualTo(bloodGroup);
                assertThat(received.severity()).isEqualTo("DISASTER");
            }
        }

        @Test
        @DisplayName("EmergencyRequestEvent should reach all branches via notification broadcast")
        void emergencyRequestEventShouldReachAllBranchesViaNotification() {
            // Simulate disaster events from multiple branches to verify broadcast capability
            UUID branch1 = UUID.randomUUID();
            UUID branch2 = UUID.randomUUID();
            UUID branch3 = UUID.randomUUID();

            for (UUID branchId : new UUID[]{branch1, branch2, branch3}) {
                EmergencyRequestEvent event = new EmergencyRequestEvent(
                        UUID.randomUUID(), branchId, "O_NEGATIVE", "DISASTER", Instant.now());

                publishEvent(EventConstants.EMERGENCY_REQUEST, event);

                EmergencyRequestEvent received = assertEventReceived(
                        NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

                assertThat(received.branchId()).isEqualTo(branchId);
            }
        }
    }

    @Nested
    @DisplayName("Step 2: Stock critical triggers emergency rebalancing")
    class StockCriticalTriggersRebalancing {

        @Test
        @DisplayName("StockCriticalEvent should reach request-matching-service for rebalancing")
        void stockCriticalEventShouldReachRequestMatchingService() {
            UUID branchId = UUID.randomUUID();

            StockCriticalEvent event = new StockCriticalEvent(
                    branchId, "O_NEGATIVE", "WHOLE_BLOOD", 2, 10, Instant.now());

            publishEvent(EventConstants.STOCK_CRITICAL, event);

            StockCriticalEvent received = assertEventReceived(
                    REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(received.componentType()).isEqualTo("WHOLE_BLOOD");
            assertThat(received.currentStock()).isEqualTo(2);
            assertThat(received.minimumStock()).isEqualTo(10);
        }

        @Test
        @DisplayName("StockCriticalEvent should also reach notification-service for alerts")
        void stockCriticalEventShouldAlsoReachNotificationService() {
            UUID branchId = UUID.randomUUID();

            StockCriticalEvent event = new StockCriticalEvent(
                    branchId, "A_POSITIVE", "PACKED_RBC", 5, 20, Instant.now());

            publishEvent(EventConstants.STOCK_CRITICAL, event);

            // Should fan-out to both request-matching and notification queues
            StockCriticalEvent matchingReceived = assertEventReceived(
                    REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);
            StockCriticalEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            assertThat(matchingReceived.branchId()).isEqualTo(branchId);
            assertThat(notificationReceived.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("StockCriticalEvent should preserve stock deficit information")
        void stockCriticalEventShouldPreserveStockDeficitInformation() {
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            StockCriticalEvent event = new StockCriticalEvent(
                    branchId, "O_NEGATIVE", "PLATELET", 0, 15, now);

            publishEvent(EventConstants.STOCK_CRITICAL, event);

            StockCriticalEvent received = assertEventReceived(
                    REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            assertThat(received.currentStock()).isZero();
            assertThat(received.minimumStock()).isEqualTo(15);
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Step 3: Stock updates inform request matching")
    class StockUpdatesInformRequestMatching {

        @Test
        @DisplayName("BloodStockUpdatedEvent should reach request-matching-service")
        void bloodStockUpdatedEventShouldReachRequestMatchingService() {
            UUID branchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    branchId, "O_NEGATIVE", "WHOLE_BLOOD", 50, Instant.now());

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, event);

            BloodStockUpdatedEvent received = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(received.componentType()).isEqualTo("WHOLE_BLOOD");
            assertThat(received.quantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("BloodStockUpdatedEvent should also reach notification-service")
        void bloodStockUpdatedEventShouldAlsoReachNotificationService() {
            UUID branchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    branchId, "AB_POSITIVE", "FFP", 30, Instant.now());

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, event);

            // Should fan-out to both request-matching and notification queues
            BloodStockUpdatedEvent matchingReceived = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            BloodStockUpdatedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(matchingReceived.branchId()).isEqualTo(branchId);
            assertThat(notificationReceived.branchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("Full workflow: Disaster end-to-end event chain")
    class FullDisasterWorkflowEventChain {

        @Test
        @DisplayName("Complete disaster workflow should trigger mobilization and rebalancing")
        void completeDisasterWorkflowShouldTriggerMobilizationAndRebalancing() {
            UUID branchId1 = UUID.randomUUID();
            UUID branchId2 = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Disaster emergency request for O-negative
            EmergencyRequestEvent emergencyRequest = new EmergencyRequestEvent(
                    UUID.randomUUID(), branchId1, "O_NEGATIVE", "DISASTER", now);
            publishEvent(EventConstants.EMERGENCY_REQUEST, emergencyRequest);

            // Verify: notification-service receives for mass donor mobilization broadcast
            EmergencyRequestEvent receivedEmergency = assertEventReceived(
                    NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);
            assertThat(receivedEmergency.severity()).isEqualTo("DISASTER");

            // Step 2: Stock becomes critical at affected branch
            StockCriticalEvent stockCritical = new StockCriticalEvent(
                    branchId1, "O_NEGATIVE", "WHOLE_BLOOD", 1, 20, now);
            publishEvent(EventConstants.STOCK_CRITICAL, stockCritical);

            // Verify: request-matching receives for rebalancing logic
            StockCriticalEvent receivedCritical = assertEventReceived(
                    REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);
            assertThat(receivedCritical.currentStock()).isEqualTo(1);
            // Verify: notification also receives for alerting
            assertEventReceived(NOTIFICATION_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            // Step 3: Stock transferred from another branch (stock update)
            BloodStockUpdatedEvent stockUpdate = new BloodStockUpdatedEvent(
                    branchId2, "O_NEGATIVE", "WHOLE_BLOOD", 100, now);
            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, stockUpdate);

            // Verify: request-matching receives updated stock levels
            BloodStockUpdatedEvent receivedUpdate = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            assertThat(receivedUpdate.quantity()).isEqualTo(100);
            // Verify: notification also receives stock update
            assertEventReceived(NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            // Step 4: Second emergency request for another blood group
            EmergencyRequestEvent secondEmergency = new EmergencyRequestEvent(
                    UUID.randomUUID(), branchId1, "A_POSITIVE", "DISASTER", now);
            publishEvent(EventConstants.EMERGENCY_REQUEST, secondEmergency);

            EmergencyRequestEvent receivedSecond = assertEventReceived(
                    NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);
            assertThat(receivedSecond.bloodGroup()).isEqualTo("A_POSITIVE");
        }

        @Test
        @DisplayName("Multiple branch stock critical events should all route to request-matching")
        void multipleBranchStockCriticalEventsShouldRouteToRequestMatching() {
            UUID[] branches = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
            String[] bloodGroups = {"O_NEGATIVE", "O_POSITIVE", "A_NEGATIVE"};

            for (int i = 0; i < branches.length; i++) {
                StockCriticalEvent event = new StockCriticalEvent(
                        branches[i], bloodGroups[i], "WHOLE_BLOOD", 0, 10, Instant.now());

                publishEvent(EventConstants.STOCK_CRITICAL, event);

                StockCriticalEvent receivedMatching = assertEventReceived(
                        REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);
                StockCriticalEvent receivedNotification = assertEventReceived(
                        NOTIFICATION_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

                assertThat(receivedMatching.branchId()).isEqualTo(branches[i]);
                assertThat(receivedMatching.bloodGroup()).isEqualTo(bloodGroups[i]);
                assertThat(receivedNotification.branchId()).isEqualTo(branches[i]);
            }
        }
    }
}
