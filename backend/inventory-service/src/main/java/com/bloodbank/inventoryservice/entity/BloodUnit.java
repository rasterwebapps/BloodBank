package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.inventoryservice.enums.TtiStatusEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blood_units")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class BloodUnit extends BranchScopedEntity {

    @Column(name = "collection_id")
    private UUID collectionId;

    @Column(name = "donor_id")
    private UUID donorId;

    @Column(name = "unit_number", unique = true, nullable = false)
    private String unitNumber;

    @Column(name = "blood_group_id", nullable = false)
    private UUID bloodGroupId;

    @Column(name = "rh_factor")
    private String rhFactor;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "collection_date")
    private Instant collectionDate;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BloodUnitStatusEnum status;

    @Column(name = "storage_location_id")
    private UUID storageLocationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tti_status")
    private TtiStatusEnum ttiStatus;

    protected BloodUnit() {}

    public BloodUnit(UUID collectionId, UUID donorId, String unitNumber, UUID bloodGroupId,
                     String rhFactor, Integer volumeMl, Instant collectionDate, Instant expiryDate) {
        this.collectionId = collectionId;
        this.donorId = donorId;
        this.unitNumber = unitNumber;
        this.bloodGroupId = bloodGroupId;
        this.rhFactor = rhFactor;
        this.volumeMl = volumeMl;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.status = BloodUnitStatusEnum.QUARANTINED;
        this.ttiStatus = TtiStatusEnum.PENDING;
    }

    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }

    public UUID getBloodGroupId() { return bloodGroupId; }
    public void setBloodGroupId(UUID bloodGroupId) { this.bloodGroupId = bloodGroupId; }

    public String getRhFactor() { return rhFactor; }
    public void setRhFactor(String rhFactor) { this.rhFactor = rhFactor; }

    public Integer getVolumeMl() { return volumeMl; }
    public void setVolumeMl(Integer volumeMl) { this.volumeMl = volumeMl; }

    public Instant getCollectionDate() { return collectionDate; }
    public void setCollectionDate(Instant collectionDate) { this.collectionDate = collectionDate; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public BloodUnitStatusEnum getStatus() { return status; }
    public void setStatus(BloodUnitStatusEnum status) { this.status = status; }

    public UUID getStorageLocationId() { return storageLocationId; }
    public void setStorageLocationId(UUID storageLocationId) { this.storageLocationId = storageLocationId; }

    public TtiStatusEnum getTtiStatus() { return ttiStatus; }
    public void setTtiStatus(TtiStatusEnum ttiStatus) { this.ttiStatus = ttiStatus; }
}
