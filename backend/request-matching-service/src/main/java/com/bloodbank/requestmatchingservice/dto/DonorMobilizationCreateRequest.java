package com.bloodbank.requestmatchingservice.dto;

import com.bloodbank.requestmatchingservice.enums.MobilizationTypeEnum;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record DonorMobilizationCreateRequest(
    UUID disasterEventId,
    UUID emergencyRequestId,
    @NotNull UUID donorId,
    @NotNull MobilizationTypeEnum contactMethod,
    Instant scheduledDonationTime,
    String notes,
    @NotNull UUID branchId
) {}
