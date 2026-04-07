package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.WidgetTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record DashboardWidgetResponse(
    UUID id,
    UUID branchId,
    String widgetCode,
    String widgetName,
    WidgetTypeEnum widgetType,
    String dataSource,
    String queryDefinition,
    String displayConfig,
    int refreshIntervalSec,
    String roleAccess,
    int sortOrder,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
