package com.bloodbank.reportingservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.reportingservice.enums.ScheduleStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "report_schedules")
public class ReportSchedule extends BaseEntity {

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "schedule_name", nullable = false, length = 200)
    private String scheduleName;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Column(name = "recipients", columnDefinition = "TEXT")
    private String recipients;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_run_status", length = 20)
    private ScheduleStatusEnum lastRunStatus;

    protected ReportSchedule() {}

    public ReportSchedule(UUID reportId, String scheduleName, String cronExpression) {
        this.reportId = reportId;
        this.scheduleName = scheduleName;
        this.cronExpression = cronExpression;
    }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public UUID getReportId() { return reportId; }
    public void setReportId(UUID reportId) { this.reportId = reportId; }

    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public String getRecipients() { return recipients; }
    public void setRecipients(String recipients) { this.recipients = recipients; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(Instant lastRunAt) { this.lastRunAt = lastRunAt; }

    public Instant getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(Instant nextRunAt) { this.nextRunAt = nextRunAt; }

    public ScheduleStatusEnum getLastRunStatus() { return lastRunStatus; }
    public void setLastRunStatus(ScheduleStatusEnum lastRunStatus) { this.lastRunStatus = lastRunStatus; }
}
