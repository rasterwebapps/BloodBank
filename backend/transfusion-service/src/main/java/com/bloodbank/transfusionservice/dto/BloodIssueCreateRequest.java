package com.bloodbank.transfusionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BloodIssueCreateRequest(
    UUID crossmatchRequestId,
    @NotNull UUID componentId,
    @NotBlank String patientName,
    @NotBlank String patientId,
    UUID hospitalId,
    @NotBlank String issuedTo,
    String issuedBy,
    String notes,
    @NotNull UUID branchId
) {}
