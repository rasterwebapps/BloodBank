package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blood_components")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class BloodComponent extends BranchScopedEntity {

    @Column(name = "blood_unit_id")
    private UUID bloodUnitId;

    @Column(name = "component_type_id", nullable = false)
    private UUID componentTypeId;

    @Column(name = "component_number", unique = true, nullable = false)
    private String componentNumber;

    @Column(name = "blood_group_id")
    private UUID bloodGroupId;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "weight_grams")
    private BigDecimal weightGrams;

    @Column(name = "preparation_date")
    private Instant preparationDate;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComponentStatusEnum status;

    @Column(name = "storage_location_id")
    private UUID storageLocationId;

    @Column(name = "irradiated")
    private boolean irradiated;

    @Column(name = "leukoreduced")
    private boolean leukoreduced;

    protected BloodComponent() {}

    public BloodComponent(UUID bloodUnitId, UUID componentTypeId, String componentNumber,
                          UUID bloodGroupId, Integer volumeMl, Instant expiryDate) {
        this.bloodUnitId = bloodUnitId;
        this.componentTypeId = componentTypeId;
        this.componentNumber = componentNumber;
        this.bloodGroupId = bloodGroupId;
        this.volumeMl = volumeMl;
        this.preparationDate = Instant.now();
        this.expiryDate = expiryDate;
        this.status = ComponentStatusEnum.QUARANTINED;
    }

    public UUID getBloodUnitId() { return bloodUnitId; }
    public void setBloodUnitId(UUID bloodUnitId) { this.bloodUnitId = bloodUnitId; }

    public UUID getComponentTypeId() { return componentTypeId; }
    public void setComponentTypeId(UUID componentTypeId) { this.componentTypeId = componentTypeId; }

    public String getComponentNumber() { return componentNumber; }
    public void setComponentNumber(String componentNumber) { this.componentNumber = componentNumber; }

    public UUID getBloodGroupId() { return bloodGroupId; }
    public void setBloodGroupId(UUID bloodGroupId) { this.bloodGroupId = bloodGroupId; }

    public Integer getVolumeMl() { return volumeMl; }
    public void setVolumeMl(Integer volumeMl) { this.volumeMl = volumeMl; }

    public BigDecimal getWeightGrams() { return weightGrams; }
    public void setWeightGrams(BigDecimal weightGrams) { this.weightGrams = weightGrams; }

    public Instant getPreparationDate() { return preparationDate; }
    public void setPreparationDate(Instant preparationDate) { this.preparationDate = preparationDate; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public ComponentStatusEnum getStatus() { return status; }
    public void setStatus(ComponentStatusEnum status) { this.status = status; }

    public UUID getStorageLocationId() { return storageLocationId; }
    public void setStorageLocationId(UUID storageLocationId) { this.storageLocationId = storageLocationId; }

    public boolean isIrradiated() { return irradiated; }
    public void setIrradiated(boolean irradiated) { this.irradiated = irradiated; }

    public boolean isLeukoreduced() { return leukoreduced; }
    public void setLeukoreduced(boolean leukoreduced) { this.leukoreduced = leukoreduced; }
}
