package com.bloodbank.hospitalservice.dto;

import com.bloodbank.hospitalservice.enums.HospitalRequestStatusEnum;
import com.bloodbank.hospitalservice.enums.PriorityEnum;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record HospitalRequestResponse(
        UUID id,
        UUID hospitalId,
        String requestNumber,
        String patientName,
        String patientId,
        UUID patientBloodGroupId,
        UUID componentTypeId,
        int unitsRequested,
        PriorityEnum priority,
        Instant requiredBy,
        String clinicalIndication,
        UUID icdCodeId,
        String requestingDoctor,
        String doctorLicense,
        HospitalRequestStatusEnum status,
        int unitsFulfilled,
        String rejectionReason,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {}
