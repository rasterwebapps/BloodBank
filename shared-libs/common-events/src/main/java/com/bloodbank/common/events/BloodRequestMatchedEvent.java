package com.bloodbank.common.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BloodRequestMatchedEvent(
    UUID requestId,
    UUID branchId,
    List<UUID> matchedUnitIds,
    Instant occurredAt
) {}
