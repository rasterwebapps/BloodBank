package com.bloodbank.requestmatchingservice.dto;

import com.bloodbank.requestmatchingservice.enums.EmergencyPriorityEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record EmergencyRequestCreateRequest(
    UUID hospitalId,
    @NotNull UUID bloodGroupId,
    @NotNull UUID componentTypeId,
    @Min(1) int unitsNeeded,
    @NotNull EmergencyPriorityEnum priority,
    String patientName,
    String clinicalSummary,
    String requestingDoctor,
    @NotNull Instant requiredBy,
    UUID disasterEventId,
    String notes,
    @NotNull UUID branchId
) {}
