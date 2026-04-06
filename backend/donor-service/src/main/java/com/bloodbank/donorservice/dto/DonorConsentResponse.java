package com.bloodbank.donorservice.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record DonorConsentResponse(
        UUID id,
        UUID donorId,
        String consentType,
        boolean consentGiven,
        LocalDateTime consentDate,
        LocalDateTime expiryDate,
        String consentText,
        String signatureReference,
        String ipAddress,
        LocalDateTime revokedAt,
        UUID branchId,
        LocalDateTime createdAt
) implements Serializable {}
