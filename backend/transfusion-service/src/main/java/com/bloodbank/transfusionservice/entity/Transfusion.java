package com.bloodbank.transfusionservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.TransfusionStatusEnum;
import com.bloodbank.transfusionservice.enums.TransfusionOutcomeEnum;

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
@Table(name = "transfusions")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Transfusion extends BranchScopedEntity {

    @Column(name = "blood_issue_id", nullable = false)
    private UUID bloodIssueId;

    @Column(name = "patient_name", nullable = false, length = 200)
    private String patientName;

    @Column(name = "patient_id", nullable = false, length = 50)
    private String patientId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "transfusion_start")
    private Instant transfusionStart;

    @Column(name = "transfusion_end")
    private Instant transfusionEnd;

    @Column(name = "volume_transfused_ml")
    private Integer volumeTransfusedMl;

    @Column(name = "administered_by", length = 255)
    private String administeredBy;

    @Column(name = "verified_by", length = 255)
    private String verifiedBy;

    @Column(name = "pre_vital_signs", columnDefinition = "TEXT")
    private String preVitalSigns;

    @Column(name = "post_vital_signs", columnDefinition = "TEXT")
    private String postVitalSigns;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransfusionStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 20)
    private TransfusionOutcomeEnum outcome;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public Transfusion() {}

    public UUID getBloodIssueId() { return bloodIssueId; }
    public void setBloodIssueId(UUID bloodIssueId) { this.bloodIssueId = bloodIssueId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }

    public Instant getTransfusionStart() { return transfusionStart; }
    public void setTransfusionStart(Instant transfusionStart) { this.transfusionStart = transfusionStart; }

    public Instant getTransfusionEnd() { return transfusionEnd; }
    public void setTransfusionEnd(Instant transfusionEnd) { this.transfusionEnd = transfusionEnd; }

    public Integer getVolumeTransfusedMl() { return volumeTransfusedMl; }
    public void setVolumeTransfusedMl(Integer volumeTransfusedMl) { this.volumeTransfusedMl = volumeTransfusedMl; }

    public String getAdministeredBy() { return administeredBy; }
    public void setAdministeredBy(String administeredBy) { this.administeredBy = administeredBy; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getPreVitalSigns() { return preVitalSigns; }
    public void setPreVitalSigns(String preVitalSigns) { this.preVitalSigns = preVitalSigns; }

    public String getPostVitalSigns() { return postVitalSigns; }
    public void setPostVitalSigns(String postVitalSigns) { this.postVitalSigns = postVitalSigns; }

    public TransfusionStatusEnum getStatus() { return status; }
    public void setStatus(TransfusionStatusEnum status) { this.status = status; }

    public TransfusionOutcomeEnum getOutcome() { return outcome; }
    public void setOutcome(TransfusionOutcomeEnum outcome) { this.outcome = outcome; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
