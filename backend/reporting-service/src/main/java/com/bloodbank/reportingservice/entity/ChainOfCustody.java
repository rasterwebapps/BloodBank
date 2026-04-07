package com.bloodbank.reportingservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.reportingservice.enums.CustodyEventEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chain_of_custody")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class ChainOfCustody extends BranchScopedEntity {

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "custody_event", nullable = false, length = 50)
    private CustodyEventEnum custodyEvent;

    @Column(name = "from_location", length = 200)
    private String fromLocation;

    @Column(name = "to_location", length = 200)
    private String toLocation;

    @Column(name = "handled_by", nullable = false)
    private String handledBy;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    protected ChainOfCustody() {}

    public ChainOfCustody(String entityType, UUID entityId, CustodyEventEnum custodyEvent,
                          String handledBy, Instant eventTime) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.custodyEvent = custodyEvent;
        this.handledBy = handledBy;
        this.eventTime = eventTime;
    }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public CustodyEventEnum getCustodyEvent() { return custodyEvent; }
    public void setCustodyEvent(CustodyEventEnum custodyEvent) { this.custodyEvent = custodyEvent; }

    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }

    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public Instant getEventTime() { return eventTime; }
    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
