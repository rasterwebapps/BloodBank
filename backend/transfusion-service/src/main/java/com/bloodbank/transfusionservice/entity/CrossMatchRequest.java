package com.bloodbank.transfusionservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.RequestStatusEnum;
import com.bloodbank.transfusionservice.enums.PriorityEnum;

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
@Table(name = "crossmatch_requests")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class CrossMatchRequest extends BranchScopedEntity {

    @Column(name = "request_number", nullable = false, unique = true, length = 30)
    private String requestNumber;

    @Column(name = "patient_name", nullable = false, length = 200)
    private String patientName;

    @Column(name = "patient_id", nullable = false, length = 50)
    private String patientId;

    @Column(name = "patient_blood_group_id")
    private UUID patientBloodGroupId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "requesting_doctor", nullable = false, length = 200)
    private String requestingDoctor;

    @Column(name = "clinical_diagnosis", columnDefinition = "TEXT")
    private String clinicalDiagnosis;

    @Column(name = "icd_code_id")
    private UUID icdCodeId;

    @Column(name = "units_requested", nullable = false)
    private int unitsRequested;

    @Column(name = "component_type_id", nullable = false)
    private UUID componentTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PriorityEnum priority;

    @Column(name = "required_by")
    private Instant requiredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatusEnum status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public CrossMatchRequest() {}

    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public UUID getPatientBloodGroupId() { return patientBloodGroupId; }
    public void setPatientBloodGroupId(UUID patientBloodGroupId) { this.patientBloodGroupId = patientBloodGroupId; }

    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }

    public String getRequestingDoctor() { return requestingDoctor; }
    public void setRequestingDoctor(String requestingDoctor) { this.requestingDoctor = requestingDoctor; }

    public String getClinicalDiagnosis() { return clinicalDiagnosis; }
    public void setClinicalDiagnosis(String clinicalDiagnosis) { this.clinicalDiagnosis = clinicalDiagnosis; }

    public UUID getIcdCodeId() { return icdCodeId; }
    public void setIcdCodeId(UUID icdCodeId) { this.icdCodeId = icdCodeId; }

    public int getUnitsRequested() { return unitsRequested; }
    public void setUnitsRequested(int unitsRequested) { this.unitsRequested = unitsRequested; }

    public UUID getComponentTypeId() { return componentTypeId; }
    public void setComponentTypeId(UUID componentTypeId) { this.componentTypeId = componentTypeId; }

    public PriorityEnum getPriority() { return priority; }
    public void setPriority(PriorityEnum priority) { this.priority = priority; }

    public Instant getRequiredBy() { return requiredBy; }
    public void setRequiredBy(Instant requiredBy) { this.requiredBy = requiredBy; }

    public RequestStatusEnum getStatus() { return status; }
    public void setStatus(RequestStatusEnum status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
