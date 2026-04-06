package com.bloodbank.transfusionservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.transfusionservice.enums.CrossMatchResultEnum;
import com.bloodbank.transfusionservice.enums.EmergencyTypeEnum;

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
@Table(name = "emergency_issues")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class EmergencyIssue extends BranchScopedEntity {

    @Column(name = "blood_issue_id", nullable = false)
    private UUID bloodIssueId;

    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_type", nullable = false, length = 30)
    private EmergencyTypeEnum emergencyType;

    @Column(name = "authorization_by", nullable = false, length = 255)
    private String authorizationBy;

    @Column(name = "authorization_time", nullable = false)
    private Instant authorizationTime;

    @Column(name = "clinical_justification", columnDefinition = "TEXT")
    private String clinicalJustification;

    @Column(name = "post_crossmatch_done", nullable = false)
    private boolean postCrossmatchDone;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_crossmatch_result", length = 20)
    private CrossMatchResultEnum postCrossmatchResult;

    public EmergencyIssue() {}

    public UUID getBloodIssueId() { return bloodIssueId; }
    public void setBloodIssueId(UUID bloodIssueId) { this.bloodIssueId = bloodIssueId; }

    public EmergencyTypeEnum getEmergencyType() { return emergencyType; }
    public void setEmergencyType(EmergencyTypeEnum emergencyType) { this.emergencyType = emergencyType; }

    public String getAuthorizationBy() { return authorizationBy; }
    public void setAuthorizationBy(String authorizationBy) { this.authorizationBy = authorizationBy; }

    public Instant getAuthorizationTime() { return authorizationTime; }
    public void setAuthorizationTime(Instant authorizationTime) { this.authorizationTime = authorizationTime; }

    public String getClinicalJustification() { return clinicalJustification; }
    public void setClinicalJustification(String clinicalJustification) { this.clinicalJustification = clinicalJustification; }

    public boolean isPostCrossmatchDone() { return postCrossmatchDone; }
    public void setPostCrossmatchDone(boolean postCrossmatchDone) { this.postCrossmatchDone = postCrossmatchDone; }

    public CrossMatchResultEnum getPostCrossmatchResult() { return postCrossmatchResult; }
    public void setPostCrossmatchResult(CrossMatchResultEnum postCrossmatchResult) { this.postCrossmatchResult = postCrossmatchResult; }
}
