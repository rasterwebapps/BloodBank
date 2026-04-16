package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.BloodStockUpdatedEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.StockCriticalEvent;
import com.bloodbank.common.events.UnitExpiringEvent;
import com.bloodbank.integration.support.AbstractWorkflowIntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-008: Inter-Branch Transfer → Cold Chain → Delivery Confirmation
 *
 * Tests the inter-branch blood unit transfer workflow including cold chain
 * monitoring, stock updates at source and destination branches, and critical
 * stock alerts when a branch transfers out its reserves.
 *
 * Event flow verified:
 *   inventory-service--[BloodStockUpdatedEvent]------> request-matching (source branch stock down)
 *   inventory-service--[BloodStockUpdatedEvent]------> notification-service (source stock update)
 *   inventory-service--[StockCriticalEvent]----------> request-matching (source stock critical)
 *   inventory-service--[StockCriticalEvent]----------> notification-service (critical alert)
 *   inventory-service--[BloodStockUpdatedEvent]------> request-matching (destination stock up)
 *   inventory-service--[BloodStockUpdatedEvent]------> notification-service (destination update)
 *   inventory-service--[UnitExpiringEvent]-----------> notification-service (cold chain warning)
 *
 * Verifications:
 * - Source branch stock decrease routes to request-matching and notification
 * - StockCriticalEvent triggers when source stock falls below minimum after transfer
 * - Destination branch stock increase routes to request-matching and notification
 * - UnitExpiringEvent from cold chain monitoring routes to notification
 * - Multiple component types transfer events route independently
 */
@DisplayName("M6-008: Inter-Branch Transfer → Cold Chain → Delivery Confirmation")
class InterBranchTransferWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE =
            "request-matching.blood.stock.updated.queue";
    private static final String NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE =
            "notification.blood.stock.updated.queue";
    private static final String REQUEST_MATCHING_STOCK_CRITICAL_QUEUE =
            "request-matching.stock.critical.queue";
    private static final String NOTIFICATION_STOCK_CRITICAL_QUEUE =
            "notification.stock.critical.queue";
    private static final String NOTIFICATION_UNIT_EXPIRING_QUEUE =
            "notification.unit.expiring.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // request-matching-service listens for blood.stock.updated (updated stock awareness)
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);

        // notification-service listens for blood.stock.updated (stock level change alerts)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);

        // request-matching-service listens for stock.critical (to trigger rebalancing)
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, EventConstants.STOCK_CRITICAL);

        // notification-service listens for stock.critical (urgent stock alerts)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_STOCK_CRITICAL_QUEUE, EventConstants.STOCK_CRITICAL);

        // notification-service listens for unit.expiring (cold chain / expiry alerts)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_UNIT_EXPIRING_QUEUE, EventConstants.UNIT_EXPIRING);
    }

    @Nested
    @DisplayName("Step 1: Source branch stock reduced after transfer out")
    class SourceBranchStockReducedAfterTransfer {

        @Test
        @DisplayName("BloodStockUpdatedEvent for source branch stock decrease should reach request-matching")
        void sourceStockDecreaseShouldReachRequestMatching() {
            UUID sourceBranchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    sourceBranchId, "O_NEGATIVE", "WHOLE_BLOOD", 3, Instant.now());

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, event);

            BloodStockUpdatedEvent received = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(received.branchId()).isEqualTo(sourceBranchId);
            assertThat(received.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(received.quantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("BloodStockUpdatedEvent for source branch should fan-out to request-matching and notification")
        void sourceStockUpdateShouldFanOutToMatchingAndNotification() {
            UUID sourceBranchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    sourceBranchId, "A_NEGATIVE", "PACKED_RBC", 5, Instant.now());

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, event);

            BloodStockUpdatedEvent matchingReceived = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            BloodStockUpdatedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(matchingReceived.branchId()).isEqualTo(sourceBranchId);
            assertThat(notificationReceived.branchId()).isEqualTo(sourceBranchId);
        }
    }

    @Nested
    @DisplayName("Step 2: Source branch stock becomes critical after transfer")
    class SourceStockCriticalAfterTransfer {

        @Test
        @DisplayName("StockCriticalEvent should reach request-matching when source stock falls critical")
        void stockCriticalEventShouldReachRequestMatchingForRebalancing() {
            UUID sourceBranchId = UUID.randomUUID();

            StockCriticalEvent event = new StockCriticalEvent(
                    sourceBranchId, "O_NEGATIVE", "WHOLE_BLOOD", 1, 10, Instant.now());

            publishEvent(EventConstants.STOCK_CRITICAL, event);

            StockCriticalEvent received = assertEventReceived(
                    REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            assertThat(received.branchId()).isEqualTo(sourceBranchId);
            assertThat(received.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(received.currentStock()).isEqualTo(1);
            assertThat(received.minimumStock()).isEqualTo(10);
        }

        @Test
        @DisplayName("StockCriticalEvent should fan-out to request-matching and notification")
        void stockCriticalEventShouldFanOutToMatchingAndNotification() {
            UUID sourceBranchId = UUID.randomUUID();

            StockCriticalEvent event = new StockCriticalEvent(
                    sourceBranchId, "B_NEGATIVE", "PLATELET", 0, 15, Instant.now());

            publishEvent(EventConstants.STOCK_CRITICAL, event);

            StockCriticalEvent matchingReceived = assertEventReceived(
                    REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);
            StockCriticalEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            assertThat(matchingReceived.branchId()).isEqualTo(sourceBranchId);
            assertThat(notificationReceived.branchId()).isEqualTo(sourceBranchId);
            assertThat(matchingReceived.currentStock()).isZero();
        }
    }

    @Nested
    @DisplayName("Step 3: Destination branch stock increased after transfer in")
    class DestinationBranchStockIncreasedAfterTransfer {

        @Test
        @DisplayName("BloodStockUpdatedEvent for destination branch stock increase should reach request-matching")
        void destinationStockIncreaseShouldReachRequestMatching() {
            UUID destinationBranchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    destinationBranchId, "O_NEGATIVE", "WHOLE_BLOOD", 12, Instant.now());

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, event);

            BloodStockUpdatedEvent received = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(received.branchId()).isEqualTo(destinationBranchId);
            assertThat(received.quantity()).isEqualTo(12);
        }

        @Test
        @DisplayName("Source and destination stock updates should route as separate events")
        void sourceAndDestinationStockUpdatesShouldRouteSeparately() {
            UUID sourceBranchId = UUID.randomUUID();
            UUID destinationBranchId = UUID.randomUUID();

            // Source stock decreases
            BloodStockUpdatedEvent sourceEvent = new BloodStockUpdatedEvent(
                    sourceBranchId, "A_POSITIVE", "WHOLE_BLOOD", 2, Instant.now());
            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, sourceEvent);

            // Destination stock increases
            BloodStockUpdatedEvent destEvent = new BloodStockUpdatedEvent(
                    destinationBranchId, "A_POSITIVE", "WHOLE_BLOOD", 18, Instant.now());
            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, destEvent);

            BloodStockUpdatedEvent first = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            BloodStockUpdatedEvent second = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(first.branchId()).isNotEqualTo(second.branchId());
        }
    }

    @Nested
    @DisplayName("Step 4: Cold chain monitoring triggers expiry alerts")
    class ColdChainMonitoringTriggersExpiryAlerts {

        @Test
        @DisplayName("UnitExpiringEvent should reach notification-service for cold chain warning")
        void unitExpiringEventShouldReachNotificationForColdChainWarning() {
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            LocalDate expiryDate = LocalDate.now().plusDays(2);
            Instant now = Instant.now();

            UnitExpiringEvent event = new UnitExpiringEvent(bloodUnitId, branchId, expiryDate, now);

            publishEvent(EventConstants.UNIT_EXPIRING, event);

            UnitExpiringEvent received = assertEventReceived(
                    NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);

            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.expiryDate()).isEqualTo(expiryDate);
        }

        @Test
        @DisplayName("UnitExpiringEvent should preserve expiry date for cold chain compliance")
        void unitExpiringEventShouldPreserveExpiryDateForColdChainCompliance() {
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            LocalDate expiryDate = LocalDate.now().plusDays(1);
            Instant now = Instant.now();

            UnitExpiringEvent event = new UnitExpiringEvent(bloodUnitId, branchId, expiryDate, now);

            publishEvent(EventConstants.UNIT_EXPIRING, event);

            UnitExpiringEvent received = assertEventReceived(
                    NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);

            assertThat(received.expiryDate()).isEqualTo(expiryDate);
            assertThat(received.occurredAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Multiple expiring units during transfer should each produce independent alerts")
        void multipleExpiringUnitsShouldProduceIndependentAlerts() {
            UUID branchId = UUID.randomUUID();
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            UUID unit1 = UUID.randomUUID();
            UUID unit2 = UUID.randomUUID();
            UUID unit3 = UUID.randomUUID();

            publishEvent(EventConstants.UNIT_EXPIRING,
                    new UnitExpiringEvent(unit1, branchId, tomorrow, Instant.now()));
            publishEvent(EventConstants.UNIT_EXPIRING,
                    new UnitExpiringEvent(unit2, branchId, tomorrow, Instant.now()));
            publishEvent(EventConstants.UNIT_EXPIRING,
                    new UnitExpiringEvent(unit3, branchId, tomorrow, Instant.now()));

            UnitExpiringEvent first = assertEventReceived(
                    NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);
            UnitExpiringEvent second = assertEventReceived(
                    NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);
            UnitExpiringEvent third = assertEventReceived(
                    NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);

            assertThat(first.bloodUnitId()).isNotEqualTo(second.bloodUnitId());
            assertThat(second.bloodUnitId()).isNotEqualTo(third.bloodUnitId());
            assertThat(first.branchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("Full workflow: Inter-branch transfer with cold chain end-to-end")
    class FullInterBranchTransferWorkflowEventChain {

        @Test
        @DisplayName("Complete inter-branch transfer should route stock and expiry events correctly")
        void completeInterBranchTransferShouldRouteAllEvents() {
            UUID sourceBranchId = UUID.randomUUID();
            UUID destinationBranchId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Source branch stock decreases after transfer out
            BloodStockUpdatedEvent sourceDecrease = new BloodStockUpdatedEvent(
                    sourceBranchId, "O_NEGATIVE", "WHOLE_BLOOD", 2, now);
            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, sourceDecrease);

            BloodStockUpdatedEvent receivedSourceDecrease = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            assertEventReceived(NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            assertThat(receivedSourceDecrease.branchId()).isEqualTo(sourceBranchId);
            assertThat(receivedSourceDecrease.quantity()).isEqualTo(2);

            // Step 2: Source branch stock falls critical after transfer
            StockCriticalEvent sourceCritical = new StockCriticalEvent(
                    sourceBranchId, "O_NEGATIVE", "WHOLE_BLOOD", 2, 10, now);
            publishEvent(EventConstants.STOCK_CRITICAL, sourceCritical);

            StockCriticalEvent receivedCritical = assertEventReceived(
                    REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);
            assertEventReceived(NOTIFICATION_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);
            assertThat(receivedCritical.currentStock()).isEqualTo(2);
            assertThat(receivedCritical.minimumStock()).isEqualTo(10);

            // Step 3: Cold chain monitoring detects expiring unit during transport
            UnitExpiringEvent expiryWarning = new UnitExpiringEvent(
                    bloodUnitId, destinationBranchId, LocalDate.now().plusDays(1), now);
            publishEvent(EventConstants.UNIT_EXPIRING, expiryWarning);

            UnitExpiringEvent receivedExpiry = assertEventReceived(
                    NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);
            assertThat(receivedExpiry.bloodUnitId()).isEqualTo(bloodUnitId);

            // Step 4: Destination branch stock increases after delivery confirmed
            BloodStockUpdatedEvent destIncrease = new BloodStockUpdatedEvent(
                    destinationBranchId, "O_NEGATIVE", "WHOLE_BLOOD", 18, now);
            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, destIncrease);

            BloodStockUpdatedEvent receivedDestIncrease = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            assertEventReceived(NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            assertThat(receivedDestIncrease.branchId()).isEqualTo(destinationBranchId);
            assertThat(receivedDestIncrease.quantity()).isEqualTo(18);
        }
    }
}
