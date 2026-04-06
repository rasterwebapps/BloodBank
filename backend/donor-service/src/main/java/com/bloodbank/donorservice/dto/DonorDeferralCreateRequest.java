package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.DeferralTypeEnum;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record DonorDeferralCreateRequest(
        @NotNull UUID donorId,
        @NotNull UUID deferralReasonId,
        @NotNull DeferralTypeEnum deferralType,
        LocalDate reinstatementDate,
        String notes,
        String deferredBy,
        @NotNull UUID branchId
) {}
