package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.donorservice.enums.ReactionOutcomeEnum;

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
@Table(name = "collection_adverse_reactions")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class CollectionAdverseReaction extends BranchScopedEntity {

    @Column(name = "collection_id", nullable = false)
    private UUID collectionId;

    @Column(name = "reaction_type", nullable = false, length = 50)
    private String reactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private SeverityEnum severity;

    @Column(name = "onset_time")
    private Instant onsetTime;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "treatment_given", columnDefinition = "TEXT")
    private String treatmentGiven;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 30)
    private ReactionOutcomeEnum outcome;

    @Column(name = "reported_by", length = 255)
    private String reportedBy;

    public CollectionAdverseReaction() {}

    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public SeverityEnum getSeverity() { return severity; }
    public void setSeverity(SeverityEnum severity) { this.severity = severity; }

    public Instant getOnsetTime() { return onsetTime; }
    public void setOnsetTime(Instant onsetTime) { this.onsetTime = onsetTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTreatmentGiven() { return treatmentGiven; }
    public void setTreatmentGiven(String treatmentGiven) { this.treatmentGiven = treatmentGiven; }

    public ReactionOutcomeEnum getOutcome() { return outcome; }
    public void setOutcome(ReactionOutcomeEnum outcome) { this.outcome = outcome; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
}
