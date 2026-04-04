package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.util.UUID;

public record DeferralReasonResponse(
    UUID id,
    String reasonCode,
    String reasonDescription,
    String deferralType,
    Integer defaultDays,
    boolean isActive
) implements Serializable {}
