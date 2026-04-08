package com.bloodbank.requestmatchingservice.dto;

import com.bloodbank.requestmatchingservice.enums.EmergencyPriorityEnum;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmergencyRequestResponse(
    UUID id,
    String requestNumber,
    UUID hospitalId,
    UUID bloodGroupId,
    UUID componentTypeId,
    int unitsNeeded,
    int unitsFulfilled,
    EmergencyPriorityEnum priority,
    String patientName,
    String clinicalSummary,
    String requestingDoctor,
    Instant requiredBy,
    EmergencyStatusEnum status,
    boolean broadcastSent,
    UUID disasterEventId,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
