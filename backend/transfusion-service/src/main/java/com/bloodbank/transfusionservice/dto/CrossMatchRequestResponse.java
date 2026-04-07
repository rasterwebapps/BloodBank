package com.bloodbank.transfusionservice.dto;

import com.bloodbank.common.model.enums.RequestStatusEnum;
import com.bloodbank.transfusionservice.enums.PriorityEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record CrossMatchRequestResponse(
    UUID id,
    String requestNumber,
    String patientName,
    String patientId,
    UUID patientBloodGroupId,
    UUID hospitalId,
    String requestingDoctor,
    String clinicalDiagnosis,
    UUID icdCodeId,
    int unitsRequested,
    UUID componentTypeId,
    PriorityEnum priority,
    Instant requiredBy,
    RequestStatusEnum status,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
