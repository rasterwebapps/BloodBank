package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cold_chain_logs")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class ColdChainLog extends BranchScopedEntity {

    @Column(name = "transport_request_id")
    private UUID transportRequestId;

    @Column(name = "storage_location_id")
    private UUID storageLocationId;

    @Column(name = "transport_box_id")
    private UUID transportBoxId;

    @Column(name = "temperature")
    private BigDecimal temperature;

    @Column(name = "humidity")
    private BigDecimal humidity;

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @Column(name = "is_within_range")
    private boolean isWithinRange;

    @Column(name = "alert_triggered")
    private boolean alertTriggered;

    @Column(name = "recorded_by")
    private String recordedBy;

    @Column(name = "notes")
    private String notes;

    protected ColdChainLog() {}

    public ColdChainLog(UUID transportRequestId, BigDecimal temperature, BigDecimal humidity,
                        String recordedBy) {
        this.transportRequestId = transportRequestId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.recordedBy = recordedBy;
        this.recordedAt = Instant.now();
    }

    public UUID getTransportRequestId() { return transportRequestId; }
    public void setTransportRequestId(UUID transportRequestId) { this.transportRequestId = transportRequestId; }

    public UUID getStorageLocationId() { return storageLocationId; }
    public void setStorageLocationId(UUID storageLocationId) { this.storageLocationId = storageLocationId; }

    public UUID getTransportBoxId() { return transportBoxId; }
    public void setTransportBoxId(UUID transportBoxId) { this.transportBoxId = transportBoxId; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }

    public boolean isWithinRange() { return isWithinRange; }
    public void setWithinRange(boolean withinRange) { isWithinRange = withinRange; }

    public boolean isAlertTriggered() { return alertTriggered; }
    public void setAlertTriggered(boolean alertTriggered) { this.alertTriggered = alertTriggered; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
