package com.bloodbank.branchservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record BranchOperatingHoursRequest(
    @NotBlank String dayOfWeek,
    @NotNull LocalTime openTime,
    @NotNull LocalTime closeTime,
    boolean isClosed
) {}
