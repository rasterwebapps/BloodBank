package com.bloodbank.donorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DonorConsentCreateRequest(
        @NotNull UUID donorId,
        @NotBlank String consentType,
        @NotNull Boolean consentGiven,
        String consentText,
        String signatureReference,
        String ipAddress,
        @NotNull UUID branchId
) {}
