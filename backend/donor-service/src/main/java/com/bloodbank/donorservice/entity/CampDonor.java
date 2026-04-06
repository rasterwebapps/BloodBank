package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.donorservice.enums.CampDonorStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "camp_donors", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"camp_id", "donor_id"})
})
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class CampDonor extends BranchScopedEntity {

    @Column(name = "camp_id", nullable = false)
    private UUID campId;

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "registration_time", nullable = false)
    private Instant registrationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampDonorStatusEnum status;

    protected CampDonor() {}

    public UUID getCampId() { return campId; }
    public void setCampId(UUID campId) { this.campId = campId; }

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public Instant getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(Instant registrationTime) { this.registrationTime = registrationTime; }

    public CampDonorStatusEnum getStatus() { return status; }
    public void setStatus(CampDonorStatusEnum status) { this.status = status; }
}
