package com.bloodbank.transfusionservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.transfusionservice.enums.IssueStatusEnum;

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
@Table(name = "blood_issues")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class BloodIssue extends BranchScopedEntity {

    @Column(name = "issue_number", nullable = false, unique = true, length = 30)
    private String issueNumber;

    @Column(name = "crossmatch_request_id")
    private UUID crossmatchRequestId;

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "patient_name", nullable = false, length = 200)
    private String patientName;

    @Column(name = "patient_id", nullable = false, length = 50)
    private String patientId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "issued_to", nullable = false, length = 200)
    private String issuedTo;

    @Column(name = "issued_by", length = 255)
    private String issuedBy;

    @Column(name = "issue_date", nullable = false)
    private Instant issueDate;

    @Column(name = "return_date")
    private Instant returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IssueStatusEnum status;

    @Column(name = "return_reason", length = 255)
    private String returnReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public BloodIssue() {}

    public String getIssueNumber() { return issueNumber; }
    public void setIssueNumber(String issueNumber) { this.issueNumber = issueNumber; }

    public UUID getCrossmatchRequestId() { return crossmatchRequestId; }
    public void setCrossmatchRequestId(UUID crossmatchRequestId) { this.crossmatchRequestId = crossmatchRequestId; }

    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }

    public String getIssuedTo() { return issuedTo; }
    public void setIssuedTo(String issuedTo) { this.issuedTo = issuedTo; }

    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }

    public Instant getIssueDate() { return issueDate; }
    public void setIssueDate(Instant issueDate) { this.issueDate = issueDate; }

    public Instant getReturnDate() { return returnDate; }
    public void setReturnDate(Instant returnDate) { this.returnDate = returnDate; }

    public IssueStatusEnum getStatus() { return status; }
    public void setStatus(IssueStatusEnum status) { this.status = status; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
