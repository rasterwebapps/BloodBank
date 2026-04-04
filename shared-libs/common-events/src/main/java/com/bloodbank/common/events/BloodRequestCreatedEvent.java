package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record BloodRequestCreatedEvent(
    UUID requestId,
    UUID hospitalId,
    UUID branchId,
    String bloodGroup,
    int quantity,
    Instant occurredAt
) {}
