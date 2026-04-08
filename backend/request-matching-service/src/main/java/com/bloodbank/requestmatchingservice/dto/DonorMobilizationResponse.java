package com.bloodbank.requestmatchingservice.dto;

import com.bloodbank.requestmatchingservice.enums.MobilizationStatusEnum;
import com.bloodbank.requestmatchingservice.enums.MobilizationTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DonorMobilizationResponse(
    UUID id,
    UUID disasterEventId,
    UUID emergencyRequestId,
    UUID donorId,
    MobilizationTypeEnum contactMethod,
    Instant contactedAt,
    MobilizationStatusEnum response,
    Instant responseAt,
    Instant scheduledDonationTime,
    boolean donationCompleted,
    UUID collectionId,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
