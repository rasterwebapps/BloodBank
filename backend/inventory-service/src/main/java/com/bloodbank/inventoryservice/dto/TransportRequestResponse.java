package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.TransportStatusEnum;
import com.bloodbank.inventoryservice.enums.TransportTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransportRequestResponse(
    UUID id,
    String requestNumber,
    UUID sourceBranchId,
    UUID destinationBranchId,
    UUID destinationHospitalId,
    UUID transportBoxId,
    TransportTypeEnum transportType,
    int unitsCount,
    Instant pickupTime,
    Instant expectedDeliveryTime,
    Instant actualDeliveryTime,
    String driverName,
    String driverContact,
    String vehicleNumber,
    TransportStatusEnum status,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
