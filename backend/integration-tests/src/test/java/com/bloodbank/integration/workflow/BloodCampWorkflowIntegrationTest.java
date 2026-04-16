package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.CampCompletedEvent;
import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.events.EventConstants;
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
 * M6-003: Camp Planning → Registration → Collection → Post-Camp Follow-Up
 *
 * Tests the blood camp lifecycle from camp completion through post-camp donation
 * processing and donor follow-up notifications.
 *
 * Event flow verified:
 *   donor-service --[CampCompletedEvent]-----------> notification-service (camp summary)
 *   donor-service --[CampCompletedEvent]-----------> reporting-service (camp report)
 *   donor-service --[DonationCompletedEvent]-------> lab-service (per-donation lab order)
 *   donor-service --[DonationCompletedEvent]-------> notification-service (donor follow-up)
 *
 * Verifications:
 * - CampCompletedEvent reaches notification for camp summary / coordinator report
 * - CampCompletedEvent reaches reporting-service for metrics
 * - Each DonationCompletedEvent from camp triggers individual lab test orders
 * - DonationCompletedEvent fans out to notification for post-camp donor follow-up
 */
@DisplayName("M6-003: Camp Planning → Registration → Collection → Post-Camp Follow-Up")
class BloodCampWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String NOTIFICATION_CAMP_COMPLETED_QUEUE =
            "notification.camp.completed.queue";
    private static final String REPORTING_CAMP_COMPLETED_QUEUE =
            "reporting.camp.completed.queue";
    private static final String LAB_DONATION_COMPLETED_QUEUE =
            "lab.donation.completed.queue";
    private static final String NOTIFICATION_DONATION_COMPLETED_QUEUE =
            "notification.donation.completed.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // notification-service listens for camp.completed (coordinator summary + donor follow-up trigger)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_CAMP_COMPLETED_QUEUE, EventConstants.CAMP_COMPLETED);

        // reporting-service listens for camp.completed (camp metrics and reports)
        declareAndBindQueue(admin, exchange,
                REPORTING_CAMP_COMPLETED_QUEUE, EventConstants.CAMP_COMPLETED);

        // lab-service listens for donation.completed (to create test orders for camp donations)
        declareAndBindQueue(admin, exchange,
                LAB_DONATION_COMPLETED_QUEUE, EventConstants.DONATION_COMPLETED);

        // notification-service listens for donation.completed (thank-you / post-camp follow-up)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_DONATION_COMPLETED_QUEUE, EventConstants.DONATION_COMPLETED);
    }

    @Nested
    @DisplayName("Step 1: Camp completed triggers summary notifications and reports")
    class CampCompletedTriggersSummary {

        @Test
        @DisplayName("CampCompletedEvent should reach notification-service for camp summary")
        void campCompletedEventShouldReachNotificationService() {
            UUID campId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            CampCompletedEvent event = new CampCompletedEvent(campId, branchId, 47, Instant.now());

            publishEvent(EventConstants.CAMP_COMPLETED, event);

            CampCompletedEvent received = assertEventReceived(
                    NOTIFICATION_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);

            assertThat(received.campId()).isEqualTo(campId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.totalCollections()).isEqualTo(47);
        }

        @Test
        @DisplayName("CampCompletedEvent should fan-out to notification and reporting services")
        void campCompletedEventShouldFanOutToNotificationAndReporting() {
            UUID campId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            CampCompletedEvent event = new CampCompletedEvent(campId, branchId, 30, Instant.now());

            publishEvent(EventConstants.CAMP_COMPLETED, event);

            CampCompletedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);
            CampCompletedEvent reportingReceived = assertEventReceived(
                    REPORTING_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);

            assertThat(notificationReceived.campId()).isEqualTo(campId);
            assertThat(reportingReceived.campId()).isEqualTo(campId);
        }

        @Test
        @DisplayName("CampCompletedEvent should preserve total collections count for reporting")
        void campCompletedEventShouldPreserveTotalCollectionsForReporting() {
            UUID campId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            CampCompletedEvent event = new CampCompletedEvent(campId, branchId, 120, now);

            publishEvent(EventConstants.CAMP_COMPLETED, event);

            CampCompletedEvent reportingReceived = assertEventReceived(
                    REPORTING_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);

            assertThat(reportingReceived.totalCollections()).isEqualTo(120);
            assertThat(reportingReceived.occurredAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Camp with zero collections should still route event correctly")
        void campWithZeroCollectionsShouldStillRouteEventCorrectly() {
            UUID campId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            CampCompletedEvent event = new CampCompletedEvent(campId, branchId, 0, Instant.now());

            publishEvent(EventConstants.CAMP_COMPLETED, event);

            CampCompletedEvent received = assertEventReceived(
                    NOTIFICATION_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);

            assertThat(received.campId()).isEqualTo(campId);
            assertThat(received.totalCollections()).isZero();
        }
    }

    @Nested
    @DisplayName("Step 2: Individual camp donations trigger lab testing and donor follow-up")
    class CampDonationsTriggerLabAndFollowUp {

        @Test
        @DisplayName("DonationCompletedEvent from camp should reach lab-service for test order")
        void campDonationCompletedEventShouldReachLabService() {
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
        @DisplayName("DonationCompletedEvent from camp should fan-out to lab and notification for follow-up")
        void campDonationCompletedEventShouldFanOutToLabAndNotification() {
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

            assertThat(labReceived.donorId()).isEqualTo(donorId);
            assertThat(notificationReceived.donorId()).isEqualTo(donorId);
        }

        @Test
        @DisplayName("Multiple camp donations should each produce independent lab test events")
        void multipleCampDonationsShouldProduceIndependentLabTestEvents() {
            UUID branchId = UUID.randomUUID();
            int campDonationCount = 5;

            UUID[] donationIds = new UUID[campDonationCount];
            for (int i = 0; i < campDonationCount; i++) {
                donationIds[i] = UUID.randomUUID();
                DonationCompletedEvent event = new DonationCompletedEvent(
                        donationIds[i], UUID.randomUUID(), branchId, Instant.now());
                publishEvent(EventConstants.DONATION_COMPLETED, event);
            }

            for (int i = 0; i < campDonationCount; i++) {
                DonationCompletedEvent labReceived = assertEventReceived(
                        LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
                assertThat(labReceived.branchId()).isEqualTo(branchId);
            }
        }
    }

    @Nested
    @DisplayName("Full workflow: Camp planning to post-camp follow-up event chain")
    class FullCampWorkflowEventChain {

        @Test
        @DisplayName("Complete camp workflow should route camp completion and donation events correctly")
        void completeCampWorkflowShouldRouteAllEvents() {
            UUID campId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID donorId1 = UUID.randomUUID();
            UUID donorId2 = UUID.randomUUID();
            UUID donationId1 = UUID.randomUUID();
            UUID donationId2 = UUID.randomUUID();
            Instant now = Instant.now();

            // Step 1: Individual donations collected during camp
            DonationCompletedEvent donation1 = new DonationCompletedEvent(
                    donationId1, donorId1, branchId, now);
            publishEvent(EventConstants.DONATION_COMPLETED, donation1);

            assertEventReceived(LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            assertEventReceived(NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            DonationCompletedEvent donation2 = new DonationCompletedEvent(
                    donationId2, donorId2, branchId, now);
            publishEvent(EventConstants.DONATION_COMPLETED, donation2);

            assertEventReceived(LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            assertEventReceived(NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            // Step 2: Camp completed (all collections finished)
            CampCompletedEvent campCompleted = new CampCompletedEvent(campId, branchId, 2, now);
            publishEvent(EventConstants.CAMP_COMPLETED, campCompleted);

            CampCompletedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);
            CampCompletedEvent reportingReceived = assertEventReceived(
                    REPORTING_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);

            assertThat(notificationReceived.campId()).isEqualTo(campId);
            assertThat(notificationReceived.totalCollections()).isEqualTo(2);
            assertThat(reportingReceived.campId()).isEqualTo(campId);
        }

        @Test
        @DisplayName("Large camp with many donations should route all events without loss")
        void largeCampShouldRouteAllEventsWithoutLoss() {
            UUID campId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            int totalDonations = 10;

            // Publish all donations
            for (int i = 0; i < totalDonations; i++) {
                DonationCompletedEvent donation = new DonationCompletedEvent(
                        UUID.randomUUID(), UUID.randomUUID(), branchId, Instant.now());
                publishEvent(EventConstants.DONATION_COMPLETED, donation);
            }

            // Verify all donations reached lab queue
            for (int i = 0; i < totalDonations; i++) {
                DonationCompletedEvent labReceived = assertEventReceived(
                        LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
                assertThat(labReceived.branchId()).isEqualTo(branchId);
            }

            // Camp completed with accurate count
            CampCompletedEvent campCompleted = new CampCompletedEvent(
                    campId, branchId, totalDonations, Instant.now());
            publishEvent(EventConstants.CAMP_COMPLETED, campCompleted);

            CampCompletedEvent received = assertEventReceived(
                    NOTIFICATION_CAMP_COMPLETED_QUEUE, CampCompletedEvent.class);
            assertThat(received.totalCollections()).isEqualTo(totalDonations);
        }
    }
}
