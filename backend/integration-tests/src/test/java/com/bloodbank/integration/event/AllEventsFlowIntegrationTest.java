package com.bloodbank.integration.event;

import com.bloodbank.common.events.BloodRequestCreatedEvent;
import com.bloodbank.common.events.BloodRequestMatchedEvent;
import com.bloodbank.common.events.BloodStockUpdatedEvent;
import com.bloodbank.common.events.CampCompletedEvent;
import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.events.EmergencyRequestEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.InvoiceGeneratedEvent;
import com.bloodbank.common.events.RecallInitiatedEvent;
import com.bloodbank.common.events.StockCriticalEvent;
import com.bloodbank.common.events.TestResultAvailableEvent;
import com.bloodbank.common.events.TransfusionCompletedEvent;
import com.bloodbank.common.events.TransfusionReactionEvent;
import com.bloodbank.common.events.UnitExpiringEvent;
import com.bloodbank.common.events.UnitReleasedEvent;
import com.bloodbank.integration.support.AbstractWorkflowIntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-010: Verify all 14 RabbitMQ events flow correctly across service boundaries.
 *
 * <p>Tests every event routing key defined in {@link EventConstants} against the
 * consumer queues declared by each downstream service's {@code RabbitMQConfig}.
 *
 * <p>Fan-out mapping (event → consuming services / queues):
 * <ul>
 *   <li>DonationCompletedEvent       → lab, inventory, notification, reporting</li>
 *   <li>CampCompletedEvent           → notification, reporting</li>
 *   <li>TestResultAvailableEvent     → inventory, notification, reporting</li>
 *   <li>UnitReleasedEvent            → inventory, notification, reporting</li>
 *   <li>BloodStockUpdatedEvent       → request-matching, notification, reporting</li>
 *   <li>StockCriticalEvent           → notification, request-matching</li>
 *   <li>UnitExpiringEvent            → notification, reporting</li>
 *   <li>BloodRequestCreatedEvent     → request-matching, notification, reporting</li>
 *   <li>BloodRequestMatchedEvent     → billing, notification, reporting</li>
 *   <li>EmergencyRequestEvent        → notification, donor</li>
 *   <li>TransfusionCompletedEvent    → notification, reporting</li>
 *   <li>TransfusionReactionEvent     → notification, reporting</li>
 *   <li>InvoiceGeneratedEvent        → notification, reporting</li>
 *   <li>RecallInitiatedEvent         → notification, reporting</li>
 * </ul>
 */
@DisplayName("M6-010: All RabbitMQ Events — Fan-Out Routing Verification")
class AllEventsFlowIntegrationTest extends AbstractWorkflowIntegrationTest {

    // ── DonationCompleted ─────────────────────────────────────────────────────
    private static final String LAB_DONATION_COMPLETED_QUEUE         = "lab.donation.completed.queue";
    private static final String INVENTORY_DONATION_COMPLETED_QUEUE   = "inventory.donation.completed.queue";
    private static final String NOTIFICATION_DONATION_COMPLETED_QUEUE = "notification.donation.completed.queue";
    private static final String REPORTING_DONATION_COMPLETED_QUEUE   = "reporting.donation.completed.queue";

    // ── CampCompleted ─────────────────────────────────────────────────────────
    private static final String NOTIFICATION_CAMP_COMPLETED_QUEUE    = "notification.camp.completed.queue";
    private static final String REPORTING_CAMP_COMPLETED_QUEUE       = "reporting.camp.completed.queue";

    // ── TestResultAvailable ───────────────────────────────────────────────────
    private static final String INVENTORY_TEST_RESULT_QUEUE          = "inventory.test.result.available.queue";
    private static final String NOTIFICATION_TEST_RESULT_QUEUE       = "notification.test.result.available.queue";
    private static final String REPORTING_TEST_RESULT_QUEUE          = "reporting.test.result.available.queue";

    // ── UnitReleased ──────────────────────────────────────────────────────────
    private static final String INVENTORY_UNIT_RELEASED_QUEUE        = "inventory.unit.released.queue";
    private static final String NOTIFICATION_UNIT_RELEASED_QUEUE     = "notification.unit.released.queue";
    private static final String REPORTING_UNIT_RELEASED_QUEUE        = "reporting.unit.released.queue";

    // ── BloodStockUpdated ─────────────────────────────────────────────────────
    private static final String REQUEST_MATCHING_STOCK_UPDATED_QUEUE = "request-matching.blood.stock.updated.queue";
    private static final String NOTIFICATION_STOCK_UPDATED_QUEUE     = "notification.blood.stock.updated.queue";
    private static final String REPORTING_STOCK_UPDATED_QUEUE        = "reporting.blood.stock.updated.queue";

    // ── StockCritical ─────────────────────────────────────────────────────────
    private static final String NOTIFICATION_STOCK_CRITICAL_QUEUE    = "notification.stock.critical.queue";
    private static final String REQUEST_MATCHING_STOCK_CRITICAL_QUEUE = "request-matching.stock.critical.queue";

    // ── UnitExpiring ──────────────────────────────────────────────────────────
    private static final String NOTIFICATION_UNIT_EXPIRING_QUEUE     = "notification.unit.expiring.queue";
    private static final String REPORTING_UNIT_EXPIRING_QUEUE        = "reporting.unit.expiring.queue";

    // ── BloodRequestCreated ───────────────────────────────────────────────────
    private static final String REQUEST_MATCHING_REQUEST_CREATED_QUEUE = "request-matching.blood.request.created.queue";
    private static final String NOTIFICATION_REQUEST_CREATED_QUEUE   = "notification.blood.request.created.queue";
    private static final String REPORTING_REQUEST_CREATED_QUEUE      = "reporting.blood.request.created.queue";

    // ── BloodRequestMatched ───────────────────────────────────────────────────
    private static final String BILLING_REQUEST_MATCHED_QUEUE        = "billing.blood.request.matched.queue";
    private static final String NOTIFICATION_REQUEST_MATCHED_QUEUE   = "notification.blood.request.matched.queue";
    private static final String REPORTING_REQUEST_MATCHED_QUEUE      = "reporting.blood.request.matched.queue";

    // ── EmergencyRequest ──────────────────────────────────────────────────────
    private static final String NOTIFICATION_EMERGENCY_REQUEST_QUEUE = "notification.emergency.request.queue";
    private static final String DONOR_EMERGENCY_REQUEST_QUEUE        = "donor.emergency.request.queue";

    // ── TransfusionCompleted ──────────────────────────────────────────────────
    private static final String NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE = "notification.transfusion.completed.queue";
    private static final String REPORTING_TRANSFUSION_COMPLETED_QUEUE    = "reporting.transfusion.completed.queue";

    // ── TransfusionReaction ───────────────────────────────────────────────────
    private static final String NOTIFICATION_TRANSFUSION_REACTION_QUEUE = "notification.transfusion.reaction.queue";
    private static final String REPORTING_TRANSFUSION_REACTION_QUEUE    = "reporting.transfusion.reaction.queue";

    // ── InvoiceGenerated ──────────────────────────────────────────────────────
    private static final String NOTIFICATION_INVOICE_GENERATED_QUEUE = "notification.invoice.generated.queue";
    private static final String REPORTING_INVOICE_GENERATED_QUEUE    = "reporting.invoice.generated.queue";

    // ── RecallInitiated ───────────────────────────────────────────────────────
    private static final String NOTIFICATION_RECALL_INITIATED_QUEUE  = "notification.recall.initiated.queue";
    private static final String REPORTING_RECALL_INITIATED_QUEUE     = "reporting.recall.initiated.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // DonationCompletedEvent consumers
        declareAndBindQueue(admin, exchange, LAB_DONATION_COMPLETED_QUEUE,          EventConstants.DONATION_COMPLETED);
        declareAndBindQueue(admin, exchange, INVENTORY_DONATION_COMPLETED_QUEUE,    EventConstants.DONATION_COMPLETED);
        declareAndBindQueue(admin, exchange, NOTIFICATION_DONATION_COMPLETED_QUEUE, EventConstants.DONATION_COMPLETED);
        declareAndBindQueue(admin, exchange, REPORTING_DONATION_COMPLETED_QUEUE,    EventConstants.DONATION_COMPLETED);

        // CampCompletedEvent consumers
        declareAndBindQueue(admin, exchange, NOTIFICATION_CAMP_COMPLETED_QUEUE, EventConstants.CAMP_COMPLETED);
        declareAndBindQueue(admin, exchange, REPORTING_CAMP_COMPLETED_QUEUE,    EventConstants.CAMP_COMPLETED);

        // TestResultAvailableEvent consumers
        declareAndBindQueue(admin, exchange, INVENTORY_TEST_RESULT_QUEUE,    EventConstants.TEST_RESULT_AVAILABLE);
        declareAndBindQueue(admin, exchange, NOTIFICATION_TEST_RESULT_QUEUE, EventConstants.TEST_RESULT_AVAILABLE);
        declareAndBindQueue(admin, exchange, REPORTING_TEST_RESULT_QUEUE,    EventConstants.TEST_RESULT_AVAILABLE);

        // UnitReleasedEvent consumers
        declareAndBindQueue(admin, exchange, INVENTORY_UNIT_RELEASED_QUEUE,    EventConstants.UNIT_RELEASED);
        declareAndBindQueue(admin, exchange, NOTIFICATION_UNIT_RELEASED_QUEUE, EventConstants.UNIT_RELEASED);
        declareAndBindQueue(admin, exchange, REPORTING_UNIT_RELEASED_QUEUE,    EventConstants.UNIT_RELEASED);

        // BloodStockUpdatedEvent consumers
        declareAndBindQueue(admin, exchange, REQUEST_MATCHING_STOCK_UPDATED_QUEUE, EventConstants.BLOOD_STOCK_UPDATED);
        declareAndBindQueue(admin, exchange, NOTIFICATION_STOCK_UPDATED_QUEUE,     EventConstants.BLOOD_STOCK_UPDATED);
        declareAndBindQueue(admin, exchange, REPORTING_STOCK_UPDATED_QUEUE,        EventConstants.BLOOD_STOCK_UPDATED);

        // StockCriticalEvent consumers
        declareAndBindQueue(admin, exchange, NOTIFICATION_STOCK_CRITICAL_QUEUE,     EventConstants.STOCK_CRITICAL);
        declareAndBindQueue(admin, exchange, REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, EventConstants.STOCK_CRITICAL);

        // UnitExpiringEvent consumers
        declareAndBindQueue(admin, exchange, NOTIFICATION_UNIT_EXPIRING_QUEUE, EventConstants.UNIT_EXPIRING);
        declareAndBindQueue(admin, exchange, REPORTING_UNIT_EXPIRING_QUEUE,    EventConstants.UNIT_EXPIRING);

        // BloodRequestCreatedEvent consumers
        declareAndBindQueue(admin, exchange, REQUEST_MATCHING_REQUEST_CREATED_QUEUE, EventConstants.BLOOD_REQUEST_CREATED);
        declareAndBindQueue(admin, exchange, NOTIFICATION_REQUEST_CREATED_QUEUE,     EventConstants.BLOOD_REQUEST_CREATED);
        declareAndBindQueue(admin, exchange, REPORTING_REQUEST_CREATED_QUEUE,        EventConstants.BLOOD_REQUEST_CREATED);

        // BloodRequestMatchedEvent consumers
        declareAndBindQueue(admin, exchange, BILLING_REQUEST_MATCHED_QUEUE,      EventConstants.BLOOD_REQUEST_MATCHED);
        declareAndBindQueue(admin, exchange, NOTIFICATION_REQUEST_MATCHED_QUEUE, EventConstants.BLOOD_REQUEST_MATCHED);
        declareAndBindQueue(admin, exchange, REPORTING_REQUEST_MATCHED_QUEUE,    EventConstants.BLOOD_REQUEST_MATCHED);

        // EmergencyRequestEvent consumers
        declareAndBindQueue(admin, exchange, NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EventConstants.EMERGENCY_REQUEST);
        declareAndBindQueue(admin, exchange, DONOR_EMERGENCY_REQUEST_QUEUE,        EventConstants.EMERGENCY_REQUEST);

        // TransfusionCompletedEvent consumers
        declareAndBindQueue(admin, exchange, NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, EventConstants.TRANSFUSION_COMPLETED);
        declareAndBindQueue(admin, exchange, REPORTING_TRANSFUSION_COMPLETED_QUEUE,    EventConstants.TRANSFUSION_COMPLETED);

        // TransfusionReactionEvent consumers
        declareAndBindQueue(admin, exchange, NOTIFICATION_TRANSFUSION_REACTION_QUEUE, EventConstants.TRANSFUSION_REACTION);
        declareAndBindQueue(admin, exchange, REPORTING_TRANSFUSION_REACTION_QUEUE,    EventConstants.TRANSFUSION_REACTION);

        // InvoiceGeneratedEvent consumers
        declareAndBindQueue(admin, exchange, NOTIFICATION_INVOICE_GENERATED_QUEUE, EventConstants.INVOICE_GENERATED);
        declareAndBindQueue(admin, exchange, REPORTING_INVOICE_GENERATED_QUEUE,    EventConstants.INVOICE_GENERATED);

        // RecallInitiatedEvent consumers
        declareAndBindQueue(admin, exchange, NOTIFICATION_RECALL_INITIATED_QUEUE, EventConstants.RECALL_INITIATED);
        declareAndBindQueue(admin, exchange, REPORTING_RECALL_INITIATED_QUEUE,    EventConstants.RECALL_INITIATED);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 1. DonationCompletedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("1. DonationCompletedEvent: donor-service → lab, inventory, notification, reporting")
    class DonationCompletedEventRouting {

        @Test
        @DisplayName("DonationCompletedEvent should fan-out to lab-service and inventory-service")
        void shouldReachLabAndInventoryService() {
            UUID donationId = UUID.randomUUID();
            UUID donorId    = UUID.randomUUID();
            UUID branchId   = UUID.randomUUID();

            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId, donorId, branchId, Instant.now()));

            DonationCompletedEvent labReceived =
                    assertEventReceived(LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent inventoryReceived =
                    assertEventReceived(INVENTORY_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            // drain remaining queues
            assertEventReceived(NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            assertEventReceived(REPORTING_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(labReceived.donationId()).isEqualTo(donationId);
            assertThat(inventoryReceived.donationId()).isEqualTo(donationId);
        }

        @Test
        @DisplayName("DonationCompletedEvent should fan-out to notification-service and reporting-service")
        void shouldReachNotificationAndReportingService() {
            UUID donationId = UUID.randomUUID();
            UUID donorId    = UUID.randomUUID();
            UUID branchId   = UUID.randomUUID();
            Instant now     = Instant.now();

            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId, donorId, branchId, now));

            // drain lab and inventory
            assertEventReceived(LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            assertEventReceived(INVENTORY_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            DonationCompletedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent reportingReceived =
                    assertEventReceived(REPORTING_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(notificationReceived.donorId()).isEqualTo(donorId);
            assertThat(reportingReceived.occurredAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("DonationCompletedEvent should fan-out to all four consumer queues")
        void shouldFanOutToAllFourQueues() {
            UUID donationId = UUID.randomUUID();

            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId, UUID.randomUUID(), UUID.randomUUID(), Instant.now()));

            assertThat(assertEventReceived(LAB_DONATION_COMPLETED_QUEUE,          DonationCompletedEvent.class).donationId()).isEqualTo(donationId);
            assertThat(assertEventReceived(INVENTORY_DONATION_COMPLETED_QUEUE,    DonationCompletedEvent.class).donationId()).isEqualTo(donationId);
            assertThat(assertEventReceived(NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class).donationId()).isEqualTo(donationId);
            assertThat(assertEventReceived(REPORTING_DONATION_COMPLETED_QUEUE,    DonationCompletedEvent.class).donationId()).isEqualTo(donationId);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. CampCompletedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("2. CampCompletedEvent: donor-service → notification, reporting")
    class CampCompletedEventRouting {

        @Test
        @DisplayName("CampCompletedEvent should fan-out to notification-service and reporting-service")
        void shouldFanOutToNotificationAndReporting() {
            UUID campId   = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            publishEvent(EventConstants.CAMP_COMPLETED,
                    new CampCompletedEvent(campId, branchId, 42, Instant.now()));

            CampCompletedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);
            CampCompletedEvent reportingReceived =
                    assertEventReceived(REPORTING_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);

            assertThat(notificationReceived.campId()).isEqualTo(campId);
            assertThat(reportingReceived.campId()).isEqualTo(campId);
            assertThat(reportingReceived.totalCollections()).isEqualTo(42);
        }

        @Test
        @DisplayName("CampCompletedEvent should preserve totalCollections for reporting metrics")
        void shouldPreserveTotalCollectionsForReporting() {
            UUID campId   = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now   = Instant.now();

            publishEvent(EventConstants.CAMP_COMPLETED,
                    new CampCompletedEvent(campId, branchId, 127, now));

            assertEventReceived(NOTIFICATION_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);
            CampCompletedEvent reportingReceived =
                    assertEventReceived(REPORTING_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);

            assertThat(reportingReceived.campId()).isEqualTo(campId);
            assertThat(reportingReceived.branchId()).isEqualTo(branchId);
            assertThat(reportingReceived.totalCollections()).isEqualTo(127);
            assertThat(reportingReceived.occurredAt()).isEqualTo(now);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. TestResultAvailableEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("3. TestResultAvailableEvent: lab-service → inventory, notification, reporting")
    class TestResultAvailableEventRouting {

        @Test
        @DisplayName("TestResultAvailableEvent should fan-out to inventory, notification, and reporting")
        void shouldFanOutToInventoryNotificationAndReporting() {
            UUID testOrderId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId    = UUID.randomUUID();

            publishEvent(EventConstants.TEST_RESULT_AVAILABLE,
                    new TestResultAvailableEvent(testOrderId, bloodUnitId, branchId, Instant.now()));

            TestResultAvailableEvent inventoryReceived =
                    assertEventReceived(INVENTORY_TEST_RESULT_QUEUE, TestResultAvailableEvent.class);
            TestResultAvailableEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_TEST_RESULT_QUEUE, TestResultAvailableEvent.class);
            TestResultAvailableEvent reportingReceived =
                    assertEventReceived(REPORTING_TEST_RESULT_QUEUE, TestResultAvailableEvent.class);

            assertThat(inventoryReceived.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(notificationReceived.testOrderId()).isEqualTo(testOrderId);
            assertThat(reportingReceived.branchId()).isEqualTo(branchId);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. UnitReleasedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("4. UnitReleasedEvent: lab-service/inventory → inventory, notification, reporting")
    class UnitReleasedEventRouting {

        @Test
        @DisplayName("UnitReleasedEvent should fan-out to inventory, notification, and reporting")
        void shouldFanOutToInventoryNotificationAndReporting() {
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId    = UUID.randomUUID();
            Instant now      = Instant.now();

            publishEvent(EventConstants.UNIT_RELEASED,
                    new UnitReleasedEvent(bloodUnitId, branchId, now));

            UnitReleasedEvent inventoryReceived =
                    assertEventReceived(INVENTORY_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);
            UnitReleasedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);
            UnitReleasedEvent reportingReceived =
                    assertEventReceived(REPORTING_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class);

            assertThat(inventoryReceived.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(notificationReceived.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(reportingReceived.occurredAt()).isEqualTo(now);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 5. BloodStockUpdatedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("5. BloodStockUpdatedEvent: inventory-service → request-matching, notification, reporting")
    class BloodStockUpdatedEventRouting {

        @Test
        @DisplayName("BloodStockUpdatedEvent should fan-out to request-matching, notification, and reporting")
        void shouldFanOutToRequestMatchingNotificationAndReporting() {
            UUID branchId = UUID.randomUUID();

            publishEvent(EventConstants.BLOOD_STOCK_UPDATED,
                    new BloodStockUpdatedEvent(branchId, "O_NEGATIVE", "PACKED_RBC", 5, Instant.now()));

            BloodStockUpdatedEvent matchingReceived =
                    assertEventReceived(REQUEST_MATCHING_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            BloodStockUpdatedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            BloodStockUpdatedEvent reportingReceived =
                    assertEventReceived(REPORTING_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);

            assertThat(matchingReceived.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(notificationReceived.quantity()).isEqualTo(5);
            assertThat(reportingReceived.componentType()).isEqualTo("PACKED_RBC");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 6. StockCriticalEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("6. StockCriticalEvent: inventory-service → notification, request-matching")
    class StockCriticalEventRouting {

        @Test
        @DisplayName("StockCriticalEvent should reach notification-service for critical alert")
        void shouldReachNotificationService() {
            UUID branchId = UUID.randomUUID();

            publishEvent(EventConstants.STOCK_CRITICAL,
                    new StockCriticalEvent(branchId, "AB_NEGATIVE", "FRESH_FROZEN_PLASMA", 2, 10, Instant.now()));

            StockCriticalEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);
            StockCriticalEvent matchingReceived =
                    assertEventReceived(REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            assertThat(notificationReceived.bloodGroup()).isEqualTo("AB_NEGATIVE");
            assertThat(notificationReceived.currentStock()).isEqualTo(2);
            assertThat(notificationReceived.minimumStock()).isEqualTo(10);
            assertThat(matchingReceived.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("StockCriticalEvent should preserve all critical stock fields")
        void shouldPreserveAllCriticalStockFields() {
            UUID branchId = UUID.randomUUID();
            Instant now   = Instant.now();

            publishEvent(EventConstants.STOCK_CRITICAL,
                    new StockCriticalEvent(branchId, "O_NEGATIVE", "WHOLE_BLOOD", 0, 15, now));

            StockCriticalEvent received =
                    assertEventReceived(NOTIFICATION_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);
            assertEventReceived(REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(received.componentType()).isEqualTo("WHOLE_BLOOD");
            assertThat(received.currentStock()).isEqualTo(0);
            assertThat(received.minimumStock()).isEqualTo(15);
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 7. UnitExpiringEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("7. UnitExpiringEvent: inventory-service → notification, reporting")
    class UnitExpiringEventRouting {

        @Test
        @DisplayName("UnitExpiringEvent should reach notification-service for expiry alert")
        void shouldReachNotificationService() {
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId    = UUID.randomUUID();
            LocalDate expiryDate = LocalDate.now().plusDays(3);

            publishEvent(EventConstants.UNIT_EXPIRING,
                    new UnitExpiringEvent(bloodUnitId, branchId, expiryDate, Instant.now()));

            UnitExpiringEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);
            UnitExpiringEvent reportingReceived =
                    assertEventReceived(REPORTING_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);

            assertThat(notificationReceived.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(notificationReceived.expiryDate()).isEqualTo(expiryDate);
            assertThat(reportingReceived.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("UnitExpiringEvent should fan-out to notification and reporting with full fields")
        void shouldPreserveExpiryDateAcrossQueues() {
            UUID bloodUnitId = UUID.randomUUID();
            UUID branchId    = UUID.randomUUID();
            LocalDate expiryDate = LocalDate.now().plusDays(1);
            Instant now          = Instant.now();

            publishEvent(EventConstants.UNIT_EXPIRING,
                    new UnitExpiringEvent(bloodUnitId, branchId, expiryDate, now));

            UnitExpiringEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);
            UnitExpiringEvent reportingReceived =
                    assertEventReceived(REPORTING_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);

            assertThat(notificationReceived.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(notificationReceived.expiryDate()).isEqualTo(expiryDate);
            assertThat(notificationReceived.occurredAt()).isEqualTo(now);
            assertThat(reportingReceived.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(reportingReceived.expiryDate()).isEqualTo(expiryDate);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 8. BloodRequestCreatedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("8. BloodRequestCreatedEvent: hospital-service → request-matching, notification, reporting")
    class BloodRequestCreatedEventRouting {

        @Test
        @DisplayName("BloodRequestCreatedEvent should fan-out to request-matching, notification, and reporting")
        void shouldFanOutToAllConsumers() {
            UUID requestId  = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId   = UUID.randomUUID();

            publishEvent(EventConstants.BLOOD_REQUEST_CREATED,
                    new BloodRequestCreatedEvent(requestId, hospitalId, branchId, "B_POSITIVE", 4, Instant.now()));

            BloodRequestCreatedEvent matchingReceived =
                    assertEventReceived(REQUEST_MATCHING_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);
            BloodRequestCreatedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);
            BloodRequestCreatedEvent reportingReceived =
                    assertEventReceived(REPORTING_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);

            assertThat(matchingReceived.requestId()).isEqualTo(requestId);
            assertThat(matchingReceived.bloodGroup()).isEqualTo("B_POSITIVE");
            assertThat(matchingReceived.quantity()).isEqualTo(4);
            assertThat(notificationReceived.hospitalId()).isEqualTo(hospitalId);
            assertThat(reportingReceived.branchId()).isEqualTo(branchId);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 9. BloodRequestMatchedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("9. BloodRequestMatchedEvent: request-matching → billing, notification, reporting")
    class BloodRequestMatchedEventRouting {

        @Test
        @DisplayName("BloodRequestMatchedEvent should fan-out to billing, notification, and reporting")
        void shouldFanOutToBillingNotificationAndReporting() {
            UUID requestId = UUID.randomUUID();
            UUID branchId  = UUID.randomUUID();
            List<UUID> matchedUnitIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED,
                    new BloodRequestMatchedEvent(requestId, branchId, matchedUnitIds, Instant.now()));

            BloodRequestMatchedEvent billingReceived =
                    assertEventReceived(BILLING_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            BloodRequestMatchedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            BloodRequestMatchedEvent reportingReceived =
                    assertEventReceived(REPORTING_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);

            assertThat(billingReceived.requestId()).isEqualTo(requestId);
            assertThat(billingReceived.matchedUnitIds()).hasSize(2);
            assertThat(notificationReceived.requestId()).isEqualTo(requestId);
            assertThat(reportingReceived.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("BloodRequestMatchedEvent should preserve matchedUnitIds list")
        void shouldPreserveMatchedUnitIds() {
            UUID unit1 = UUID.randomUUID();
            UUID unit2 = UUID.randomUUID();
            UUID unit3 = UUID.randomUUID();

            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED,
                    new BloodRequestMatchedEvent(UUID.randomUUID(), UUID.randomUUID(),
                            List.of(unit1, unit2, unit3), Instant.now()));

            BloodRequestMatchedEvent billingReceived =
                    assertEventReceived(BILLING_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            assertEventReceived(NOTIFICATION_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            assertEventReceived(REPORTING_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);

            assertThat(billingReceived.matchedUnitIds())
                    .hasSize(3)
                    .containsExactlyInAnyOrder(unit1, unit2, unit3);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 10. EmergencyRequestEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("10. EmergencyRequestEvent: request-matching → notification, donor-service")
    class EmergencyRequestEventRouting {

        @Test
        @DisplayName("EmergencyRequestEvent should fan-out to notification-service and donor-service")
        void shouldFanOutToNotificationAndDonorService() {
            UUID requestId = UUID.randomUUID();
            UUID branchId  = UUID.randomUUID();

            publishEvent(EventConstants.EMERGENCY_REQUEST,
                    new EmergencyRequestEvent(requestId, branchId, "O_NEGATIVE", "CRITICAL", Instant.now()));

            EmergencyRequestEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);
            EmergencyRequestEvent donorReceived =
                    assertEventReceived(DONOR_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

            assertThat(notificationReceived.requestId()).isEqualTo(requestId);
            assertThat(notificationReceived.bloodGroup()).isEqualTo("O_NEGATIVE");
            assertThat(notificationReceived.severity()).isEqualTo("CRITICAL");
            assertThat(donorReceived.requestId()).isEqualTo(requestId);
            assertThat(donorReceived.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("EmergencyRequestEvent should reach donor-service for donor mobilization")
        void shouldReachDonorServiceForMobilization() {
            UUID requestId = UUID.randomUUID();
            UUID branchId  = UUID.randomUUID();
            Instant now    = Instant.now();

            publishEvent(EventConstants.EMERGENCY_REQUEST,
                    new EmergencyRequestEvent(requestId, branchId, "AB_NEGATIVE", "LIFE_THREATENING", now));

            assertEventReceived(NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);
            EmergencyRequestEvent donorReceived =
                    assertEventReceived(DONOR_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);

            assertThat(donorReceived.requestId()).isEqualTo(requestId);
            assertThat(donorReceived.severity()).isEqualTo("LIFE_THREATENING");
            assertThat(donorReceived.occurredAt()).isEqualTo(now);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 11. TransfusionCompletedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("11. TransfusionCompletedEvent: transfusion-service → notification, reporting")
    class TransfusionCompletedEventRouting {

        @Test
        @DisplayName("TransfusionCompletedEvent should fan-out to notification-service and reporting-service")
        void shouldFanOutToNotificationAndReporting() {
            UUID transfusionId = UUID.randomUUID();
            UUID bloodUnitId   = UUID.randomUUID();
            UUID branchId      = UUID.randomUUID();
            Instant now        = Instant.now();

            publishEvent(EventConstants.TRANSFUSION_COMPLETED,
                    new TransfusionCompletedEvent(transfusionId, bloodUnitId, branchId, now));

            TransfusionCompletedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);
            TransfusionCompletedEvent reportingReceived =
                    assertEventReceived(REPORTING_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);

            assertThat(notificationReceived.transfusionId()).isEqualTo(transfusionId);
            assertThat(notificationReceived.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(reportingReceived.transfusionId()).isEqualTo(transfusionId);
            assertThat(reportingReceived.occurredAt()).isEqualTo(now);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 12. TransfusionReactionEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("12. TransfusionReactionEvent: transfusion-service → notification, reporting")
    class TransfusionReactionEventRouting {

        @Test
        @DisplayName("TransfusionReactionEvent should fan-out to notification-service and reporting-service")
        void shouldFanOutToNotificationAndReporting() {
            UUID transfusionId = UUID.randomUUID();
            UUID bloodUnitId   = UUID.randomUUID();
            UUID branchId      = UUID.randomUUID();

            publishEvent(EventConstants.TRANSFUSION_REACTION,
                    new TransfusionReactionEvent(transfusionId, bloodUnitId, branchId, "SEVERE", Instant.now()));

            TransfusionReactionEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);
            TransfusionReactionEvent reportingReceived =
                    assertEventReceived(REPORTING_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);

            assertThat(notificationReceived.transfusionId()).isEqualTo(transfusionId);
            assertThat(notificationReceived.severity()).isEqualTo("SEVERE");
            assertThat(reportingReceived.bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(reportingReceived.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("TransfusionReactionEvent severity should be preserved for hemovigilance reporting")
        void shouldPreserveSeverityForHemovigilance() {
            String[] severities = {"MILD", "MODERATE", "SEVERE", "LIFE_THREATENING"};

            for (String severity : severities) {
                UUID transfusionId = UUID.randomUUID();

                publishEvent(EventConstants.TRANSFUSION_REACTION,
                        new TransfusionReactionEvent(
                                transfusionId, UUID.randomUUID(), UUID.randomUUID(), severity, Instant.now()));

                TransfusionReactionEvent notificationReceived =
                        assertEventReceived(NOTIFICATION_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);
                TransfusionReactionEvent reportingReceived =
                        assertEventReceived(REPORTING_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);

                assertThat(notificationReceived.severity()).isEqualTo(severity);
                assertThat(notificationReceived.transfusionId()).isEqualTo(transfusionId);
                assertThat(reportingReceived.severity()).isEqualTo(severity);
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 13. InvoiceGeneratedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("13. InvoiceGeneratedEvent: billing-service → notification, reporting")
    class InvoiceGeneratedEventRouting {

        @Test
        @DisplayName("InvoiceGeneratedEvent should fan-out to notification-service and reporting-service")
        void shouldFanOutToNotificationAndReporting() {
            UUID invoiceId  = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId   = UUID.randomUUID();
            Instant now     = Instant.now();

            publishEvent(EventConstants.INVOICE_GENERATED,
                    new InvoiceGeneratedEvent(invoiceId, hospitalId, branchId, now));

            InvoiceGeneratedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            InvoiceGeneratedEvent reportingReceived =
                    assertEventReceived(REPORTING_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(notificationReceived.invoiceId()).isEqualTo(invoiceId);
            assertThat(notificationReceived.hospitalId()).isEqualTo(hospitalId);
            assertThat(reportingReceived.invoiceId()).isEqualTo(invoiceId);
            assertThat(reportingReceived.occurredAt()).isEqualTo(now);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 14. RecallInitiatedEvent
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("14. RecallInitiatedEvent: compliance-service → notification, reporting")
    class RecallInitiatedEventRouting {

        @Test
        @DisplayName("RecallInitiatedEvent should fan-out to notification-service and reporting-service")
        void shouldFanOutToNotificationAndReporting() {
            UUID recallId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            List<UUID> affectedUnitIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            publishEvent(EventConstants.RECALL_INITIATED,
                    new RecallInitiatedEvent(recallId, branchId, "CONTAMINATION_SUSPECTED", affectedUnitIds, Instant.now()));

            RecallInitiatedEvent notificationReceived =
                    assertEventReceived(NOTIFICATION_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
            RecallInitiatedEvent reportingReceived =
                    assertEventReceived(REPORTING_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);

            assertThat(notificationReceived.recallId()).isEqualTo(recallId);
            assertThat(notificationReceived.reason()).isEqualTo("CONTAMINATION_SUSPECTED");
            assertThat(notificationReceived.affectedUnitIds()).hasSize(3);
            assertThat(reportingReceived.recallId()).isEqualTo(recallId);
            assertThat(reportingReceived.affectedUnitIds()).containsExactlyElementsOf(affectedUnitIds);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Full cross-service event chain
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Complete cross-service event chain: all 14 events in sequence")
    class CompleteCrossServiceEventChain {

        @Test
        @DisplayName("All 14 event types should be routable without interference")
        void allEventTypesShouldBeRoutableWithoutInterference() {
            UUID branchId = UUID.randomUUID();
            Instant now   = Instant.now();

            // 1. DonationCompleted
            UUID donationId = UUID.randomUUID();
            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId, UUID.randomUUID(), branchId, now));
            assertThat(assertEventReceived(LAB_DONATION_COMPLETED_QUEUE,          DonationCompletedEvent.class).donationId()).isEqualTo(donationId);
            assertThat(assertEventReceived(INVENTORY_DONATION_COMPLETED_QUEUE,    DonationCompletedEvent.class).donationId()).isEqualTo(donationId);
            assertThat(assertEventReceived(NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class).donationId()).isEqualTo(donationId);
            assertThat(assertEventReceived(REPORTING_DONATION_COMPLETED_QUEUE,    DonationCompletedEvent.class).donationId()).isEqualTo(donationId);

            // 2. CampCompleted
            UUID campId = UUID.randomUUID();
            publishEvent(EventConstants.CAMP_COMPLETED,
                    new CampCompletedEvent(campId, branchId, 30, now));
            assertThat(assertEventReceived(NOTIFICATION_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class).campId()).isEqualTo(campId);
            assertThat(assertEventReceived(REPORTING_CAMP_COMPLETED_QUEUE,    CampCompletedEvent.class).campId()).isEqualTo(campId);

            // 3. TestResultAvailable
            UUID testOrderId = UUID.randomUUID();
            UUID bloodUnitId = UUID.randomUUID();
            publishEvent(EventConstants.TEST_RESULT_AVAILABLE,
                    new TestResultAvailableEvent(testOrderId, bloodUnitId, branchId, now));
            assertThat(assertEventReceived(INVENTORY_TEST_RESULT_QUEUE,    TestResultAvailableEvent.class).bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(assertEventReceived(NOTIFICATION_TEST_RESULT_QUEUE, TestResultAvailableEvent.class).bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(assertEventReceived(REPORTING_TEST_RESULT_QUEUE,    TestResultAvailableEvent.class).bloodUnitId()).isEqualTo(bloodUnitId);

            // 4. UnitReleased
            publishEvent(EventConstants.UNIT_RELEASED,
                    new UnitReleasedEvent(bloodUnitId, branchId, now));
            assertThat(assertEventReceived(INVENTORY_UNIT_RELEASED_QUEUE,    UnitReleasedEvent.class).bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(assertEventReceived(NOTIFICATION_UNIT_RELEASED_QUEUE, UnitReleasedEvent.class).bloodUnitId()).isEqualTo(bloodUnitId);
            assertThat(assertEventReceived(REPORTING_UNIT_RELEASED_QUEUE,    UnitReleasedEvent.class).bloodUnitId()).isEqualTo(bloodUnitId);

            // 5. BloodStockUpdated
            publishEvent(EventConstants.BLOOD_STOCK_UPDATED,
                    new BloodStockUpdatedEvent(branchId, "A_POSITIVE", "WHOLE_BLOOD", 20, now));
            assertEventReceived(REQUEST_MATCHING_STOCK_UPDATED_QUEUE, BloodStockUpdatedEvent.class);
            assertEventReceived(NOTIFICATION_STOCK_UPDATED_QUEUE,     BloodStockUpdatedEvent.class);
            assertEventReceived(REPORTING_STOCK_UPDATED_QUEUE,        BloodStockUpdatedEvent.class);

            // 6. StockCritical
            publishEvent(EventConstants.STOCK_CRITICAL,
                    new StockCriticalEvent(branchId, "O_NEGATIVE", "PACKED_RBC", 1, 10, now));
            assertEventReceived(NOTIFICATION_STOCK_CRITICAL_QUEUE,     StockCriticalEvent.class);
            assertEventReceived(REQUEST_MATCHING_STOCK_CRITICAL_QUEUE, StockCriticalEvent.class);

            // 7. UnitExpiring
            publishEvent(EventConstants.UNIT_EXPIRING,
                    new UnitExpiringEvent(bloodUnitId, branchId, LocalDate.now().plusDays(2), now));
            assertEventReceived(NOTIFICATION_UNIT_EXPIRING_QUEUE, UnitExpiringEvent.class);
            assertEventReceived(REPORTING_UNIT_EXPIRING_QUEUE,    UnitExpiringEvent.class);

            // 8. BloodRequestCreated
            UUID requestId  = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            publishEvent(EventConstants.BLOOD_REQUEST_CREATED,
                    new BloodRequestCreatedEvent(requestId, hospitalId, branchId, "A_POSITIVE", 2, now));
            assertEventReceived(REQUEST_MATCHING_REQUEST_CREATED_QUEUE, BloodRequestCreatedEvent.class);
            assertEventReceived(NOTIFICATION_REQUEST_CREATED_QUEUE,     BloodRequestCreatedEvent.class);
            assertEventReceived(REPORTING_REQUEST_CREATED_QUEUE,        BloodRequestCreatedEvent.class);

            // 9. BloodRequestMatched
            List<UUID> matchedUnits = List.of(bloodUnitId);
            publishEvent(EventConstants.BLOOD_REQUEST_MATCHED,
                    new BloodRequestMatchedEvent(requestId, branchId, matchedUnits, now));
            assertEventReceived(BILLING_REQUEST_MATCHED_QUEUE,      BloodRequestMatchedEvent.class);
            assertEventReceived(NOTIFICATION_REQUEST_MATCHED_QUEUE, BloodRequestMatchedEvent.class);
            assertEventReceived(REPORTING_REQUEST_MATCHED_QUEUE,    BloodRequestMatchedEvent.class);

            // 10. EmergencyRequest
            UUID emergencyRequestId = UUID.randomUUID();
            publishEvent(EventConstants.EMERGENCY_REQUEST,
                    new EmergencyRequestEvent(emergencyRequestId, branchId, "O_NEGATIVE", "CRITICAL", now));
            assertEventReceived(NOTIFICATION_EMERGENCY_REQUEST_QUEUE, EmergencyRequestEvent.class);
            assertEventReceived(DONOR_EMERGENCY_REQUEST_QUEUE,        EmergencyRequestEvent.class);

            // 11. TransfusionCompleted
            UUID transfusionId = UUID.randomUUID();
            publishEvent(EventConstants.TRANSFUSION_COMPLETED,
                    new TransfusionCompletedEvent(transfusionId, bloodUnitId, branchId, now));
            assertEventReceived(NOTIFICATION_TRANSFUSION_COMPLETED_QUEUE, TransfusionCompletedEvent.class);
            assertEventReceived(REPORTING_TRANSFUSION_COMPLETED_QUEUE,    TransfusionCompletedEvent.class);

            // 12. TransfusionReaction
            publishEvent(EventConstants.TRANSFUSION_REACTION,
                    new TransfusionReactionEvent(transfusionId, bloodUnitId, branchId, "MODERATE", now));
            assertEventReceived(NOTIFICATION_TRANSFUSION_REACTION_QUEUE, TransfusionReactionEvent.class);
            assertEventReceived(REPORTING_TRANSFUSION_REACTION_QUEUE,    TransfusionReactionEvent.class);

            // 13. InvoiceGenerated
            UUID invoiceId = UUID.randomUUID();
            publishEvent(EventConstants.INVOICE_GENERATED,
                    new InvoiceGeneratedEvent(invoiceId, hospitalId, branchId, now));
            assertEventReceived(NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            assertEventReceived(REPORTING_INVOICE_GENERATED_QUEUE,    InvoiceGeneratedEvent.class);

            // 14. RecallInitiated
            UUID recallId = UUID.randomUUID();
            publishEvent(EventConstants.RECALL_INITIATED,
                    new RecallInitiatedEvent(recallId, branchId, "LOOKBACK_TRIGGERED",
                            List.of(bloodUnitId), now));
            assertEventReceived(NOTIFICATION_RECALL_INITIATED_QUEUE, RecallInitiatedEvent.class);
            assertEventReceived(REPORTING_RECALL_INITIATED_QUEUE,    RecallInitiatedEvent.class);
        }
    }
}
