package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.WidgetTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DashboardWidgetCreateRequest(
    UUID branchId,
    @NotBlank String widgetCode,
    @NotBlank String widgetName,
    @NotNull WidgetTypeEnum widgetType,
    String dataSource,
    String queryDefinition,
    String displayConfig,
    Integer refreshIntervalSec,
    String roleAccess,
    Integer sortOrder
) {}
