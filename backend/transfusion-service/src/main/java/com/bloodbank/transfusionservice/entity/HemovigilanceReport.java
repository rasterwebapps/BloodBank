package com.bloodbank.transfusionservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.transfusionservice.enums.HemovigilanceStatusEnum;
import com.bloodbank.transfusionservice.enums.ImputabilityEnum;

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
@Table(name = "hemovigilance_reports")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class HemovigilanceReport extends BranchScopedEntity {

    @Column(name = "transfusion_reaction_id", nullable = false)
    private UUID transfusionReactionId;

    @Column(name = "report_number", nullable = false, unique = true, length = 30)
    private String reportNumber;

    @Column(name = "report_date", nullable = false)
    private Instant reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "imputability", length = 20)
    private ImputabilityEnum imputability;

    @Column(name = "reporter_name", nullable = false, length = 200)
    private String reporterName;

    @Column(name = "reporter_designation", length = 100)
    private String reporterDesignation;

    @Column(name = "investigation_summary", columnDefinition = "TEXT")
    private String investigationSummary;

    @Column(name = "corrective_actions", columnDefinition = "TEXT")
    private String correctiveActions;

    @Column(name = "reported_to_authority", nullable = false)
    private boolean reportedToAuthority;

    @Column(name = "authority_report_date")
    private Instant authorityReportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HemovigilanceStatusEnum status;

    public HemovigilanceReport() {}

    public UUID getTransfusionReactionId() { return transfusionReactionId; }
    public void setTransfusionReactionId(UUID transfusionReactionId) { this.transfusionReactionId = transfusionReactionId; }

    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }

    public Instant getReportDate() { return reportDate; }
    public void setReportDate(Instant reportDate) { this.reportDate = reportDate; }

    public ImputabilityEnum getImputability() { return imputability; }
    public void setImputability(ImputabilityEnum imputability) { this.imputability = imputability; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterDesignation() { return reporterDesignation; }
    public void setReporterDesignation(String reporterDesignation) { this.reporterDesignation = reporterDesignation; }

    public String getInvestigationSummary() { return investigationSummary; }
    public void setInvestigationSummary(String investigationSummary) { this.investigationSummary = investigationSummary; }

    public String getCorrectiveActions() { return correctiveActions; }
    public void setCorrectiveActions(String correctiveActions) { this.correctiveActions = correctiveActions; }

    public boolean isReportedToAuthority() { return reportedToAuthority; }
    public void setReportedToAuthority(boolean reportedToAuthority) { this.reportedToAuthority = reportedToAuthority; }

    public Instant getAuthorityReportDate() { return authorityReportDate; }
    public void setAuthorityReportDate(Instant authorityReportDate) { this.authorityReportDate = authorityReportDate; }

    public HemovigilanceStatusEnum getStatus() { return status; }
    public void setStatus(HemovigilanceStatusEnum status) { this.status = status; }
}
