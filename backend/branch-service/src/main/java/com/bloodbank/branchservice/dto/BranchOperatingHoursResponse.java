package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

public record BranchOperatingHoursResponse(
    UUID id,
    UUID branchId,
    String dayOfWeek,
    LocalTime openTime,
    LocalTime closeTime,
    boolean isClosed
) implements Serializable {}
