package com.bloodbank.requestmatchingservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.requestmatchingservice.enums.DisasterSeverityEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterStatusEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "disaster_events")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class DisasterEvent extends BranchScopedEntity {

    @Column(name = "event_code", nullable = false, unique = true, length = 30)
    private String eventCode;

    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private DisasterTypeEnum eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private DisasterSeverityEnum severity;

    @Column(name = "location_description", length = 500)
    private String locationDescription;

    @Column(name = "city_id")
    private UUID cityId;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "estimated_casualties")
    private Integer estimatedCasualties;

    @Column(name = "blood_units_needed")
    private Integer bloodUnitsNeeded;

    @Column(name = "coordinator_name", length = 200)
    private String coordinatorName;

    @Column(name = "coordinator_contact", length = 100)
    private String coordinatorContact;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DisasterStatusEnum status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public DisasterEvent() {}

    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public DisasterTypeEnum getEventType() { return eventType; }
    public void setEventType(DisasterTypeEnum eventType) { this.eventType = eventType; }

    public DisasterSeverityEnum getSeverity() { return severity; }
    public void setSeverity(DisasterSeverityEnum severity) { this.severity = severity; }

    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }

    public UUID getCityId() { return cityId; }
    public void setCityId(UUID cityId) { this.cityId = cityId; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public Integer getEstimatedCasualties() { return estimatedCasualties; }
    public void setEstimatedCasualties(Integer estimatedCasualties) { this.estimatedCasualties = estimatedCasualties; }

    public Integer getBloodUnitsNeeded() { return bloodUnitsNeeded; }
    public void setBloodUnitsNeeded(Integer bloodUnitsNeeded) { this.bloodUnitsNeeded = bloodUnitsNeeded; }

    public String getCoordinatorName() { return coordinatorName; }
    public void setCoordinatorName(String coordinatorName) { this.coordinatorName = coordinatorName; }

    public String getCoordinatorContact() { return coordinatorContact; }
    public void setCoordinatorContact(String coordinatorContact) { this.coordinatorContact = coordinatorContact; }

    public DisasterStatusEnum getStatus() { return status; }
    public void setStatus(DisasterStatusEnum status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
