package com.bloodbank.transfusionservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.transfusionservice.enums.ReactionOutcomeEnum;

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
@Table(name = "transfusion_reactions")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class TransfusionReaction extends BranchScopedEntity {

    @Column(name = "transfusion_id", nullable = false)
    private UUID transfusionId;

    @Column(name = "reaction_type_id", nullable = false)
    private UUID reactionTypeId;

    @Column(name = "onset_time", nullable = false)
    private Instant onsetTime;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private SeverityEnum severity;

    @Column(name = "treatment_given", columnDefinition = "TEXT")
    private String treatmentGiven;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 30)
    private ReactionOutcomeEnum outcome;

    @Column(name = "reported_by", length = 255)
    private String reportedBy;

    public TransfusionReaction() {}

    public UUID getTransfusionId() { return transfusionId; }
    public void setTransfusionId(UUID transfusionId) { this.transfusionId = transfusionId; }

    public UUID getReactionTypeId() { return reactionTypeId; }
    public void setReactionTypeId(UUID reactionTypeId) { this.reactionTypeId = reactionTypeId; }

    public Instant getOnsetTime() { return onsetTime; }
    public void setOnsetTime(Instant onsetTime) { this.onsetTime = onsetTime; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public SeverityEnum getSeverity() { return severity; }
    public void setSeverity(SeverityEnum severity) { this.severity = severity; }

    public String getTreatmentGiven() { return treatmentGiven; }
    public void setTreatmentGiven(String treatmentGiven) { this.treatmentGiven = treatmentGiven; }

    public ReactionOutcomeEnum getOutcome() { return outcome; }
    public void setOutcome(ReactionOutcomeEnum outcome) { this.outcome = outcome; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
}
