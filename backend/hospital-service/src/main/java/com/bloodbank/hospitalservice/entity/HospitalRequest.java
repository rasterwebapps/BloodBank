package com.bloodbank.hospitalservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.hospitalservice.enums.HospitalRequestStatusEnum;
import com.bloodbank.hospitalservice.enums.PriorityEnum;

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
@Table(name = "hospital_requests")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class HospitalRequest extends BranchScopedEntity {

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "request_number", nullable = false, unique = true, length = 30)
    private String requestNumber;

    @Column(name = "patient_name", nullable = false, length = 200)
    private String patientName;

    @Column(name = "patient_id", length = 50)
    private String patientId;

    @Column(name = "patient_blood_group_id", nullable = false)
    private UUID patientBloodGroupId;

    @Column(name = "component_type_id", nullable = false)
    private UUID componentTypeId;

    @Column(name = "units_requested", nullable = false)
    private int unitsRequested;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PriorityEnum priority;

    @Column(name = "required_by")
    private Instant requiredBy;

    @Column(name = "clinical_indication", columnDefinition = "TEXT")
    private String clinicalIndication;

    @Column(name = "icd_code_id")
    private UUID icdCodeId;

    @Column(name = "requesting_doctor", length = 200)
    private String requestingDoctor;

    @Column(name = "doctor_license", length = 50)
    private String doctorLicense;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HospitalRequestStatusEnum status;

    @Column(name = "units_fulfilled", nullable = false)
    private int unitsFulfilled;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public HospitalRequest() {}

    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }

    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public UUID getPatientBloodGroupId() { return patientBloodGroupId; }
    public void setPatientBloodGroupId(UUID patientBloodGroupId) { this.patientBloodGroupId = patientBloodGroupId; }

    public UUID getComponentTypeId() { return componentTypeId; }
    public void setComponentTypeId(UUID componentTypeId) { this.componentTypeId = componentTypeId; }

    public int getUnitsRequested() { return unitsRequested; }
    public void setUnitsRequested(int unitsRequested) { this.unitsRequested = unitsRequested; }

    public PriorityEnum getPriority() { return priority; }
    public void setPriority(PriorityEnum priority) { this.priority = priority; }

    public Instant getRequiredBy() { return requiredBy; }
    public void setRequiredBy(Instant requiredBy) { this.requiredBy = requiredBy; }

    public String getClinicalIndication() { return clinicalIndication; }
    public void setClinicalIndication(String clinicalIndication) { this.clinicalIndication = clinicalIndication; }

    public UUID getIcdCodeId() { return icdCodeId; }
    public void setIcdCodeId(UUID icdCodeId) { this.icdCodeId = icdCodeId; }

    public String getRequestingDoctor() { return requestingDoctor; }
    public void setRequestingDoctor(String requestingDoctor) { this.requestingDoctor = requestingDoctor; }

    public String getDoctorLicense() { return doctorLicense; }
    public void setDoctorLicense(String doctorLicense) { this.doctorLicense = doctorLicense; }

    public HospitalRequestStatusEnum getStatus() { return status; }
    public void setStatus(HospitalRequestStatusEnum status) { this.status = status; }

    public int getUnitsFulfilled() { return unitsFulfilled; }
    public void setUnitsFulfilled(int unitsFulfilled) { this.unitsFulfilled = unitsFulfilled; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
