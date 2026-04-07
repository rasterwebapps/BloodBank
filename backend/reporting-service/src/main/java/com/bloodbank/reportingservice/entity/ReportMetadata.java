package com.bloodbank.reportingservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.reportingservice.enums.OutputFormatEnum;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "report_metadata")
public class ReportMetadata extends BaseEntity {

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "report_code", nullable = false, unique = true, length = 50)
    private String reportCode;

    @Column(name = "report_name", nullable = false, length = 200)
    private String reportName;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportTypeEnum reportType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "query_definition", columnDefinition = "TEXT")
    private String queryDefinition;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_format", length = 20)
    private OutputFormatEnum outputFormat = OutputFormatEnum.PDF;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected ReportMetadata() {}

    public ReportMetadata(String reportCode, String reportName, ReportTypeEnum reportType) {
        this.reportCode = reportCode;
        this.reportName = reportName;
        this.reportType = reportType;
    }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public ReportTypeEnum getReportType() { return reportType; }
    public void setReportType(ReportTypeEnum reportType) { this.reportType = reportType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQueryDefinition() { return queryDefinition; }
    public void setQueryDefinition(String queryDefinition) { this.queryDefinition = queryDefinition; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public OutputFormatEnum getOutputFormat() { return outputFormat; }
    public void setOutputFormat(OutputFormatEnum outputFormat) { this.outputFormat = outputFormat; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
