package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record CampCompletedEvent(
    UUID campId,
    UUID branchId,
    int totalCollections,
    Instant occurredAt
) {}
