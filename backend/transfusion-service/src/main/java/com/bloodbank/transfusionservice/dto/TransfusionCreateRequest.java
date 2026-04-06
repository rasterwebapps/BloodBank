package com.bloodbank.transfusionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransfusionCreateRequest(
    @NotNull UUID bloodIssueId,
    @NotBlank String patientName,
    @NotBlank String patientId,
    UUID hospitalId,
    @NotBlank String administeredBy,
    String verifiedBy,
    String preVitalSigns,
    String notes,
    @NotNull UUID branchId
) {}
