package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.donorservice.enums.SampleStatusEnum;
import com.bloodbank.donorservice.enums.SampleTypeEnum;

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
@Table(name = "collection_samples")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class CollectionSample extends BranchScopedEntity {

    @Column(name = "collection_id", nullable = false)
    private UUID collectionId;

    @Column(name = "sample_number", nullable = false, unique = true, length = 30)
    private String sampleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_type", nullable = false, length = 30)
    private SampleTypeEnum sampleType;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SampleStatusEnum status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public CollectionSample() {}

    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }

    public String getSampleNumber() { return sampleNumber; }
    public void setSampleNumber(String sampleNumber) { this.sampleNumber = sampleNumber; }

    public SampleTypeEnum getSampleType() { return sampleType; }
    public void setSampleType(SampleTypeEnum sampleType) { this.sampleType = sampleType; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }

    public SampleStatusEnum getStatus() { return status; }
    public void setStatus(SampleStatusEnum status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
