package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.DisposalReasonEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "unit_disposals")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class UnitDisposal extends BranchScopedEntity {

    @Column(name = "blood_unit_id")
    private UUID bloodUnitId;

    @Column(name = "component_id")
    private UUID componentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposal_reason", nullable = false)
    private DisposalReasonEnum disposalReason;

    @Column(name = "disposal_date")
    private Instant disposalDate;

    @Column(name = "disposed_by")
    private String disposedBy;

    @Column(name = "authorization_by")
    private String authorizationBy;

    @Column(name = "notes")
    private String notes;

    protected UnitDisposal() {}

    public UnitDisposal(UUID bloodUnitId, UUID componentId, DisposalReasonEnum disposalReason,
                        String disposedBy, String authorizationBy) {
        this.bloodUnitId = bloodUnitId;
        this.componentId = componentId;
        this.disposalReason = disposalReason;
        this.disposedBy = disposedBy;
        this.authorizationBy = authorizationBy;
        this.disposalDate = Instant.now();
    }

    public UUID getBloodUnitId() { return bloodUnitId; }
    public void setBloodUnitId(UUID bloodUnitId) { this.bloodUnitId = bloodUnitId; }

    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }

    public DisposalReasonEnum getDisposalReason() { return disposalReason; }
    public void setDisposalReason(DisposalReasonEnum disposalReason) { this.disposalReason = disposalReason; }

    public Instant getDisposalDate() { return disposalDate; }
    public void setDisposalDate(Instant disposalDate) { this.disposalDate = disposalDate; }

    public String getDisposedBy() { return disposedBy; }
    public void setDisposedBy(String disposedBy) { this.disposedBy = disposedBy; }

    public String getAuthorizationBy() { return authorizationBy; }
    public void setAuthorizationBy(String authorizationBy) { this.authorizationBy = authorizationBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
