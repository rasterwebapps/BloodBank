package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record TransfusionReactionEvent(
    UUID transfusionId,
    UUID bloodUnitId,
    UUID branchId,
    String severity,
    Instant occurredAt
) {}
