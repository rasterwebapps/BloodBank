package com.bloodbank.donorservice.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record DonorHealthRecordCreateRequest(
        @NotNull UUID donorId,
        BigDecimal weightKg,
        BigDecimal heightCm,
        Integer bloodPressureSystolic,
        Integer bloodPressureDiastolic,
        Integer pulseRate,
        BigDecimal temperatureCelsius,
        BigDecimal hemoglobinGdl,
        String notes,
        String screenedBy,
        @NotNull UUID branchId
) {}
