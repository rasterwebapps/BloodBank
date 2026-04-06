package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.donorservice.enums.DeferralStatusEnum;
import com.bloodbank.donorservice.enums.DeferralTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "donor_deferrals")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class DonorDeferral extends BranchScopedEntity {

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "deferral_reason_id", nullable = false)
    private UUID deferralReasonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "deferral_type", nullable = false, length = 20)
    private DeferralTypeEnum deferralType;

    @Column(name = "deferral_date", nullable = false)
    private LocalDate deferralDate;

    @Column(name = "reinstatement_date")
    private LocalDate reinstatementDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deferred_by", length = 255)
    private String deferredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeferralStatusEnum status;

    protected DonorDeferral() {}

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public UUID getDeferralReasonId() { return deferralReasonId; }
    public void setDeferralReasonId(UUID deferralReasonId) { this.deferralReasonId = deferralReasonId; }

    public DeferralTypeEnum getDeferralType() { return deferralType; }
    public void setDeferralType(DeferralTypeEnum deferralType) { this.deferralType = deferralType; }

    public LocalDate getDeferralDate() { return deferralDate; }
    public void setDeferralDate(LocalDate deferralDate) { this.deferralDate = deferralDate; }

    public LocalDate getReinstatementDate() { return reinstatementDate; }
    public void setReinstatementDate(LocalDate reinstatementDate) { this.reinstatementDate = reinstatementDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDeferredBy() { return deferredBy; }
    public void setDeferredBy(String deferredBy) { this.deferredBy = deferredBy; }

    public DeferralStatusEnum getStatus() { return status; }
    public void setStatus(DeferralStatusEnum status) { this.status = status; }
}
