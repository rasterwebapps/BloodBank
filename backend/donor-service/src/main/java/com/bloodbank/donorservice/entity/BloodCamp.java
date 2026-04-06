package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.donorservice.enums.CampStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "blood_camps")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class BloodCamp extends BranchScopedEntity {

    @Column(name = "camp_code", nullable = false, unique = true, length = 30)
    private String campCode;

    @Column(name = "camp_name", nullable = false, length = 200)
    private String campName;

    @Column(name = "organizer_name", length = 200)
    private String organizerName;

    @Column(name = "organizer_contact", length = 100)
    private String organizerContact;

    @Column(name = "venue_name", nullable = false, length = 200)
    private String venueName;

    @Column(name = "venue_address", nullable = false, length = 500)
    private String venueAddress;

    @Column(name = "city_id")
    private UUID cityId;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "expected_donors")
    private Integer expectedDonors;

    @Column(name = "actual_donors")
    private Integer actualDonors;

    @Column(name = "total_collected", nullable = false)
    private int totalCollected;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampStatusEnum status;

    @Column(name = "coordinator_id", length = 255)
    private String coordinatorId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    protected BloodCamp() {}

    public String getCampCode() { return campCode; }
    public void setCampCode(String campCode) { this.campCode = campCode; }

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public String getOrganizerContact() { return organizerContact; }
    public void setOrganizerContact(String organizerContact) { this.organizerContact = organizerContact; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public UUID getCityId() { return cityId; }
    public void setCityId(UUID cityId) { this.cityId = cityId; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public Integer getExpectedDonors() { return expectedDonors; }
    public void setExpectedDonors(Integer expectedDonors) { this.expectedDonors = expectedDonors; }

    public Integer getActualDonors() { return actualDonors; }
    public void setActualDonors(Integer actualDonors) { this.actualDonors = actualDonors; }

    public int getTotalCollected() { return totalCollected; }
    public void setTotalCollected(int totalCollected) { this.totalCollected = totalCollected; }

    public CampStatusEnum getStatus() { return status; }
    public void setStatus(CampStatusEnum status) { this.status = status; }

    public String getCoordinatorId() { return coordinatorId; }
    public void setCoordinatorId(String coordinatorId) { this.coordinatorId = coordinatorId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
