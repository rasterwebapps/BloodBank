package com.bloodbank.hospitalservice.dto;

import com.bloodbank.hospitalservice.enums.PriorityEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record HospitalRequestCreateRequest(
        @NotNull UUID hospitalId,
        @NotBlank String patientName,
        String patientId,
        @NotNull UUID patientBloodGroupId,
        @NotNull UUID componentTypeId,
        @Min(1) int unitsRequested,
        @NotNull PriorityEnum priority,
        Instant requiredBy,
        String clinicalIndication,
        UUID icdCodeId,
        String requestingDoctor,
        String doctorLicense,
        String notes,
        @NotNull UUID branchId
) {}
