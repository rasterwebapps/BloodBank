package com.bloodbank.requestmatchingservice.dto;

import com.bloodbank.requestmatchingservice.enums.DisasterSeverityEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterStatusEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DisasterEventResponse(
    UUID id,
    String eventCode,
    String eventName,
    DisasterTypeEnum eventType,
    DisasterSeverityEnum severity,
    String locationDescription,
    UUID cityId,
    Instant startDate,
    Instant endDate,
    Integer estimatedCasualties,
    Integer bloodUnitsNeeded,
    String coordinatorName,
    String coordinatorContact,
    DisasterStatusEnum status,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
