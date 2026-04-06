package com.bloodbank.donorservice.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record CampResourceResponse(
        UUID id,
        UUID campId,
        String resourceType,
        String resourceName,
        int quantity,
        String notes,
        UUID branchId,
        LocalDateTime createdAt
) implements Serializable {}
