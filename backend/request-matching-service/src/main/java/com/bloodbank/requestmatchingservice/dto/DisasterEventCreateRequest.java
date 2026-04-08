package com.bloodbank.requestmatchingservice.dto;

import com.bloodbank.requestmatchingservice.enums.DisasterSeverityEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record DisasterEventCreateRequest(
    @NotBlank String eventName,
    @NotNull DisasterTypeEnum eventType,
    @NotNull DisasterSeverityEnum severity,
    String locationDescription,
    UUID cityId,
    Instant startDate,
    Integer estimatedCasualties,
    Integer bloodUnitsNeeded,
    String coordinatorName,
    String coordinatorContact,
    String notes,
    @NotNull UUID branchId
) {}
