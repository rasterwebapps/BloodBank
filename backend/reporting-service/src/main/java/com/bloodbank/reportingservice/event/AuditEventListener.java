package com.bloodbank.reportingservice.event;

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
import com.bloodbank.reportingservice.config.RabbitMQConfig;
import com.bloodbank.reportingservice.entity.AuditLog;
import com.bloodbank.reportingservice.enums.AuditActionEnum;
import com.bloodbank.reportingservice.repository.AuditLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditLogRepository auditLogRepository;

    public AuditEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.DONATION_COMPLETED_QUEUE)
    public void handleDonationCompleted(DonationCompletedEvent event) {
        log.info("Received DonationCompletedEvent for donation {}", event.donationId());
        createAuditLog("Donation", event.donationId(), AuditActionEnum.CREATE,
                event.branchId(), "Donation completed for donor " + event.donorId(), event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.CAMP_COMPLETED_QUEUE)
    public void handleCampCompleted(CampCompletedEvent event) {
        log.info("Received CampCompletedEvent for camp {}", event.campId());
        createAuditLog("BloodCamp", event.campId(), AuditActionEnum.UPDATE,
                event.branchId(), "Blood camp completed with " + event.totalCollections() + " collections",
                event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.TEST_RESULT_AVAILABLE_QUEUE)
    public void handleTestResultAvailable(TestResultAvailableEvent event) {
        log.info("Received TestResultAvailableEvent for order {}", event.testOrderId());
        createAuditLog("TestOrder", event.testOrderId(), AuditActionEnum.UPDATE,
                event.branchId(), "Test result available for blood unit " + event.bloodUnitId(),
                event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.UNIT_RELEASED_QUEUE)
    public void handleUnitReleased(UnitReleasedEvent event) {
        log.info("Received UnitReleasedEvent for unit {}", event.bloodUnitId());
        createAuditLog("BloodUnit", event.bloodUnitId(), AuditActionEnum.UPDATE,
                event.branchId(), "Blood unit released", event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.BLOOD_STOCK_UPDATED_QUEUE)
    public void handleBloodStockUpdated(BloodStockUpdatedEvent event) {
        log.info("Received BloodStockUpdatedEvent for branch {}", event.branchId());
        createAuditLog("BloodStock", UUID.randomUUID(), AuditActionEnum.UPDATE,
                event.branchId(), "Stock updated: " + event.bloodGroup() + " " + event.componentType()
                        + " qty=" + event.quantity(), event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_CRITICAL_QUEUE)
    public void handleStockCritical(StockCriticalEvent event) {
        log.info("Received StockCriticalEvent for branch {}", event.branchId());
        createAuditLog("BloodStock", UUID.randomUUID(), AuditActionEnum.UPDATE,
                event.branchId(), "Critical stock level: " + event.bloodGroup() + " " + event.componentType()
                        + " current=" + event.currentStock() + " min=" + event.minimumStock(),
                event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.UNIT_EXPIRING_QUEUE)
    public void handleUnitExpiring(UnitExpiringEvent event) {
        log.info("Received UnitExpiringEvent for unit {}", event.bloodUnitId());
        createAuditLog("BloodUnit", event.bloodUnitId(), AuditActionEnum.UPDATE,
                event.branchId(), "Blood unit expiring on " + event.expiryDate(), event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.BLOOD_REQUEST_CREATED_QUEUE)
    public void handleBloodRequestCreated(BloodRequestCreatedEvent event) {
        log.info("Received BloodRequestCreatedEvent for request {}", event.requestId());
        createAuditLog("BloodRequest", event.requestId(), AuditActionEnum.CREATE,
                event.branchId(), "Blood request created: " + event.bloodGroup() + " qty=" + event.quantity(),
                event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.BLOOD_REQUEST_MATCHED_QUEUE)
    public void handleBloodRequestMatched(BloodRequestMatchedEvent event) {
        log.info("Received BloodRequestMatchedEvent for request {}", event.requestId());
        createAuditLog("BloodRequest", event.requestId(), AuditActionEnum.UPDATE,
                event.branchId(), "Blood request matched with " + event.matchedUnitIds().size() + " units",
                event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.EMERGENCY_REQUEST_QUEUE)
    public void handleEmergencyRequest(EmergencyRequestEvent event) {
        log.info("Received EmergencyRequestEvent for request {}", event.requestId());
        createAuditLog("EmergencyRequest", event.requestId(), AuditActionEnum.CREATE,
                event.branchId(), "Emergency request: " + event.bloodGroup() + " severity=" + event.severity(),
                event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSFUSION_COMPLETED_QUEUE)
    public void handleTransfusionCompleted(TransfusionCompletedEvent event) {
        log.info("Received TransfusionCompletedEvent for transfusion {}", event.transfusionId());
        createAuditLog("Transfusion", event.transfusionId(), AuditActionEnum.CREATE,
                event.branchId(), "Transfusion completed for blood unit " + event.bloodUnitId(),
                event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSFUSION_REACTION_QUEUE)
    public void handleTransfusionReaction(TransfusionReactionEvent event) {
        log.info("Received TransfusionReactionEvent for transfusion {}", event.transfusionId());
        createAuditLog("Transfusion", event.transfusionId(), AuditActionEnum.UPDATE,
                event.branchId(), "Transfusion reaction reported: " + event.severity()
                        + " for unit " + event.bloodUnitId(), event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.INVOICE_GENERATED_QUEUE)
    public void handleInvoiceGenerated(InvoiceGeneratedEvent event) {
        log.info("Received InvoiceGeneratedEvent for invoice {}", event.invoiceId());
        createAuditLog("Invoice", event.invoiceId(), AuditActionEnum.CREATE,
                event.branchId(), "Invoice generated for hospital " + event.hospitalId(),
                event.occurredAt());
    }

    @RabbitListener(queues = RabbitMQConfig.RECALL_INITIATED_QUEUE)
    public void handleRecallInitiated(RecallInitiatedEvent event) {
        log.info("Received RecallInitiatedEvent for recall {}", event.recallId());
        createAuditLog("Recall", event.recallId(), AuditActionEnum.CREATE,
                event.branchId(), "Recall initiated: " + event.reason()
                        + " affecting " + event.affectedUnitIds().size() + " units",
                event.occurredAt());
    }

    private void createAuditLog(String entityType, UUID entityId, AuditActionEnum action,
                                UUID branchId, String description, Instant timestamp) {
        AuditLog auditLog = new AuditLog(entityType, entityId, action, "SYSTEM", description, timestamp);
        auditLog.setBranchId(branchId);
        auditLog.setActorName("System Event Processor");
        auditLog.setActorRole("SYSTEM");
        auditLogRepository.save(auditLog);
    }
}
