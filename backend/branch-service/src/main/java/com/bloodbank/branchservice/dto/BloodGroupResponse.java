package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.util.UUID;

public record BloodGroupResponse(
    UUID id,
    String groupName,
    String description,
    boolean isActive
) implements Serializable {}
