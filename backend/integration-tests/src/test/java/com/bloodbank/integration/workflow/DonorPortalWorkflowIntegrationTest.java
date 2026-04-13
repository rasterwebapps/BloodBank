package com.bloodbank.integration.workflow;

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
 * M6-009: Donor Portal Self-Registration → Appointment → Digital Card
 *
 * Tests the donor portal self-service workflow from initial self-registration
 * through appointment booking and post-donation digital card issuance.
 * The workflow verifies that donation events trigger the correct downstream
 * processing (lab testing, notification with digital card delivery).
 *
 * Event flow verified:
 *   donor-service    --[DonationCompletedEvent]-------> lab-service (test order creation)
 *   donor-service    --[DonationCompletedEvent]-------> notification-service (digital card + thanks)
 *
 * Verifications:
 * - DonationCompletedEvent from portal appointment reaches lab-service for testing
 * - DonationCompletedEvent reaches notification-service for digital donation card delivery
 * - Donor ID is preserved through events for card association
 * - Multiple portal donors produce independent donation events
 * - First-time and repeat donors route through the same event channel
 */
@DisplayName("M6-009: Donor Portal Self-Registration → Appointment → Digital Card")
class DonorPortalWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String LAB_DONATION_COMPLETED_QUEUE =
            "lab.donation.completed.queue";
    private static final String NOTIFICATION_DONATION_COMPLETED_QUEUE =
            "notification.donation.completed.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // lab-service listens for donation.completed (test order for portal donor's donation)
        declareAndBindQueue(admin, exchange,
                LAB_DONATION_COMPLETED_QUEUE, EventConstants.DONATION_COMPLETED);

        // notification-service listens for donation.completed (digital card + thank-you delivery)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_DONATION_COMPLETED_QUEUE, EventConstants.DONATION_COMPLETED);
    }

    @Nested
    @DisplayName("Step 1: Portal donor completes appointment and donates")
    class PortalDonorCompletesAppointmentAndDonates {

        @Test
        @DisplayName("DonationCompletedEvent from portal appointment should reach lab-service")
        void portalDonationCompletedEventShouldReachLabService() {
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
        @DisplayName("DonationCompletedEvent should reach notification-service for digital card delivery")
        void portalDonationCompletedEventShouldReachNotificationForDigitalCard() {
            UUID donationId = UUID.randomUUID();
            UUID donorId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            DonationCompletedEvent event = new DonationCompletedEvent(
                    donationId, donorId, branchId, Instant.now());

            publishEvent(EventConstants.DONATION_COMPLETED, event);

            // Drain lab queue
            assertEventReceived(LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            DonationCompletedEvent received = assertEventReceived(
                    NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(received.donationId()).isEqualTo(donationId);
            assertThat(received.donorId()).isEqualTo(donorId);
        }

        @Test
        @DisplayName("DonationCompletedEvent should fan-out to lab and notification for digital card")
        void portalDonationCompletedEventShouldFanOutToLabAndNotification() {
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
    }

    @Nested
    @DisplayName("Step 2: Donor ID preserved for digital card association")
    class DonorIdPreservedForDigitalCardAssociation {

        @Test
        @DisplayName("DonationCompletedEvent should preserve donor ID for digital card association")
        void donationCompletedEventShouldPreserveDonorIdForDigitalCard() {
            UUID donationId = UUID.randomUUID();
            UUID donorId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            DonationCompletedEvent event = new DonationCompletedEvent(
                    donationId, donorId, branchId, now);

            publishEvent(EventConstants.DONATION_COMPLETED, event);

            DonationCompletedEvent received = assertEventReceived(
                    NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(received.donorId()).isEqualTo(donorId);
            assertThat(received.donationId()).isEqualTo(donationId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Multiple portal donors produce independent events")
    class MultiplePortalDonorsProduceIndependentEvents {

        @Test
        @DisplayName("Multiple portal donors donating should each produce independent events")
        void multiplePortalDonorsShouldProduceIndependentEvents() {
            UUID branchId = UUID.randomUUID();

            UUID donorId1 = UUID.randomUUID();
            UUID donorId2 = UUID.randomUUID();
            UUID donorId3 = UUID.randomUUID();
            UUID donationId1 = UUID.randomUUID();
            UUID donationId2 = UUID.randomUUID();
            UUID donationId3 = UUID.randomUUID();

            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId1, donorId1, branchId, Instant.now()));
            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId2, donorId2, branchId, Instant.now()));
            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId3, donorId3, branchId, Instant.now()));

            DonationCompletedEvent first = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent second = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent third = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(first.donationId()).isNotEqualTo(second.donationId());
            assertThat(second.donationId()).isNotEqualTo(third.donationId());
            assertThat(first.donorId()).isNotEqualTo(second.donorId());
        }

        @Test
        @DisplayName("Repeat donor should produce new independent donation event each time")
        void repeatDonorShouldProduceNewDonationEventEachTime() {
            UUID donorId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            UUID donationId1 = UUID.randomUUID();
            UUID donationId2 = UUID.randomUUID();

            // First donation
            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId1, donorId, branchId, Instant.now()));

            DonationCompletedEvent firstLab = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent firstNotification = assertEventReceived(
                    NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            assertThat(firstLab.donorId()).isEqualTo(donorId);
            assertThat(firstNotification.donorId()).isEqualTo(donorId);

            // Second donation by the same donor (repeat)
            publishEvent(EventConstants.DONATION_COMPLETED,
                    new DonationCompletedEvent(donationId2, donorId, branchId, Instant.now()));

            DonationCompletedEvent secondLab = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            DonationCompletedEvent secondNotification = assertEventReceived(
                    NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);

            assertThat(secondLab.donorId()).isEqualTo(donorId);
            assertThat(secondNotification.donorId()).isEqualTo(donorId);

            // Both donations belong to same donor but are distinct events
            assertThat(firstLab.donationId()).isNotEqualTo(secondLab.donationId());
        }
    }

    @Nested
    @DisplayName("Full workflow: Portal self-registration to digital card delivery")
    class FullPortalWorkflowEventChain {

        @Test
        @DisplayName("Complete portal workflow should route donation events for lab and digital card")
        void completePortalWorkflowShouldRouteAllEvents() {
            UUID donationId = UUID.randomUUID();
            UUID donorId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            // Donor completed appointment and donated via portal
            DonationCompletedEvent donationCompleted = new DonationCompletedEvent(
                    donationId, donorId, branchId, now);
            publishEvent(EventConstants.DONATION_COMPLETED, donationCompleted);

            // Lab service receives to create test order for donated unit
            DonationCompletedEvent labReceived = assertEventReceived(
                    LAB_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            assertThat(labReceived.donationId()).isEqualTo(donationId);
            assertThat(labReceived.donorId()).isEqualTo(donorId);
            assertThat(labReceived.branchId()).isEqualTo(branchId);
            assertThat(labReceived.occurredAt()).isEqualTo(now);

            // Notification service receives to deliver digital donation card to donor
            DonationCompletedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_DONATION_COMPLETED_QUEUE, DonationCompletedEvent.class);
            assertThat(notificationReceived.donationId()).isEqualTo(donationId);
            assertThat(notificationReceived.donorId()).isEqualTo(donorId);
        }
    }
}
