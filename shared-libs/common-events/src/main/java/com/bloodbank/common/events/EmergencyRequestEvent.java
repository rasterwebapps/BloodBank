package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record EmergencyRequestEvent(
    UUID requestId,
    UUID branchId,
    String bloodGroup,
    String severity,
    Instant occurredAt
) {}
