package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.DeliveryConditionEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_confirmations")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class DeliveryConfirmation extends BranchScopedEntity {

    @Column(name = "transport_request_id", nullable = false)
    private UUID transportRequestId;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_arrival")
    private DeliveryConditionEnum conditionOnArrival;

    @Column(name = "temperature_on_arrival")
    private BigDecimal temperatureOnArrival;

    @Column(name = "units_received")
    private int unitsReceived;

    @Column(name = "units_rejected")
    private int unitsRejected;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "signature_reference")
    private String signatureReference;

    @Column(name = "notes")
    private String notes;

    protected DeliveryConfirmation() {}

    public DeliveryConfirmation(UUID transportRequestId, String receivedBy,
                                DeliveryConditionEnum conditionOnArrival, int unitsReceived) {
        this.transportRequestId = transportRequestId;
        this.receivedBy = receivedBy;
        this.conditionOnArrival = conditionOnArrival;
        this.unitsReceived = unitsReceived;
        this.receivedAt = Instant.now();
    }

    public UUID getTransportRequestId() { return transportRequestId; }
    public void setTransportRequestId(UUID transportRequestId) { this.transportRequestId = transportRequestId; }

    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public DeliveryConditionEnum getConditionOnArrival() { return conditionOnArrival; }
    public void setConditionOnArrival(DeliveryConditionEnum conditionOnArrival) { this.conditionOnArrival = conditionOnArrival; }

    public BigDecimal getTemperatureOnArrival() { return temperatureOnArrival; }
    public void setTemperatureOnArrival(BigDecimal temperatureOnArrival) { this.temperatureOnArrival = temperatureOnArrival; }

    public int getUnitsReceived() { return unitsReceived; }
    public void setUnitsReceived(int unitsReceived) { this.unitsReceived = unitsReceived; }

    public int getUnitsRejected() { return unitsRejected; }
    public void setUnitsRejected(int unitsRejected) { this.unitsRejected = unitsRejected; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getSignatureReference() { return signatureReference; }
    public void setSignatureReference(String signatureReference) { this.signatureReference = signatureReference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
