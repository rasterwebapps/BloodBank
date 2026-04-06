package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.TransferStatusEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_transfers")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class StockTransfer extends BranchScopedEntity {

    @Column(name = "source_branch_id", nullable = false)
    private UUID sourceBranchId;

    @Column(name = "destination_branch_id", nullable = false)
    private UUID destinationBranchId;

    @Column(name = "transfer_number", unique = true, nullable = false)
    private String transferNumber;

    @Column(name = "component_id")
    private UUID componentId;

    @Column(name = "pooled_component_id")
    private UUID pooledComponentId;

    @Column(name = "request_date")
    private Instant requestDate;

    @Column(name = "shipped_date")
    private Instant shippedDate;

    @Column(name = "received_date")
    private Instant receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatusEnum status;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "notes")
    private String notes;

    protected StockTransfer() {}

    public StockTransfer(UUID sourceBranchId, UUID destinationBranchId, String transferNumber,
                         String requestedBy) {
        this.sourceBranchId = sourceBranchId;
        this.destinationBranchId = destinationBranchId;
        this.transferNumber = transferNumber;
        this.requestedBy = requestedBy;
        this.requestDate = Instant.now();
        this.status = TransferStatusEnum.REQUESTED;
    }

    public UUID getSourceBranchId() { return sourceBranchId; }
    public void setSourceBranchId(UUID sourceBranchId) { this.sourceBranchId = sourceBranchId; }

    public UUID getDestinationBranchId() { return destinationBranchId; }
    public void setDestinationBranchId(UUID destinationBranchId) { this.destinationBranchId = destinationBranchId; }

    public String getTransferNumber() { return transferNumber; }
    public void setTransferNumber(String transferNumber) { this.transferNumber = transferNumber; }

    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }

    public UUID getPooledComponentId() { return pooledComponentId; }
    public void setPooledComponentId(UUID pooledComponentId) { this.pooledComponentId = pooledComponentId; }

    public Instant getRequestDate() { return requestDate; }
    public void setRequestDate(Instant requestDate) { this.requestDate = requestDate; }

    public Instant getShippedDate() { return shippedDate; }
    public void setShippedDate(Instant shippedDate) { this.shippedDate = shippedDate; }

    public Instant getReceivedDate() { return receivedDate; }
    public void setReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; }

    public TransferStatusEnum getStatus() { return status; }
    public void setStatus(TransferStatusEnum status) { this.status = status; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
