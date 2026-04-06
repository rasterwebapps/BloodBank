package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.ReservationStatusEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "unit_reservations")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class UnitReservation extends BranchScopedEntity {

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "reserved_for")
    private String reservedFor;

    @Column(name = "reservation_date")
    private Instant reservationDate;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatusEnum status;

    @Column(name = "reserved_by")
    private String reservedBy;

    @Column(name = "notes")
    private String notes;

    protected UnitReservation() {}

    public UnitReservation(UUID componentId, String reservedFor, Instant expiryDate, String reservedBy) {
        this.componentId = componentId;
        this.reservedFor = reservedFor;
        this.reservationDate = Instant.now();
        this.expiryDate = expiryDate;
        this.reservedBy = reservedBy;
        this.status = ReservationStatusEnum.ACTIVE;
    }

    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }

    public String getReservedFor() { return reservedFor; }
    public void setReservedFor(String reservedFor) { this.reservedFor = reservedFor; }

    public Instant getReservationDate() { return reservationDate; }
    public void setReservationDate(Instant reservationDate) { this.reservationDate = reservationDate; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public ReservationStatusEnum getStatus() { return status; }
    public void setStatus(ReservationStatusEnum status) { this.status = status; }

    public String getReservedBy() { return reservedBy; }
    public void setReservedBy(String reservedBy) { this.reservedBy = reservedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
