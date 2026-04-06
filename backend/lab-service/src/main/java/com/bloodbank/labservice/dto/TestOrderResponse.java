package com.bloodbank.labservice.dto;

import com.bloodbank.labservice.enums.OrderPriorityEnum;
import com.bloodbank.labservice.enums.OrderStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TestOrderResponse(
        UUID id,
        UUID sampleId,
        UUID collectionId,
        UUID donorId,
        UUID panelId,
        String orderNumber,
        Instant orderDate,
        OrderPriorityEnum priority,
        OrderStatusEnum status,
        String orderedBy,
        Instant completedAt,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
