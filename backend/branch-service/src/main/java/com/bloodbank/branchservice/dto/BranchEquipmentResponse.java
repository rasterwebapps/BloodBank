package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public record BranchEquipmentResponse(
    UUID id,
    UUID branchId,
    String equipmentName,
    String equipmentType,
    String serialNumber,
    String manufacturer,
    String model,
    LocalDate purchaseDate,
    LocalDate lastMaintenanceDate,
    LocalDate nextMaintenanceDate,
    String status
) implements Serializable {}
