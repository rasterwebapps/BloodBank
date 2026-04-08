package com.bloodbank.requestmatchingservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.requestmatchingservice.enums.MobilizationStatusEnum;
import com.bloodbank.requestmatchingservice.enums.MobilizationTypeEnum;

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
@Table(name = "donor_mobilizations")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class DonorMobilization extends BranchScopedEntity {

    @Column(name = "disaster_event_id")
    private UUID disasterEventId;

    @Column(name = "emergency_request_id")
    private UUID emergencyRequestId;

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_method", nullable = false, length = 20)
    private MobilizationTypeEnum contactMethod;

    @Column(name = "contacted_at", nullable = false)
    private Instant contactedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "response", length = 20)
    private MobilizationStatusEnum response;

    @Column(name = "response_at")
    private Instant responseAt;

    @Column(name = "scheduled_donation_time")
    private Instant scheduledDonationTime;

    @Column(name = "donation_completed", nullable = false)
    private boolean donationCompleted;

    @Column(name = "collection_id")
    private UUID collectionId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public DonorMobilization() {}

    public UUID getDisasterEventId() { return disasterEventId; }
    public void setDisasterEventId(UUID disasterEventId) { this.disasterEventId = disasterEventId; }

    public UUID getEmergencyRequestId() { return emergencyRequestId; }
    public void setEmergencyRequestId(UUID emergencyRequestId) { this.emergencyRequestId = emergencyRequestId; }

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public MobilizationTypeEnum getContactMethod() { return contactMethod; }
    public void setContactMethod(MobilizationTypeEnum contactMethod) { this.contactMethod = contactMethod; }

    public Instant getContactedAt() { return contactedAt; }
    public void setContactedAt(Instant contactedAt) { this.contactedAt = contactedAt; }

    public MobilizationStatusEnum getResponse() { return response; }
    public void setResponse(MobilizationStatusEnum response) { this.response = response; }

    public Instant getResponseAt() { return responseAt; }
    public void setResponseAt(Instant responseAt) { this.responseAt = responseAt; }

    public Instant getScheduledDonationTime() { return scheduledDonationTime; }
    public void setScheduledDonationTime(Instant scheduledDonationTime) { this.scheduledDonationTime = scheduledDonationTime; }

    public boolean isDonationCompleted() { return donationCompleted; }
    public void setDonationCompleted(boolean donationCompleted) { this.donationCompleted = donationCompleted; }

    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
