package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.DeferralStatusEnum;
import com.bloodbank.donorservice.enums.DeferralTypeEnum;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DonorDeferralResponse(
        UUID id,
        UUID donorId,
        UUID deferralReasonId,
        DeferralTypeEnum deferralType,
        LocalDate deferralDate,
        LocalDate reinstatementDate,
        String notes,
        String deferredBy,
        DeferralStatusEnum status,
        UUID branchId,
        LocalDateTime createdAt
) implements Serializable {}
