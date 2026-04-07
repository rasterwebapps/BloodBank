package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.OutputFormatEnum;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportMetadataResponse(
    UUID id,
    UUID branchId,
    String reportCode,
    String reportName,
    ReportTypeEnum reportType,
    String description,
    String queryDefinition,
    String parameters,
    OutputFormatEnum outputFormat,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
