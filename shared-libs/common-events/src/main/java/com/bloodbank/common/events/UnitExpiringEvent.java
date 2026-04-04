package com.bloodbank.common.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UnitExpiringEvent(
    UUID bloodUnitId,
    UUID branchId,
    LocalDate expiryDate,
    Instant occurredAt
) {}
