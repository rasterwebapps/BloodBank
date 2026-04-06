package com.bloodbank.donorservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CampDonorCreateRequest(
        @NotNull UUID campId,
        @NotNull UUID donorId,
        @NotNull UUID branchId
) {}
