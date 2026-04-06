package com.bloodbank.donorservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CollectionCompleteRequest(
        @NotNull Integer volumeMl,
        LocalDateTime endTime,
        String notes
) {}
