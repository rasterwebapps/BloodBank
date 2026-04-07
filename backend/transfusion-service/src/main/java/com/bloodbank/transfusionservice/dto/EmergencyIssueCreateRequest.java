package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.EmergencyTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EmergencyIssueCreateRequest(
    @NotNull UUID componentId,
    @NotBlank String patientName,
    @NotBlank String patientId,
    UUID hospitalId,
    @NotBlank String issuedTo,
    String issuedBy,
    @NotNull EmergencyTypeEnum emergencyType,
    @NotBlank String authorizationBy,
    String clinicalJustification,
    String notes,
    @NotNull UUID branchId
) {}
