package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record DonationCompletedEvent(
    UUID donationId,
    UUID donorId,
    UUID branchId,
    Instant occurredAt
) {}
