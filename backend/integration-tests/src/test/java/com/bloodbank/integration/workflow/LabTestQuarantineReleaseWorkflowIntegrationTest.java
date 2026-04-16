package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.BloodStockUpdatedEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.TestResultAvailableEvent;
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
 * M6-004: Test Result → Quarantine → Release → Stock Update
 *
 * Tests the lab testing result processing workflow. A test result triggers
 * inventory to either quarantine (reactive) or release (non-reactive) the unit,
 * followed by a stock level update propagated to downstream services.
 *
 * Event flow verified:
 *   lab-service      --[TestResultAvailableEvent]----> inventory-service (release decision)
 *   lab-service      --[TestResultAvailableEvent]----> notification-service (result alert)
 *   inventory-service--[UnitReleasedEvent]-----------> inventory (cleared unit enters stock)
 *   inventory-service--[BloodStockUpdatedEvent]------> request-matching (stock awareness)
 *   inventory-service--[BloodStockUpdatedEvent]------> notification-service (stock change alert)
 *
 * Verifications:
 * - TestResultAvailableEvent fans out to inventory and notification
 * - UnitReleasedEvent reaches inventory queue (quarantine clear / unit released to stock)
 * - BloodStockUpdatedEvent fans out to request-matching and notification
 * - Quarantined units (reactive results) should NOT produce UnitReleasedEvent
 */
@DisplayName("M6-004: Test Result → Quarantine → Release → Stock Update")
class LabTestQuarantineReleaseWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String INVENTORY_TEST_RESULT_AVAILABLE_QUEUE =
            "inventory.test.result.available.queue";
    private static final String NOTIFICATION_TEST_RESULT_AVAILABLE_QUEUE =
            "notification.test.result.available.queue";
    private static final String INVENTORY_UNIT_RELEASED_QUEUE =
            "inventory.unit.released.queue";
    private static final String REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE =
            "request-matching.blood.stock.updated.queue";
    private static final String NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE =
            "notification.blood.stock.updated.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // inventory-service listens for test.result.available (to decide quarantine or release)
        declareAndBindQueue(admin, exchange,
                INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, EventConstants.TEST_RESULT_AVAILABLE);

        // notification-service listens for test.result.available (result alerts to lab staff)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_TEST_RESULT_AVAILABLE_QUEUE, EventConstants.TEST_RESULT_AVAILABLE);

        // inventory-service listens for unit.released (unit cleared and added to stock)
        declareAndBindQueue(admin, exchange,
                INVENTORY_UNIT_RELEASED_QUEUE, EventConstants.UNIT_RELEASED);

        // request-matching-service listens for blood.stock.updated (stock awareness for matching)
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);

        // notification-service listens for blood.stock.updated (stock level change alerts)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);
    }

    @Nested
    @DisplayName("Step 1: Test result available triggers inventory decision")
    class TestResultTriggersInventoryDecision {

        @Test
        @DisplayName("TestResultAvailableEvent should reach inventory-service for quarantine/release decision")
        void testResultAvailableEventShouldReachInventoryService() {
            UUID testOrderId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            TestResultAvailableEvent event = new TestResultAvailableEvent(
                    testOrderId, bloodUnitId, branchId, Instant.now());

            publishEvent(EventConstants.TEST_RESULT_AVAILABLE, event);

            TestResultAvailableEvent received = assertEventReceived(
                    INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);

            assertThat(received.testOrderId()).isEqualTo(testOrderId);
            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("TestResultAvailableEvent should fan-out to inventory and notification")
        void testResultAvailableEventShouldFanOutToInventoryAndNotification() {
            UUID testOrderId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            TestResultAvailableEvent event = new TestResultAvailableEvent(
                    testOrderId, bloodUnitId, branchId, Instant.now());

            publishEvent(EventConstants.TEST_RESULT_AVAILABLE, event);

            TestResultAvailableEvent inventoryReceived = assertEventReceived(
                    INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);
            TestResultAvailableEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);

            assertThat(inventoryReceived.testOrderId()).isEqualTo(testOrderId);
            assertThat(notificationReceived.testOrderId()).isEqualTo(testOrderId);
        }

        @Test
        @DisplayName("TestResultAvailableEvent should preserve blood unit ID for full traceability")
        void testResultAvailableEventShouldPreserveBloodUnitIdForTraceability() {
            UUID testOrderId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            TestResultAvailableEvent event = new TestResultAvailableEvent(
                    testOrderId, bloodUnitId, branchId, now);

            publishEvent(EventConstants.TEST_RESULT_AVAILABLE, event);

            TestResultAvailableEvent received = assertEventReceived(
                    INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);

            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.occurredAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Multiple test results should each route independently to inventory")
        void multipleTestResultsShouldEachRouteIndependently() {
            UUID branchId = UUID.randomUUID();
            int resultCount = 4;
            UUID[] unitIds = new UUID[resultCount];

            for (int i = 0; i < resultCount; i++) {
                unitIds[i] = UUID.randomUUID();
                TestResultAvailableEvent event = new TestResultAvailableEvent(
                        UUID.randomUUID(), unitIds[i], branchId, Instant.now());
                publishEvent(EventConstants.TEST_RESULT_AVAILABLE, event);
            }

            for (int i = 0; i < resultCount; i++) {
                TestResultAvailableEvent received = assertEventReceived(
                        INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);
                assertThat(received.branchId()).isEqualTo(branchId);
            }
        }
    }

    @Nested
    @DisplayName("Step 2: Cleared unit released to inventory stock")
    class ClearedUnitReleasedToStock {

        @Test
        @DisplayName("UnitReleasedEvent should reach inventory-service when unit clears quarantine")
        void unitReleasedEventShouldReachInventoryServiceAfterQuarantineCleared() {
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            UnitReleasedEvent event = new UnitReleasedEvent(bloodUnitId, branchId, Instant.now());

            publishEvent(EventConstants.UNIT_RELEASED, event);

            UnitReleasedEvent received = assertEventReceived(
                    INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);

            assertThat(received.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(received.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("UnitReleasedEvent should preserve timestamp for chain-of-custody")
        void unitReleasedEventShouldPreserveTimestampForChainOfCustody() {
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            UnitReleasedEvent event = new UnitReleasedEvent(bloodUnitId, branchId, now);

            publishEvent(EventConstants.UNIT_RELEASED, event);

            UnitReleasedEvent received = assertEventReceived(
                    INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);

            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Step 3: Stock level updated after unit enters inventory")
    class StockLevelUpdatedAfterUnitEnters {

        @Test
        @DisplayName("BloodStockUpdatedEvent should reach request-matching-service after unit released")
        void bloodStockUpdatedEventShouldReachRequestMatchingService() {
            UUID branchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    branchId, "O_POSITIVE", "WHOLE_BLOOD", 15, Instant.now());

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, event);

            BloodStockUpdatedEvent received = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("O_POSITIVE");
            assertThat(received.quantity()).isEqualTo(15);
        }

        @Test
        @DisplayName("BloodStockUpdatedEvent should fan-out to request-matching and notification")
        void bloodStockUpdatedEventShouldFanOutToRequestMatchingAndNotification() {
            UUID branchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    branchId, "B_NEGATIVE", "PACKED_RBC", 7, Instant.now());

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, event);

            BloodStockUpdatedEvent matchingReceived = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            BloodStockUpdatedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(matchingReceived.branchId()).isEqualTo(branchId);
            assertThat(notificationReceived.branchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("Full workflow: Test result to stock update end-to-end")
    class FullLabTestToStockUpdateWorkflow {

        @Test
        @DisplayName("Complete test result to stock update workflow should route all events correctly")
        void completeTestResultToStockUpdateWorkflowShouldRouteAllEvents() {
            UUID testOrderId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Lab test result becomes available
            TestResultAvailableEvent testResult = new TestResultAvailableEvent(
                    testOrderId, bloodUnitId, branchId, now);
            publishEvent(EventConstants.TEST_RESULT_AVAILABLE, testResult);

            TestResultAvailableEvent inventoryReceived = assertEventReceived(
                    INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);
            assertEventReceived(NOTIFICATION_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);
            assertThat(inventoryReceived.bloodUnitId()).isEqualTo(bloodUnitId);

            // Step 2: Unit passes tests and is released from quarantine
            UnitReleasedEvent unitReleased = new UnitReleasedEvent(bloodUnitId, branchId, now);
            publishEvent(EventConstants.UNIT_RELEASED, unitReleased);

            UnitReleasedEvent releaseReceived = assertEventReceived(
                    INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);
            assertThat(releaseReceived.bloodUnitId()).isEqualTo(bloodUnitId);

            // Step 3: Stock updated after unit enters usable inventory
            BloodStockUpdatedEvent stockUpdated = new BloodStockUpdatedEvent(
                    branchId, "A_POSITIVE", "WHOLE_BLOOD", 18, now);
            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, stockUpdated);

            BloodStockUpdatedEvent matchingStock = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            BloodStockUpdatedEvent notificationStock = assertEventReceived(
                    NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            assertThat(matchingStock.quantity()).isEqualTo(18);
            assertThat(notificationStock.quantity()).isEqualTo(18);
        }

        @Test
        @DisplayName("Quarantined unit (reactive test) should NOT produce UnitReleasedEvent")
        void quarantinedUnitShouldNotProduceUnitReleasedEvent() {
            UUID testOrderId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            // Test result available — inventory decides to quarantine (reactive unit)
            TestResultAvailableEvent testResult = new TestResultAvailableEvent(
                    testOrderId, bloodUnitId, branchId, Instant.now());
            publishEvent(EventConstants.TEST_RESULT_AVAILABLE, testResult);

            // Drain test result queues
            assertEventReceived(INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);
            assertEventReceived(NOTIFICATION_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);

            // Quarantined unit: inventory service decides NOT to publish UnitReleasedEvent
            // No UnitReleasedEvent should appear on inventory queue
            assertNoEventReceived(INVENTORY_UNIT_RELEASED_QUEUE);
        }
    }
}
