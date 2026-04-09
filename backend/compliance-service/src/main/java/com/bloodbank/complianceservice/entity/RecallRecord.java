package com.bloodbank.complianceservice.entity;

import com.bloodbank.complianceservice.enums.RecallSeverityEnum;
import com.bloodbank.complianceservice.enums.RecallStatusEnum;
import com.bloodbank.complianceservice.enums.RecallTypeEnum;
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
@Table(name = "recall_records")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class RecallRecord extends BranchScopedEntity {

    @Column(name = "recall_number", nullable = false, unique = true, length = 30)
    private String recallNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "recall_type", nullable = false, length = 30)
    private RecallTypeEnum recallType;

    @Column(name = "recall_reason", nullable = false, length = 500)
    private String recallReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private RecallSeverityEnum severity;

    @Column(name = "initiated_date", nullable = false)
    private Instant initiatedDate;

    @Column(name = "initiated_by")
    private String initiatedBy;

    @Column(name = "affected_units_count", nullable = false)
    private int affectedUnitsCount = 0;

    @Column(name = "units_recovered", nullable = false)
    private int unitsRecovered = 0;

    @Column(name = "units_transfused", nullable = false)
    private int unitsTransfused = 0;

    @Column(name = "notification_sent", nullable = false)
    private boolean notificationSent = false;

    @Column(name = "lookback_investigation_id")
    private UUID lookbackInvestigationId;

    @Column(name = "closure_date")
    private Instant closureDate;

    @Column(name = "closed_by")
    private String closedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecallStatusEnum status = RecallStatusEnum.INITIATED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public RecallRecord() {}

    public String getRecallNumber() { return recallNumber; }
    public void setRecallNumber(String recallNumber) { this.recallNumber = recallNumber; }

    public RecallTypeEnum getRecallType() { return recallType; }
    public void setRecallType(RecallTypeEnum recallType) { this.recallType = recallType; }

    public String getRecallReason() { return recallReason; }
    public void setRecallReason(String recallReason) { this.recallReason = recallReason; }

    public RecallSeverityEnum getSeverity() { return severity; }
    public void setSeverity(RecallSeverityEnum severity) { this.severity = severity; }

    public Instant getInitiatedDate() { return initiatedDate; }
    public void setInitiatedDate(Instant initiatedDate) { this.initiatedDate = initiatedDate; }

    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }

    public int getAffectedUnitsCount() { return affectedUnitsCount; }
    public void setAffectedUnitsCount(int affectedUnitsCount) { this.affectedUnitsCount = affectedUnitsCount; }

    public int getUnitsRecovered() { return unitsRecovered; }
    public void setUnitsRecovered(int unitsRecovered) { this.unitsRecovered = unitsRecovered; }

    public int getUnitsTransfused() { return unitsTransfused; }
    public void setUnitsTransfused(int unitsTransfused) { this.unitsTransfused = unitsTransfused; }

    public boolean isNotificationSent() { return notificationSent; }
    public void setNotificationSent(boolean notificationSent) { this.notificationSent = notificationSent; }

    public UUID getLookbackInvestigationId() { return lookbackInvestigationId; }
    public void setLookbackInvestigationId(UUID lookbackInvestigationId) { this.lookbackInvestigationId = lookbackInvestigationId; }

    public Instant getClosureDate() { return closureDate; }
    public void setClosureDate(Instant closureDate) { this.closureDate = closureDate; }

    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }

    public RecallStatusEnum getStatus() { return status; }
    public void setStatus(RecallStatusEnum status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
