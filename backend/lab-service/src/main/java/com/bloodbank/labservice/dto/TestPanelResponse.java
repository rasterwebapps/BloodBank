package com.bloodbank.labservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestPanelResponse(
        UUID id,
        String panelCode,
        String panelName,
        String description,
        String testNames,
        boolean isMandatory,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
