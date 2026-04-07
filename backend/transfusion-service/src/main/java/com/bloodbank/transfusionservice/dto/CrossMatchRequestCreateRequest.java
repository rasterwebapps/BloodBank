package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.PriorityEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CrossMatchRequestCreateRequest(
    @NotBlank String patientName,
    @NotBlank String patientId,
    @NotNull UUID patientBloodGroupId,
    UUID hospitalId,
    @NotBlank String requestingDoctor,
    String clinicalDiagnosis,
    UUID icdCodeId,
    @Min(1) int unitsRequested,
    @NotNull UUID componentTypeId,
    @NotNull PriorityEnum priority,
    Instant requiredBy,
    String notes,
    @NotNull UUID branchId
) {}
