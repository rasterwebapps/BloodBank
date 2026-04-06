package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.TransportBoxStatusEnum;
import com.bloodbank.inventoryservice.enums.TransportBoxTypeEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transport_boxes")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class TransportBox extends BranchScopedEntity {

    @Column(name = "box_code", unique = true, nullable = false)
    private String boxCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "box_type", nullable = false)
    private TransportBoxTypeEnum boxType;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "temperature_range")
    private String temperatureRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransportBoxStatusEnum status;

    @Column(name = "last_sanitized")
    private Instant lastSanitized;

    protected TransportBox() {}

    public TransportBox(String boxCode, TransportBoxTypeEnum boxType, Integer capacity,
                        String temperatureRange) {
        this.boxCode = boxCode;
        this.boxType = boxType;
        this.capacity = capacity;
        this.temperatureRange = temperatureRange;
        this.status = TransportBoxStatusEnum.AVAILABLE;
    }

    public String getBoxCode() { return boxCode; }
    public void setBoxCode(String boxCode) { this.boxCode = boxCode; }

    public TransportBoxTypeEnum getBoxType() { return boxType; }
    public void setBoxType(TransportBoxTypeEnum boxType) { this.boxType = boxType; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getTemperatureRange() { return temperatureRange; }
    public void setTemperatureRange(String temperatureRange) { this.temperatureRange = temperatureRange; }

    public TransportBoxStatusEnum getStatus() { return status; }
    public void setStatus(TransportBoxStatusEnum status) { this.status = status; }

    public Instant getLastSanitized() { return lastSanitized; }
    public void setLastSanitized(Instant lastSanitized) { this.lastSanitized = lastSanitized; }
}
