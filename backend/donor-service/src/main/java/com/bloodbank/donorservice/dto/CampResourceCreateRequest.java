package com.bloodbank.donorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CampResourceCreateRequest(
        @NotNull UUID campId,
        @NotBlank String resourceType,
        @NotBlank String resourceName,
        Integer quantity,
        String notes,
        @NotNull UUID branchId
) {}
