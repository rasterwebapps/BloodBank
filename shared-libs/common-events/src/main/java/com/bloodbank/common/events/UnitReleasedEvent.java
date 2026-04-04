package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record UnitReleasedEvent(
    UUID bloodUnitId,
    UUID branchId,
    Instant occurredAt
) {}
