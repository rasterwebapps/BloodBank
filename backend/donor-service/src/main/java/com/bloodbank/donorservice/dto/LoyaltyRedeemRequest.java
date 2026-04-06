package com.bloodbank.donorservice.dto;

import jakarta.validation.constraints.NotNull;

public record LoyaltyRedeemRequest(
        @NotNull Integer points
) {}
