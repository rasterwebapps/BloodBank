package com.bloodbank.labservice.dto;

import com.bloodbank.labservice.enums.OrderPriorityEnum;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TestOrderCreateRequest(
        @NotNull UUID sampleId,
        @NotNull UUID collectionId,
        @NotNull UUID donorId,
        UUID panelId,
        @NotNull OrderPriorityEnum priority,
        String orderedBy,
        String notes,
        @NotNull UUID branchId
) {}
