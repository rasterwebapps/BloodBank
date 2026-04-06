package com.bloodbank.donorservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DonorHealthRecordResponse(
        UUID id,
        UUID donorId,
        LocalDateTime screeningDate,
        BigDecimal weightKg,
        BigDecimal heightCm,
        Integer bloodPressureSystolic,
        Integer bloodPressureDiastolic,
        Integer pulseRate,
        BigDecimal temperatureCelsius,
        BigDecimal hemoglobinGdl,
        boolean isEligible,
        String notes,
        String screenedBy,
        UUID branchId,
        LocalDateTime createdAt
) implements Serializable {}
