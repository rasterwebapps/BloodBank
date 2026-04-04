package com.bloodbank.branchservice.entity;

import com.bloodbank.common.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "component_types")
public class ComponentType extends BaseEntity {

    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "shelf_life_days", nullable = false)
    private int shelfLifeDays;

    @Column(name = "storage_temp_min", precision = 5, scale = 2)
    private BigDecimal storageTempMin;

    @Column(name = "storage_temp_max", precision = 5, scale = 2)
    private BigDecimal storageTempMax;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected ComponentType() {}

    public ComponentType(String typeCode, String typeName, int shelfLifeDays) {
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.shelfLifeDays = shelfLifeDays;
    }

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getShelfLifeDays() { return shelfLifeDays; }
    public void setShelfLifeDays(int shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }

    public BigDecimal getStorageTempMin() { return storageTempMin; }
    public void setStorageTempMin(BigDecimal storageTempMin) { this.storageTempMin = storageTempMin; }

    public BigDecimal getStorageTempMax() { return storageTempMax; }
    public void setStorageTempMax(BigDecimal storageTempMax) { this.storageTempMax = storageTempMax; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
