package com.bloodbank.notificationservice.event;

import com.bloodbank.common.events.BloodRequestCreatedEvent;
import com.bloodbank.common.events.BloodRequestMatchedEvent;
import com.bloodbank.common.events.BloodStockUpdatedEvent;
import com.bloodbank.common.events.CampCompletedEvent;
import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.events.EmergencyRequestEvent;
import com.bloodbank.common.events.InvoiceGeneratedEvent;
import com.bloodbank.common.events.RecallInitiatedEvent;
import com.bloodbank.common.events.StockCriticalEvent;
import com.bloodbank.common.events.TestResultAvailableEvent;
import com.bloodbank.common.events.TransfusionCompletedEvent;
import com.bloodbank.common.events.TransfusionReactionEvent;
import com.bloodbank.common.events.UnitExpiringEvent;
import com.bloodbank.common.events.UnitReleasedEvent;
import com.bloodbank.notificationservice.config.RabbitMQConfig;
import com.bloodbank.notificationservice.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(DomainEventListener.class);

    private final NotificationService notificationService;

    public DomainEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.DONATION_COMPLETED_QUEUE)
    public void handleDonationCompleted(DonationCompletedEvent event) {
        log.info("Received DonationCompletedEvent: donationId={}, donorId={}, branchId={}",
                event.donationId(), event.donorId(), event.branchId());
        notificationService.createSystemNotification(
                "Donation Completed",
                "Donation " + event.donationId() + " completed for donor " + event.donorId(),
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.CAMP_COMPLETED_QUEUE)
    public void handleCampCompleted(CampCompletedEvent event) {
        log.info("Received CampCompletedEvent: campId={}, branchId={}, totalCollections={}",
                event.campId(), event.branchId(), event.totalCollections());
        notificationService.createSystemNotification(
                "Camp Completed",
                "Camp " + event.campId() + " completed with " + event.totalCollections() + " collections",
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.TEST_RESULT_AVAILABLE_QUEUE)
    public void handleTestResultAvailable(TestResultAvailableEvent event) {
        log.info("Received TestResultAvailableEvent: testOrderId={}, bloodUnitId={}, branchId={}",
                event.testOrderId(), event.bloodUnitId(), event.branchId());
        notificationService.createSystemNotification(
                "Test Result Available",
                "Test results available for order " + event.testOrderId(),
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.UNIT_RELEASED_QUEUE)
    public void handleUnitReleased(UnitReleasedEvent event) {
        log.info("Received UnitReleasedEvent: bloodUnitId={}, branchId={}",
                event.bloodUnitId(), event.branchId());
        notificationService.createSystemNotification(
                "Unit Released",
                "Blood unit " + event.bloodUnitId() + " has been released",
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.BLOOD_STOCK_UPDATED_QUEUE)
    public void handleBloodStockUpdated(BloodStockUpdatedEvent event) {
        log.info("Received BloodStockUpdatedEvent: branchId={}, bloodGroup={}, quantity={}",
                event.branchId(), event.bloodGroup(), event.quantity());
        notificationService.createSystemNotification(
                "Blood Stock Updated",
                "Stock updated for " + event.bloodGroup() + ": " + event.quantity() + " units",
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_CRITICAL_QUEUE)
    public void handleStockCritical(StockCriticalEvent event) {
        log.info("Received StockCriticalEvent: branchId={}, bloodGroup={}, currentStock={}",
                event.branchId(), event.bloodGroup(), event.currentStock());
        notificationService.createSystemNotification(
                "Critical Stock Alert",
                "CRITICAL: " + event.bloodGroup() + " stock at " + event.currentStock()
                        + " (minimum: " + event.minimumStock() + ")",
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.UNIT_EXPIRING_QUEUE)
    public void handleUnitExpiring(UnitExpiringEvent event) {
        log.info("Received UnitExpiringEvent: bloodUnitId={}, branchId={}, expiryDate={}",
                event.bloodUnitId(), event.branchId(), event.expiryDate());
        notificationService.createSystemNotification(
                "Unit Expiring Soon",
                "Blood unit " + event.bloodUnitId() + " expires on " + event.expiryDate(),
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.BLOOD_REQUEST_CREATED_QUEUE)
    public void handleBloodRequestCreated(BloodRequestCreatedEvent event) {
        log.info("Received BloodRequestCreatedEvent: requestId={}, branchId={}, bloodGroup={}",
                event.requestId(), event.branchId(), event.bloodGroup());
        notificationService.createSystemNotification(
                "Blood Request Created",
                "New blood request " + event.requestId() + " for " + event.bloodGroup()
                        + " (" + event.quantity() + " units)",
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.BLOOD_REQUEST_MATCHED_QUEUE)
    public void handleBloodRequestMatched(BloodRequestMatchedEvent event) {
        log.info("Received BloodRequestMatchedEvent: requestId={}, branchId={}, matchedUnits={}",
                event.requestId(), event.branchId(), event.matchedUnitIds().size());
        notificationService.createSystemNotification(
                "Blood Request Matched",
                "Request " + event.requestId() + " matched with " + event.matchedUnitIds().size() + " units",
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.EMERGENCY_REQUEST_QUEUE)
    public void handleEmergencyRequest(EmergencyRequestEvent event) {
        log.info("Received EmergencyRequestEvent: requestId={}, branchId={}, severity={}",
                event.requestId(), event.branchId(), event.severity());
        notificationService.createSystemNotification(
                "Emergency Blood Request",
                "EMERGENCY [" + event.severity() + "]: Request " + event.requestId()
                        + " for " + event.bloodGroup(),
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSFUSION_COMPLETED_QUEUE)
    public void handleTransfusionCompleted(TransfusionCompletedEvent event) {
        log.info("Received TransfusionCompletedEvent: transfusionId={}, bloodUnitId={}, branchId={}",
                event.transfusionId(), event.bloodUnitId(), event.branchId());
        notificationService.createSystemNotification(
                "Transfusion Completed",
                "Transfusion " + event.transfusionId() + " completed for unit " + event.bloodUnitId(),
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSFUSION_REACTION_QUEUE)
    public void handleTransfusionReaction(TransfusionReactionEvent event) {
        log.info("Received TransfusionReactionEvent: transfusionId={}, severity={}, branchId={}",
                event.transfusionId(), event.severity(), event.branchId());
        notificationService.createSystemNotification(
                "Transfusion Reaction Alert",
                "REACTION [" + event.severity() + "]: Transfusion " + event.transfusionId()
                        + " for unit " + event.bloodUnitId(),
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.INVOICE_GENERATED_QUEUE)
    public void handleInvoiceGenerated(InvoiceGeneratedEvent event) {
        log.info("Received InvoiceGeneratedEvent: invoiceId={}, hospitalId={}, branchId={}",
                event.invoiceId(), event.hospitalId(), event.branchId());
        notificationService.createSystemNotification(
                "Invoice Generated",
                "Invoice " + event.invoiceId() + " generated for hospital " + event.hospitalId(),
                event.branchId());
    }

    @RabbitListener(queues = RabbitMQConfig.RECALL_INITIATED_QUEUE)
    public void handleRecallInitiated(RecallInitiatedEvent event) {
        log.info("Received RecallInitiatedEvent: recallId={}, branchId={}, reason={}",
                event.recallId(), event.branchId(), event.reason());
        notificationService.createSystemNotification(
                "Recall Initiated",
                "RECALL: " + event.reason() + " affecting " + event.affectedUnitIds().size() + " units",
                event.branchId());
    }
}
