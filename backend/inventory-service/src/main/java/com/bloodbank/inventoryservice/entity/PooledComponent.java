package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pooled_components")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class PooledComponent extends BranchScopedEntity {

    @Column(name = "pool_number", unique = true, nullable = false)
    private String poolNumber;

    @Column(name = "component_type_id", nullable = false)
    private UUID componentTypeId;

    @Column(name = "blood_group_id")
    private UUID bloodGroupId;

    @Column(name = "total_volume_ml")
    private Integer totalVolumeMl;

    @Column(name = "number_of_units")
    private int numberOfUnits;

    @Column(name = "preparation_date")
    private Instant preparationDate;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComponentStatusEnum status;

    @Column(name = "storage_location_id")
    private UUID storageLocationId;

    @Column(name = "prepared_by")
    private String preparedBy;

    @Column(name = "notes")
    private String notes;

    protected PooledComponent() {}

    public PooledComponent(String poolNumber, UUID componentTypeId, UUID bloodGroupId,
                           Integer totalVolumeMl, int numberOfUnits, Instant expiryDate, String preparedBy) {
        this.poolNumber = poolNumber;
        this.componentTypeId = componentTypeId;
        this.bloodGroupId = bloodGroupId;
        this.totalVolumeMl = totalVolumeMl;
        this.numberOfUnits = numberOfUnits;
        this.preparationDate = Instant.now();
        this.expiryDate = expiryDate;
        this.status = ComponentStatusEnum.AVAILABLE;
        this.preparedBy = preparedBy;
    }

    public String getPoolNumber() { return poolNumber; }
    public void setPoolNumber(String poolNumber) { this.poolNumber = poolNumber; }

    public UUID getComponentTypeId() { return componentTypeId; }
    public void setComponentTypeId(UUID componentTypeId) { this.componentTypeId = componentTypeId; }

    public UUID getBloodGroupId() { return bloodGroupId; }
    public void setBloodGroupId(UUID bloodGroupId) { this.bloodGroupId = bloodGroupId; }

    public Integer getTotalVolumeMl() { return totalVolumeMl; }
    public void setTotalVolumeMl(Integer totalVolumeMl) { this.totalVolumeMl = totalVolumeMl; }

    public int getNumberOfUnits() { return numberOfUnits; }
    public void setNumberOfUnits(int numberOfUnits) { this.numberOfUnits = numberOfUnits; }

    public Instant getPreparationDate() { return preparationDate; }
    public void setPreparationDate(Instant preparationDate) { this.preparationDate = preparationDate; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public ComponentStatusEnum getStatus() { return status; }
    public void setStatus(ComponentStatusEnum status) { this.status = status; }

    public UUID getStorageLocationId() { return storageLocationId; }
    public void setStorageLocationId(UUID storageLocationId) { this.storageLocationId = storageLocationId; }

    public String getPreparedBy() { return preparedBy; }
    public void setPreparedBy(String preparedBy) { this.preparedBy = preparedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
