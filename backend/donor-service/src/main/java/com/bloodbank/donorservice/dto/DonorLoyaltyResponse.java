package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.LoyaltyTierEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record DonorLoyaltyResponse(
        UUID id,
        UUID donorId,
        int pointsEarned,
        int pointsRedeemed,
        int pointsBalance,
        LoyaltyTierEnum tier,
        LocalDateTime lastActivityDate,
        UUID branchId,
        LocalDateTime createdAt
) implements Serializable {}
