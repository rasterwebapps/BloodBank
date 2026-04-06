package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.common.model.enums.CollectionStatusEnum;
import com.bloodbank.donorservice.enums.CollectionTypeEnum;
import com.bloodbank.donorservice.enums.DonationTypeEnum;

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
@Table(name = "collections")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Collection extends BranchScopedEntity {

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "health_record_id")
    private UUID healthRecordId;

    @Column(name = "collection_number", nullable = false, unique = true, length = 30)
    private String collectionNumber;

    @Column(name = "collection_date", nullable = false)
    private Instant collectionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", nullable = false, length = 30)
    private CollectionTypeEnum collectionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", nullable = false, length = 30)
    private DonationTypeEnum donationType;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "bag_type", length = 50)
    private String bagType;

    @Column(name = "bag_lot_number", length = 100)
    private String bagLotNumber;

    @Column(name = "phlebotomist_id", length = 255)
    private String phlebotomistId;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CollectionStatusEnum status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "camp_collection_id")
    private UUID campCollectionId;

    public Collection() {}

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public UUID getHealthRecordId() { return healthRecordId; }
    public void setHealthRecordId(UUID healthRecordId) { this.healthRecordId = healthRecordId; }

    public String getCollectionNumber() { return collectionNumber; }
    public void setCollectionNumber(String collectionNumber) { this.collectionNumber = collectionNumber; }

    public Instant getCollectionDate() { return collectionDate; }
    public void setCollectionDate(Instant collectionDate) { this.collectionDate = collectionDate; }

    public CollectionTypeEnum getCollectionType() { return collectionType; }
    public void setCollectionType(CollectionTypeEnum collectionType) { this.collectionType = collectionType; }

    public DonationTypeEnum getDonationType() { return donationType; }
    public void setDonationType(DonationTypeEnum donationType) { this.donationType = donationType; }

    public Integer getVolumeMl() { return volumeMl; }
    public void setVolumeMl(Integer volumeMl) { this.volumeMl = volumeMl; }

    public String getBagType() { return bagType; }
    public void setBagType(String bagType) { this.bagType = bagType; }

    public String getBagLotNumber() { return bagLotNumber; }
    public void setBagLotNumber(String bagLotNumber) { this.bagLotNumber = bagLotNumber; }

    public String getPhlebotomistId() { return phlebotomistId; }
    public void setPhlebotomistId(String phlebotomistId) { this.phlebotomistId = phlebotomistId; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public CollectionStatusEnum getStatus() { return status; }
    public void setStatus(CollectionStatusEnum status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UUID getCampCollectionId() { return campCollectionId; }
    public void setCampCollectionId(UUID campCollectionId) { this.campCollectionId = campCollectionId; }
}
