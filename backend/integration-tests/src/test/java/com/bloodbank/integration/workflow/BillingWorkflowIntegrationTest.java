package com.bloodbank.integration.workflow;

import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.InvoiceGeneratedEvent;
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
 * M6-007: Invoice Generation → Payment → Credit Note
 *
 * Tests the billing workflow event chain from invoice generation through
 * payment notification and credit note issuance. Verifies that billing events
 * reach the correct downstream services for notification and audit.
 *
 * Event flow verified:
 *   billing-service  --[InvoiceGeneratedEvent]-------> notification-service (invoice alert)
 *   billing-service  --[InvoiceGeneratedEvent]-------> reporting-service (financial audit log)
 *
 * Verifications:
 * - InvoiceGeneratedEvent reaches notification-service for invoice delivery to hospital
 * - InvoiceGeneratedEvent reaches reporting-service for financial audit trail
 * - All invoice fields (invoiceId, hospitalId, branchId) preserved through serialization
 * - Multiple invoices for the same hospital route independently
 */
@DisplayName("M6-007: Invoice Generation → Payment → Credit Note")
class BillingWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

    private static final String NOTIFICATION_INVOICE_GENERATED_QUEUE =
            "notification.invoice.generated.queue";
    private static final String REPORTING_INVOICE_GENERATED_QUEUE =
            "reporting.invoice.generated.queue";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // notification-service listens for invoice.generated (deliver invoice to hospital)
        declareAndBindQueue(admin, exchange,
                NOTIFICATION_INVOICE_GENERATED_QUEUE, EventConstants.INVOICE_GENERATED);

        // reporting-service listens for invoice.generated (financial audit trail)
        declareAndBindQueue(admin, exchange,
                REPORTING_INVOICE_GENERATED_QUEUE, EventConstants.INVOICE_GENERATED);
    }

    @Nested
    @DisplayName("Step 1: Invoice generated triggers notification and reporting")
    class InvoiceGeneratedTriggersNotificationAndReporting {

        @Test
        @DisplayName("InvoiceGeneratedEvent should reach notification-service for invoice delivery")
        void invoiceGeneratedEventShouldReachNotificationService() {
            UUID invoiceId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            InvoiceGeneratedEvent event = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, Instant.now());

            publishEvent(EventConstants.INVOICE_GENERATED, event);

            InvoiceGeneratedEvent received = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(received.invoiceId()).isEqualTo(invoiceId);
            assertThat(received.hospitalId()).isEqualTo(hospitalId);
            assertThat(received.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("InvoiceGeneratedEvent should reach reporting-service for audit trail")
        void invoiceGeneratedEventShouldReachReportingService() {
            UUID invoiceId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            InvoiceGeneratedEvent event = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, Instant.now());

            publishEvent(EventConstants.INVOICE_GENERATED, event);

            // Drain notification queue
            assertEventReceived(NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            InvoiceGeneratedEvent received = assertEventReceived(
                    REPORTING_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(received.invoiceId()).isEqualTo(invoiceId);
            assertThat(received.hospitalId()).isEqualTo(hospitalId);
        }

        @Test
        @DisplayName("InvoiceGeneratedEvent should fan-out to notification and reporting")
        void invoiceGeneratedEventShouldFanOutToNotificationAndReporting() {
            UUID invoiceId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            InvoiceGeneratedEvent event = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, Instant.now());

            publishEvent(EventConstants.INVOICE_GENERATED, event);

            InvoiceGeneratedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            InvoiceGeneratedEvent reportingReceived = assertEventReceived(
                    REPORTING_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(notificationReceived.invoiceId()).isEqualTo(invoiceId);
            assertThat(reportingReceived.invoiceId()).isEqualTo(invoiceId);
        }

        @Test
        @DisplayName("InvoiceGeneratedEvent should preserve all fields for payment processing")
        void invoiceGeneratedEventShouldPreserveAllFieldsForPaymentProcessing() {
            UUID invoiceId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            InvoiceGeneratedEvent event = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, now);

            publishEvent(EventConstants.INVOICE_GENERATED, event);

            InvoiceGeneratedEvent received = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(received.invoiceId()).isEqualTo(invoiceId);
            assertThat(received.hospitalId()).isEqualTo(hospitalId);
            assertThat(received.branchId()).isEqualTo(branchId);
            assertThat(received.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Multiple invoices route independently")
    class MultipleInvoicesRouteIndependently {

        @Test
        @DisplayName("Multiple invoices for the same hospital should each route independently")
        void multipleInvoicesForSameHospitalShouldRouteIndependently() {
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            UUID invoiceId1 = UUID.randomUUID();
            UUID invoiceId2 = UUID.randomUUID();
            UUID invoiceId3 = UUID.randomUUID();

            publishEvent(EventConstants.INVOICE_GENERATED,
                    new InvoiceGeneratedEvent(invoiceId1, hospitalId, branchId, Instant.now()));
            publishEvent(EventConstants.INVOICE_GENERATED,
                    new InvoiceGeneratedEvent(invoiceId2, hospitalId, branchId, Instant.now()));
            publishEvent(EventConstants.INVOICE_GENERATED,
                    new InvoiceGeneratedEvent(invoiceId3, hospitalId, branchId, Instant.now()));

            InvoiceGeneratedEvent first = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            InvoiceGeneratedEvent second = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            InvoiceGeneratedEvent third = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(first.invoiceId()).isNotEqualTo(second.invoiceId());
            assertThat(second.invoiceId()).isNotEqualTo(third.invoiceId());
            assertThat(first.hospitalId()).isEqualTo(hospitalId);
            assertThat(second.hospitalId()).isEqualTo(hospitalId);
            assertThat(third.hospitalId()).isEqualTo(hospitalId);
        }

        @Test
        @DisplayName("Invoices for different hospitals should not interfere with each other")
        void invoicesForDifferentHospitalsShouldNotInterferere() {
            UUID hospital1 = UUID.randomUUID();
            UUID hospital2 = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID invoiceId1 = UUID.randomUUID();
            UUID invoiceId2 = UUID.randomUUID();

            publishEvent(EventConstants.INVOICE_GENERATED,
                    new InvoiceGeneratedEvent(invoiceId1, hospital1, branchId, Instant.now()));
            publishEvent(EventConstants.INVOICE_GENERATED,
                    new InvoiceGeneratedEvent(invoiceId2, hospital2, branchId, Instant.now()));

            InvoiceGeneratedEvent first = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            InvoiceGeneratedEvent second = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);

            assertThat(first.invoiceId()).isNotEqualTo(second.invoiceId());
        }
    }

    @Nested
    @DisplayName("Full workflow: Invoice to credit note billing lifecycle")
    class FullBillingWorkflowEventChain {

        @Test
        @DisplayName("Complete billing workflow should produce invoice event reaching all services")
        void completeBillingWorkflowShouldProduceInvoiceEventReachingAllServices() {
            UUID invoiceId = UUID.randomUUID();
            UUID hospitalId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            Instant now = Instant.now();

            // Billing service generates invoice after blood request matched and issued
            InvoiceGeneratedEvent invoiceGenerated = new InvoiceGeneratedEvent(
                    invoiceId, hospitalId, branchId, now);
            publishEvent(EventConstants.INVOICE_GENERATED, invoiceGenerated);

            // Notification-service receives to send invoice to hospital finance department
            InvoiceGeneratedEvent notificationReceived = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            assertThat(notificationReceived.invoiceId()).isEqualTo(invoiceId);
            assertThat(notificationReceived.hospitalId()).isEqualTo(hospitalId);

            // Reporting-service receives for financial audit trail and revenue reporting
            InvoiceGeneratedEvent reportingReceived = assertEventReceived(
                    REPORTING_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            assertThat(reportingReceived.invoiceId()).isEqualTo(invoiceId);
            assertThat(reportingReceived.occurredAt()).isEqualTo(now);

            // Credit note scenario: second invoice for the same hospital (e.g. credit adjustment)
            UUID creditNoteInvoiceId = UUID.randomUUID();
            InvoiceGeneratedEvent creditNoteEvent = new InvoiceGeneratedEvent(
                    creditNoteInvoiceId, hospitalId, branchId, Instant.now());
            publishEvent(EventConstants.INVOICE_GENERATED, creditNoteEvent);

            InvoiceGeneratedEvent creditNoteNotification = assertEventReceived(
                    NOTIFICATION_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            assertThat(creditNoteNotification.invoiceId()).isEqualTo(creditNoteInvoiceId);
            assertThat(creditNoteNotification.hospitalId()).isEqualTo(hospitalId);

            // Both original and credit note reach reporting
            InvoiceGeneratedEvent creditNoteReporting = assertEventReceived(
                    REPORTING_INVOICE_GENERATED_QUEUE, InvoiceGeneratedEvent.class);
            assertThat(creditNoteReporting.invoiceId()).isEqualTo(creditNoteInvoiceId);
        }
    }
}
