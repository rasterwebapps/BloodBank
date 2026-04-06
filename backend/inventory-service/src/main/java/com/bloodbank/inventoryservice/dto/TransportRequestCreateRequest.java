package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.TransportTypeEnum;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record TransportRequestCreateRequest(
    @NotNull UUID sourceBranchId,
    UUID destinationBranchId,
    UUID destinationHospitalId,
    UUID transportBoxId,
    @NotNull TransportTypeEnum transportType,
    @NotNull Integer unitsCount,
    Instant expectedDeliveryTime,
    String driverName,
    String driverContact,
    String vehicleNumber,
    String notes,
    @NotNull UUID branchId
) {}
