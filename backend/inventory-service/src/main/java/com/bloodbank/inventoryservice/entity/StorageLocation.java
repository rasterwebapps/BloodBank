package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.StorageLocationStatusEnum;
import com.bloodbank.inventoryservice.enums.StorageLocationTypeEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "storage_locations")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class StorageLocation extends BranchScopedEntity {

    @Column(name = "location_code", nullable = false)
    private String locationCode;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private StorageLocationTypeEnum locationType;

    @Column(name = "temperature_min")
    private BigDecimal temperatureMin;

    @Column(name = "temperature_max")
    private BigDecimal temperatureMax;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "current_count")
    private int currentCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StorageLocationStatusEnum status;

    protected StorageLocation() {}

    public StorageLocation(String locationCode, String locationName, StorageLocationTypeEnum locationType,
                           BigDecimal temperatureMin, BigDecimal temperatureMax, Integer capacity) {
        this.locationCode = locationCode;
        this.locationName = locationName;
        this.locationType = locationType;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.capacity = capacity;
        this.currentCount = 0;
        this.status = StorageLocationStatusEnum.ACTIVE;
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public StorageLocationTypeEnum getLocationType() { return locationType; }
    public void setLocationType(StorageLocationTypeEnum locationType) { this.locationType = locationType; }

    public BigDecimal getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(BigDecimal temperatureMin) { this.temperatureMin = temperatureMin; }

    public BigDecimal getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(BigDecimal temperatureMax) { this.temperatureMax = temperatureMax; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }

    public StorageLocationStatusEnum getStatus() { return status; }
    public void setStatus(StorageLocationStatusEnum status) { this.status = status; }
}
