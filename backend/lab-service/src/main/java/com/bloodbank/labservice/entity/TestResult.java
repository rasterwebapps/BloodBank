package com.bloodbank.labservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.TestResultEnum;

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
@Table(name = "test_results")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class TestResult extends BranchScopedEntity {

    @Column(name = "test_order_id", nullable = false)
    private UUID testOrderId;

    @Column(name = "test_name", nullable = false, length = 100)
    private String testName;

    @Column(name = "test_method", length = 100)
    private String testMethod;

    @Column(name = "result_value", length = 255)
    private String resultValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false, length = 20)
    private TestResultEnum resultStatus;

    @Column(name = "is_abnormal", nullable = false)
    private boolean isAbnormal;

    @Column(name = "unit_of_measure", length = 50)
    private String unitOfMeasure;

    @Column(name = "reference_range", length = 100)
    private String referenceRange;

    @Column(name = "instrument_id")
    private UUID instrumentId;

    @Column(name = "tested_by")
    private String testedBy;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "tested_at")
    private Instant testedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    protected TestResult() {}

    public TestResult(UUID testOrderId, String testName, TestResultEnum resultStatus) {
        this.testOrderId = testOrderId;
        this.testName = testName;
        this.resultStatus = resultStatus;
    }

    public UUID getTestOrderId() { return testOrderId; }
    public void setTestOrderId(UUID testOrderId) { this.testOrderId = testOrderId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getTestMethod() { return testMethod; }
    public void setTestMethod(String testMethod) { this.testMethod = testMethod; }

    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }

    public TestResultEnum getResultStatus() { return resultStatus; }
    public void setResultStatus(TestResultEnum resultStatus) { this.resultStatus = resultStatus; }

    public boolean isAbnormal() { return isAbnormal; }
    public void setAbnormal(boolean abnormal) { this.isAbnormal = abnormal; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }

    public UUID getInstrumentId() { return instrumentId; }
    public void setInstrumentId(UUID instrumentId) { this.instrumentId = instrumentId; }

    public String getTestedBy() { return testedBy; }
    public void setTestedBy(String testedBy) { this.testedBy = testedBy; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public Instant getTestedAt() { return testedAt; }
    public void setTestedAt(Instant testedAt) { this.testedAt = testedAt; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
