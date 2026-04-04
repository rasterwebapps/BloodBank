package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record BloodStockUpdatedEvent(
    UUID branchId,
    String bloodGroup,
    String componentType,
    int quantity,
    Instant occurredAt
) {}
