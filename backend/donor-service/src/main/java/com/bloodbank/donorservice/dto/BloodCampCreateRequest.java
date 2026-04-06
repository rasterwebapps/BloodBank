package com.bloodbank.donorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BloodCampCreateRequest(
        @NotBlank String campName,
        String organizerName,
        String organizerContact,
        @NotBlank String venueName,
        @NotBlank String venueAddress,
        UUID cityId,
        BigDecimal latitude,
        BigDecimal longitude,
        @NotNull LocalDate scheduledDate,
        Integer expectedDonors,
        String coordinatorId,
        String notes,
        @NotNull UUID branchId
) {}
