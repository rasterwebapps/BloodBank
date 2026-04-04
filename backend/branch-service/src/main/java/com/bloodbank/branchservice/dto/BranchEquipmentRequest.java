package com.bloodbank.branchservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BranchEquipmentRequest(
    @NotBlank @Size(max = 200) String equipmentName,
    @NotBlank String equipmentType,
    @Size(max = 100) String serialNumber,
    @Size(max = 200) String manufacturer,
    @Size(max = 100) String model,
    LocalDate purchaseDate,
    LocalDate lastMaintenanceDate,
    LocalDate nextMaintenanceDate,
    String status
) {}
