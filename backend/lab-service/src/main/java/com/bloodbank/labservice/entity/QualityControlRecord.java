package com.bloodbank.labservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.labservice.enums.QcLevelEnum;
import com.bloodbank.labservice.enums.QcStatusEnum;

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
@Table(name = "quality_control_records")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class QualityControlRecord extends BranchScopedEntity {

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Column(name = "qc_date", nullable = false)
    private Instant qcDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "qc_level", nullable = false, length = 20)
    private QcLevelEnum qcLevel;

    @Column(name = "test_name", nullable = false, length = 100)
    private String testName;

    @Column(name = "expected_value", length = 100)
    private String expectedValue;

    @Column(name = "actual_value", length = 100)
    private String actualValue;

    @Column(name = "is_within_range", nullable = false)
    private boolean isWithinRange;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "performed_by")
    private String performedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QcStatusEnum status = QcStatusEnum.COMPLETED;

    protected QualityControlRecord() {}

    public QualityControlRecord(UUID instrumentId, QcLevelEnum qcLevel,
                                String testName, boolean isWithinRange) {
        this.instrumentId = instrumentId;
        this.qcLevel = qcLevel;
        this.testName = testName;
        this.isWithinRange = isWithinRange;
        this.qcDate = Instant.now();
        this.status = QcStatusEnum.COMPLETED;
    }

    public UUID getInstrumentId() { return instrumentId; }
    public void setInstrumentId(UUID instrumentId) { this.instrumentId = instrumentId; }

    public Instant getQcDate() { return qcDate; }
    public void setQcDate(Instant qcDate) { this.qcDate = qcDate; }

    public QcLevelEnum getQcLevel() { return qcLevel; }
    public void setQcLevel(QcLevelEnum qcLevel) { this.qcLevel = qcLevel; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public String getActualValue() { return actualValue; }
    public void setActualValue(String actualValue) { this.actualValue = actualValue; }

    public boolean isWithinRange() { return isWithinRange; }
    public void setWithinRange(boolean withinRange) { this.isWithinRange = withinRange; }

    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public QcStatusEnum getStatus() { return status; }
    public void setStatus(QcStatusEnum status) { this.status = status; }
}
