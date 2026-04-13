package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.BloodStockUpdatedEvent;
import com.bloodbank.common.events.DonationCompletedEvent;
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
 * M6-001: Donor Registration → Eligibility → Collection → Lab Testing →
 * Component Processing → Inventory
 *
 * Tests the full blood donation lifecycle event chain from donation completion
 * through lab testing, component processing, and inventory stock update.
 *
 * Event flow verified:
 *   donor-service    --[DonationCompletedEvent]-------> lab-service (triggers test order)
 *   donor-service    --[DonationCompletedEvent]-------> notification-service (donor thanks)
 *   lab-service      --[TestResultAvailableEvent]-----> inventory-service (unit release decision)
 *   lab-service      --[TestResultAvailableEvent]-----> notification-service (result alert)
 *   inventory-service--[UnitReleasedEvent]------------> inventory (stock deduction on issue)
 *   inventory-service--[BloodStockUpdatedEvent]-------> request-matching (stock awareness)
 *   inventory-service--[BloodStockUpdatedEvent]-------> notification-service (stock update)
 *
 * Verifications:
 * - DonationCompletedEvent triggers lab testing (reaches lab queue)
 * - TestResultAvailableEvent triggers component processing/unit release (reaches inventory queue)
 * - UnitReleasedEvent reaches inventory update queue
 * - BloodStockUpdatedEvent fans out to request-matching and notification
 */
@DisplayName("M6-001: Donor Registration → Eligibility → Collection → Lab Testing → Component Processing → Inventory")
class BloodDonationLifecycleWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String LAB_DONATION_COMPLETED_QUEUE =
            "lab.donation.completed.queue";
    private static final String NOTIFICATION_DONATION_COMPLETED_QUEUE =
            "notification.donation.completed.queue";
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
        // lab-service listens for donation.completed to create test order
        declareAndBindQueue(admin, exchange,
                LAB_DONATION_COMPLETED_QUEUE, EventConstants.DONATION_COMPLETED);

        // notification-service listens for donation.completed (thank-you notification)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_DONATION_COMPLETED_QUEUE, EventConstants.DONATION_COMPLETED);

        // inventory-service listens for test.result.available (to release or quarantine unit)
        declareAndBindQueue(admin, exchange,
                INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, EventConstants.TEST_RESULT_AVAILABLE);

        // notification-service listens for test.result.available (result alerts)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_TEST_RESULT_AVAILABLE_QUEUE, EventConstants.TEST_RESULT_AVAILABLE);

        // inventory-service listens for unit.released (stock deduction)
        declareAndBindQueue(admin, exchange,
                INVENTORY_UNIT_RELEASED_QUEUE, EventConstants.UNIT_RELEASED);

        // request-matching-service listens for blood.stock.updated (stock awareness)
        declareAndBindQueue(admin, exchange,
                REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);

        // notification-service listens for blood.stock.updated (stock level alerts)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);
    }

    @Nested
    @DisplayName("Step 1: Donation completed triggers lab testing")
    class DonationCompletedTriggersLabTesting {

        @Test
        @DisplayName("DonationCompletedEvent should reach lab-service queue for test order creation")
        void donationCompletedEventShouldReachLabService() {
            UUID donationId = UUID.randomUUID();
            UUID donorId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            DonationCompletedEvent event = new DonationCompletedEvent(
                    donationId, donorId, branchId, Instant.now());

            publishEvent(EventConstants.DONATION_COMPLETED, event);

            DonationCompletedEvent received = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(received.donationId()).isEqualTo(donationId);
            assertThat(received.donorId()).isEqualTo(donorId);
            assertThat(received.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("DonationCompletedEvent should fan-out to lab-service and notification-service")
        void donationCompletedEventShouldFanOutToLabAndNotification() {
            UUID donationId = UUID.randomUUID();
            UUID donorId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            DonationCompletedEvent event = new DonationCompletedEvent(
                    donationId, donorId, branchId, Instant.now());

            publishEvent(EventConstants.DONATION_COMPLETED, event);

            DonationCompletedEvent labReceived = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(labReceived.donationId()).isEqualTo(donationId);
            assertThat(notificationReceived.donationId()).isEqualTo(donationId);
        }

        @Test
        @DisplayName("DonationCompletedEvent should preserve all fields through serialization")
        void donationCompletedEventShouldPreserveAllFields() {
            UUID donationId = UUID.randomUUID();
            UUID donorId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            DonationCompletedEvent event = new DonationCompletedEvent(
                    donationId, donorId, branchId, now);

            publishEvent(EventConstants.DONATION_COMPLETED, event);

            DonationCompletedEvent received = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(received.donationId()).isEqualTo(donationId);
            assertThat(received.donorId()).isEqualTo(donorId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Step 2: Test result triggers component processing")
    class TestResultTriggersComponentProcessing {

        @Test
        @DisplayName("TestResultAvailableEvent should reach inventory-service for unit release decision")
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
        @DisplayName("TestResultAvailableEvent should fan-out to inventory-service and notification-service")
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
        @DisplayName("TestResultAvailableEvent should preserve bloodUnitId for unit lifecycle tracing")
        void testResultAvailableEventShouldPreserveBloodUnitId() {
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
    }

    @Nested
    @DisplayName("Step 3: Unit release updates inventory stock")
    class UnitReleaseUpdatesInventoryStock {

        @Test
        @DisplayName("UnitReleasedEvent should reach inventory-service for stock deduction")
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
    @DisplayName("Step 4: Stock level update notifies downstream services")
    class StockUpdateNotifiesDownstream {

        @Test
        @DisplayName("BloodStockUpdatedEvent should reach request-matching-service for stock awareness")
        void bloodStockUpdatedEventShouldReachRequestMatchingService() {
            UUID branchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    branchId, "A_POSITIVE", "WHOLE_BLOOD", 42, Instant.now());

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, event);

            BloodStockUpdatedEvent received = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("A_POSITIVE");
            assertThat(received.componentType()).isEqualTo("WHOLE_BLOOD");
            assertThat(received.quantity()).isEqualTo(42);
        }

        @Test
        @DisplayName("BloodStockUpdatedEvent should fan-out to request-matching and notification")
        void bloodStockUpdatedEventShouldFanOutToRequestMatchingAndNotification() {
            UUID branchId = UUID.randomUUID();

            BloodStockUpdatedEvent event = new BloodStockUpdatedEvent(
                    branchId, "O_NEGATIVE", "PACKED_RBC", 8, Instant.now());

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
    @DisplayName("Full lifecycle: Donation → Lab → Component Processing → Inventory")
    class FullDonationLifecycleEventChain {

        @Test
        @DisplayName("Complete donation lifecycle should route all events in correct sequence")
        void completeDonationLifecycleShouldRouteAllEvents() {
            UUID donationId = UUID.randomUUID();
            UUID donorId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID testOrderId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Donation completed → triggers lab testing
            DonationCompletedEvent donationCompleted = new DonationCompletedEvent(
                    donationId, donorId, branchId, now);
            publishEvent(EventConstants.DONATION_COMPLETED, donationCompleted);

            DonationCompletedEvent receivedByLab = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent receivedByNotification = assertEventReceived(
                    NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            assertThat(receivedByLab.donationId()).isEqualTo(donationId);
            assertThat(receivedByNotification.donationId()).isEqualTo(donationId);

            // Step 2: Test result available → triggers component processing / unit release decision
            TestResultAvailableEvent testResultAvailable = new TestResultAvailableEvent(
                    testOrderId, bloodUnitId, branchId, now);
            publishEvent(EventConstants.TEST_RESULT_AVAILABLE, testResultAvailable);

            TestResultAvailableEvent receivedByInventory = assertEventReceived(
                    INVENTORY_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);
            assertEventReceived(NOTIFICATION_TEST_RESULT_AVAILABLE_QUEUE, TestResultAvailableEvent.class);
            assertThat(receivedByInventory.bloodUnitId()).isEqualTo(bloodUnitId);

            // Step 3: Unit released after processing (cleared units enter inventory)
            UnitReleasedEvent unitReleased = new UnitReleasedEvent(bloodUnitId, branchId, now);
            publishEvent(EventConstants.UNIT_RELEASED, unitReleased);

            UnitReleasedEvent receivedRelease = assertEventReceived(
                    INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);
            assertThat(receivedRelease.bloodUnitId()).isEqualTo(bloodUnitId);

            // Step 4: Blood stock updated after unit enters inventory
            BloodStockUpdatedEvent stockUpdated = new BloodStockUpdatedEvent(
                    branchId, "A_POSITIVE", "WHOLE_BLOOD", 25, now);
            publishEvent(EventConstants.BLOOD_STOCK_UPDATED, stockUpdated);

            BloodStockUpdatedEvent receivedStockMatching = assertEventReceived(
                    REQUEST_MATCHING_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            BloodStockUpdatedEvent receivedStockNotification = assertEventReceived(
                    NOTIFICATION_BLOOD_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            assertThat(receivedStockMatching.quantity()).isEqualTo(25);
            assertThat(receivedStockNotification.quantity()).isEqualTo(25);
        }

        @Test
        @DisplayName("Multiple donations from the same branch should each produce independent events")
        void multipleDonationsShouldProduceIndependentEvents() {
            UUID branchId = UUID.randomUUID();

            UUID donationId1 = UUID.randomUUID();
            UUID donationId2 = UUID.randomUUID();

            DonationCompletedEvent event1 = new DonationCompletedEvent(
                    donationId1, UUID.randomUUID(), branchId, Instant.now());
            DonationCompletedEvent event2 = new DonationCompletedEvent(
                    donationId2, UUID.randomUUID(), branchId, Instant.now());

            publishEvent(EventConstants.DONATION_COMPLETED, event1);
            publishEvent(EventConstants.DONATION_COMPLETED, event2);

            DonationCompletedEvent first = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent second = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(first.donationId()).isNotEqualTo(second.donationId());
            assertThat(first.branchId()).isEqualTo(branchId);
            assertThat(second.branchId()).isEqualTo(branchId);
        }
    }
}
