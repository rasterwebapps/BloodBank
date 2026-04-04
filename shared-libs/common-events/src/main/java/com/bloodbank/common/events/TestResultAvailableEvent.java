package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record TestResultAvailableEvent(
    UUID testOrderId,
    UUID bloodUnitId,
    UUID branchId,
    Instant occurredAt
) {}
