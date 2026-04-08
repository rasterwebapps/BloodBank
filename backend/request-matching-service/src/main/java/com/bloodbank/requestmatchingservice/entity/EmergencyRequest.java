package com.bloodbank.requestmatchingservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.requestmatchingservice.enums.EmergencyPriorityEnum;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;

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
@Table(name = "emergency_requests")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class EmergencyRequest extends BranchScopedEntity {

    @Column(name = "request_number", nullable = false, unique = true, length = 30)
    private String requestNumber;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "blood_group_id", nullable = false)
    private UUID bloodGroupId;

    @Column(name = "component_type_id", nullable = false)
    private UUID componentTypeId;

    @Column(name = "units_needed", nullable = false)
    private int unitsNeeded;

    @Column(name = "units_fulfilled", nullable = false)
    private int unitsFulfilled;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private EmergencyPriorityEnum priority;

    @Column(name = "patient_name", length = 200)
    private String patientName;

    @Column(name = "clinical_summary", columnDefinition = "TEXT")
    private String clinicalSummary;

    @Column(name = "requesting_doctor", length = 200)
    private String requestingDoctor;

    @Column(name = "required_by", nullable = false)
    private Instant requiredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmergencyStatusEnum status;

    @Column(name = "broadcast_sent", nullable = false)
    private boolean broadcastSent;

    @Column(name = "disaster_event_id")
    private UUID disasterEventId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public EmergencyRequest() {}

    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }

    public UUID getBloodGroupId() { return bloodGroupId; }
    public void setBloodGroupId(UUID bloodGroupId) { this.bloodGroupId = bloodGroupId; }

    public UUID getComponentTypeId() { return componentTypeId; }
    public void setComponentTypeId(UUID componentTypeId) { this.componentTypeId = componentTypeId; }

    public int getUnitsNeeded() { return unitsNeeded; }
    public void setUnitsNeeded(int unitsNeeded) { this.unitsNeeded = unitsNeeded; }

    public int getUnitsFulfilled() { return unitsFulfilled; }
    public void setUnitsFulfilled(int unitsFulfilled) { this.unitsFulfilled = unitsFulfilled; }

    public EmergencyPriorityEnum getPriority() { return priority; }
    public void setPriority(EmergencyPriorityEnum priority) { this.priority = priority; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getClinicalSummary() { return clinicalSummary; }
    public void setClinicalSummary(String clinicalSummary) { this.clinicalSummary = clinicalSummary; }

    public String getRequestingDoctor() { return requestingDoctor; }
    public void setRequestingDoctor(String requestingDoctor) { this.requestingDoctor = requestingDoctor; }

    public Instant getRequiredBy() { return requiredBy; }
    public void setRequiredBy(Instant requiredBy) { this.requiredBy = requiredBy; }

    public EmergencyStatusEnum getStatus() { return status; }
    public void setStatus(EmergencyStatusEnum status) { this.status = status; }

    public boolean isBroadcastSent() { return broadcastSent; }
    public void setBroadcastSent(boolean broadcastSent) { this.broadcastSent = broadcastSent; }

    public UUID getDisasterEventId() { return disasterEventId; }
    public void setDisasterEventId(UUID disasterEventId) { this.disasterEventId = disasterEventId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
