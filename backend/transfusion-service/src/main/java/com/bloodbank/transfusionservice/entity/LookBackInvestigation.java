package com.bloodbank.transfusionservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.transfusionservice.enums.InfectionTypeEnum;
import com.bloodbank.transfusionservice.enums.LookBackStatusEnum;

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
@Table(name = "lookback_investigations")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class LookBackInvestigation extends BranchScopedEntity {

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "trigger_test_result_id")
    private UUID triggerTestResultId;

    @Column(name = "investigation_number", nullable = false, unique = true, length = 30)
    private String investigationNumber;

    @Column(name = "investigation_date", nullable = false)
    private Instant investigationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "infection_type", nullable = false, length = 50)
    private InfectionTypeEnum infectionType;

    @Column(name = "affected_units_count", nullable = false)
    private int affectedUnitsCount;

    @Column(name = "recipients_traced", nullable = false)
    private int recipientsTraced;

    @Column(name = "recipients_notified", nullable = false)
    private int recipientsNotified;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LookBackStatusEnum status;

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "corrective_actions", columnDefinition = "TEXT")
    private String correctiveActions;

    public LookBackInvestigation() {}

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public UUID getTriggerTestResultId() { return triggerTestResultId; }
    public void setTriggerTestResultId(UUID triggerTestResultId) { this.triggerTestResultId = triggerTestResultId; }

    public String getInvestigationNumber() { return investigationNumber; }
    public void setInvestigationNumber(String investigationNumber) { this.investigationNumber = investigationNumber; }

    public Instant getInvestigationDate() { return investigationDate; }
    public void setInvestigationDate(Instant investigationDate) { this.investigationDate = investigationDate; }

    public InfectionTypeEnum getInfectionType() { return infectionType; }
    public void setInfectionType(InfectionTypeEnum infectionType) { this.infectionType = infectionType; }

    public int getAffectedUnitsCount() { return affectedUnitsCount; }
    public void setAffectedUnitsCount(int affectedUnitsCount) { this.affectedUnitsCount = affectedUnitsCount; }

    public int getRecipientsTraced() { return recipientsTraced; }
    public void setRecipientsTraced(int recipientsTraced) { this.recipientsTraced = recipientsTraced; }

    public int getRecipientsNotified() { return recipientsNotified; }
    public void setRecipientsNotified(int recipientsNotified) { this.recipientsNotified = recipientsNotified; }

    public LookBackStatusEnum getStatus() { return status; }
    public void setStatus(LookBackStatusEnum status) { this.status = status; }

    public String getFindings() { return findings; }
    public void setFindings(String findings) { this.findings = findings; }

    public String getCorrectiveActions() { return correctiveActions; }
    public void setCorrectiveActions(String correctiveActions) { this.correctiveActions = correctiveActions; }
}
