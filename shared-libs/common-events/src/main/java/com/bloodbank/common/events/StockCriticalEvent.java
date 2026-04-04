package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record StockCriticalEvent(
    UUID branchId,
    String bloodGroup,
    String componentType,
    int currentStock,
    int minimumStock,
    Instant occurredAt
) {}
