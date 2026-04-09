package com.bloodbank.complianceservice.dto;

import com.bloodbank.complianceservice.enums.RecallSeverityEnum;
import com.bloodbank.complianceservice.enums.RecallTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RecallCreateRequest(
        @NotNull RecallTypeEnum recallType,
        @NotBlank @Size(max = 500) String recallReason,
        @NotNull RecallSeverityEnum severity,
        String initiatedBy,
        int affectedUnitsCount,
        UUID lookbackInvestigationId,
        String notes,
        @NotNull UUID branchId
) {}
