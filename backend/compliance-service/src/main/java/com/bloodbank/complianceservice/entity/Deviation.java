package com.bloodbank.complianceservice.entity;

import com.bloodbank.complianceservice.enums.DeviationCategoryEnum;
import com.bloodbank.complianceservice.enums.DeviationSeverityEnum;
import com.bloodbank.complianceservice.enums.DeviationStatusEnum;
import com.bloodbank.complianceservice.enums.DeviationTypeEnum;
import com.bloodbank.common.model.BranchScopedEntity;

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
@Table(name = "deviations")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Deviation extends BranchScopedEntity {

    @Column(name = "deviation_number", nullable = false, unique = true, length = 30)
    private String deviationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "deviation_type", nullable = false, length = 30)
    private DeviationTypeEnum deviationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private DeviationSeverityEnum severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private DeviationCategoryEnum category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "detected_date", nullable = false)
    private Instant detectedDate;

    @Column(name = "detected_by")
    private String detectedBy;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "preventive_action", columnDefinition = "TEXT")
    private String preventiveAction;

    @Column(name = "sop_reference_id")
    private UUID sopReferenceId;

    @Column(name = "closure_date")
    private Instant closureDate;

    @Column(name = "closed_by")
    private String closedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeviationStatusEnum status = DeviationStatusEnum.OPEN;

    public Deviation() {}

    public String getDeviationNumber() { return deviationNumber; }
    public void setDeviationNumber(String deviationNumber) { this.deviationNumber = deviationNumber; }

    public DeviationTypeEnum getDeviationType() { return deviationType; }
    public void setDeviationType(DeviationTypeEnum deviationType) { this.deviationType = deviationType; }

    public DeviationSeverityEnum getSeverity() { return severity; }
    public void setSeverity(DeviationSeverityEnum severity) { this.severity = severity; }

    public DeviationCategoryEnum getCategory() { return category; }
    public void setCategory(DeviationCategoryEnum category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getDetectedDate() { return detectedDate; }
    public void setDetectedDate(Instant detectedDate) { this.detectedDate = detectedDate; }

    public String getDetectedBy() { return detectedBy; }
    public void setDetectedBy(String detectedBy) { this.detectedBy = detectedBy; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }

    public String getPreventiveAction() { return preventiveAction; }
    public void setPreventiveAction(String preventiveAction) { this.preventiveAction = preventiveAction; }

    public UUID getSopReferenceId() { return sopReferenceId; }
    public void setSopReferenceId(UUID sopReferenceId) { this.sopReferenceId = sopReferenceId; }

    public Instant getClosureDate() { return closureDate; }
    public void setClosureDate(Instant closureDate) { this.closureDate = closureDate; }

    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }

    public DeviationStatusEnum getStatus() { return status; }
    public void setStatus(DeviationStatusEnum status) { this.status = status; }
}
