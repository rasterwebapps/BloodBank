package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.SignatureMeaningEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DigitalSignatureResponse(
    UUID id,
    UUID branchId,
    String entityType,
    UUID entityId,
    String signerId,
    String signerName,
    String signerRole,
    SignatureMeaningEnum signatureMeaning,
    String signatureHash,
    String signatureAlgorithm,
    Instant signedAt,
    String ipAddress,
    boolean valid,
    LocalDateTime createdAt
) {}
