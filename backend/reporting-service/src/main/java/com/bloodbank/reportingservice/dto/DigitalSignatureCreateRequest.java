package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.SignatureMeaningEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DigitalSignatureCreateRequest(
    UUID branchId,
    @NotBlank String entityType,
    @NotNull UUID entityId,
    @NotBlank String signerId,
    @NotBlank String signerName,
    String signerRole,
    @NotNull SignatureMeaningEnum signatureMeaning,
    @NotBlank String signatureHash,
    String signatureAlgorithm,
    String ipAddress
) {}
