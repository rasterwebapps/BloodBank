package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.TransportStatusEnum;
import com.bloodbank.inventoryservice.enums.TransportTypeEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transport_requests")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class TransportRequest extends BranchScopedEntity {

    @Column(name = "request_number", unique = true, nullable = false)
    private String requestNumber;

    @Column(name = "source_branch_id", nullable = false)
    private UUID sourceBranchId;

    @Column(name = "destination_branch_id")
    private UUID destinationBranchId;

    @Column(name = "destination_hospital_id")
    private UUID destinationHospitalId;

    @Column(name = "transport_box_id")
    private UUID transportBoxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false)
    private TransportTypeEnum transportType;

    @Column(name = "units_count")
    private int unitsCount;

    @Column(name = "pickup_time")
    private Instant pickupTime;

    @Column(name = "expected_delivery_time")
    private Instant expectedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private Instant actualDeliveryTime;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_contact")
    private String driverContact;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransportStatusEnum status;

    @Column(name = "notes")
    private String notes;

    protected TransportRequest() {}

    public TransportRequest(String requestNumber, UUID sourceBranchId, TransportTypeEnum transportType,
                            int unitsCount) {
        this.requestNumber = requestNumber;
        this.sourceBranchId = sourceBranchId;
        this.transportType = transportType;
        this.unitsCount = unitsCount;
        this.status = TransportStatusEnum.REQUESTED;
    }

    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

    public UUID getSourceBranchId() { return sourceBranchId; }
    public void setSourceBranchId(UUID sourceBranchId) { this.sourceBranchId = sourceBranchId; }

    public UUID getDestinationBranchId() { return destinationBranchId; }
    public void setDestinationBranchId(UUID destinationBranchId) { this.destinationBranchId = destinationBranchId; }

    public UUID getDestinationHospitalId() { return destinationHospitalId; }
    public void setDestinationHospitalId(UUID destinationHospitalId) { this.destinationHospitalId = destinationHospitalId; }

    public UUID getTransportBoxId() { return transportBoxId; }
    public void setTransportBoxId(UUID transportBoxId) { this.transportBoxId = transportBoxId; }

    public TransportTypeEnum getTransportType() { return transportType; }
    public void setTransportType(TransportTypeEnum transportType) { this.transportType = transportType; }

    public int getUnitsCount() { return unitsCount; }
    public void setUnitsCount(int unitsCount) { this.unitsCount = unitsCount; }

    public Instant getPickupTime() { return pickupTime; }
    public void setPickupTime(Instant pickupTime) { this.pickupTime = pickupTime; }

    public Instant getExpectedDeliveryTime() { return expectedDeliveryTime; }
    public void setExpectedDeliveryTime(Instant expectedDeliveryTime) { this.expectedDeliveryTime = expectedDeliveryTime; }

    public Instant getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(Instant actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverContact() { return driverContact; }
    public void setDriverContact(String driverContact) { this.driverContact = driverContact; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public TransportStatusEnum getStatus() { return status; }
    public void setStatus(TransportStatusEnum status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
