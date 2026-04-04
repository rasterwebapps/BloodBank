package com.bloodbank.common.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecallInitiatedEvent(
    UUID recallId,
    UUID branchId,
    String reason,
    List<UUID> affectedUnitIds,
    Instant occurredAt
) {}
