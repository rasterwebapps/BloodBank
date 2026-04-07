package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.OutputFormatEnum;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReportMetadataCreateRequest(
    UUID branchId,
    @NotBlank String reportCode,
    @NotBlank String reportName,
    @NotNull ReportTypeEnum reportType,
    String description,
    String queryDefinition,
    String parameters,
    OutputFormatEnum outputFormat
) {}
