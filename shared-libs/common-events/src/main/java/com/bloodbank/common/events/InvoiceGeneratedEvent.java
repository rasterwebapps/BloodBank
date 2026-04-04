package com.bloodbank.common.events;

import java.time.Instant;
import java.util.UUID;

public record InvoiceGeneratedEvent(
    UUID invoiceId,
    UUID hospitalId,
    UUID branchId,
    Instant occurredAt
) {}
