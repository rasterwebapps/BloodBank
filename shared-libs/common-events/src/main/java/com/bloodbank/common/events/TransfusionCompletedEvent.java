package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record TransfusionCompletedEvent(
    UUID transfusionId,
    UUID bloodUnitId,
    UUID branchId,
    Instant occurredAt
) {}
