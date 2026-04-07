package com.bloodbank.transfusionservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.transfusionservice.enums.CrossMatchMethodEnum;
import com.bloodbank.transfusionservice.enums.CrossMatchResultEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "crossmatch_results")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class CrossMatchResult extends BranchScopedEntity {

    @Column(name = "crossmatch_request_id", nullable = false)
    private UUID crossmatchRequestId;

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "crossmatch_method", nullable = false, length = 50)
    private CrossMatchMethodEnum crossmatchMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private CrossMatchResultEnum result;

    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @Column(name = "verified_by", length = 255)
    private String verifiedBy;

    @Column(name = "performed_at")
    private Instant performedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public CrossMatchResult() {}

    public UUID getCrossmatchRequestId() { return crossmatchRequestId; }
    public void setCrossmatchRequestId(UUID crossmatchRequestId) { this.crossmatchRequestId = crossmatchRequestId; }

    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }

    public CrossMatchMethodEnum getCrossmatchMethod() { return crossmatchMethod; }
    public void setCrossmatchMethod(CrossMatchMethodEnum crossmatchMethod) { this.crossmatchMethod = crossmatchMethod; }

    public CrossMatchResultEnum getResult() { return result; }
    public void setResult(CrossMatchResultEnum result) { this.result = result; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public Instant getPerformedAt() { return performedAt; }
    public void setPerformedAt(Instant performedAt) { this.performedAt = performedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
