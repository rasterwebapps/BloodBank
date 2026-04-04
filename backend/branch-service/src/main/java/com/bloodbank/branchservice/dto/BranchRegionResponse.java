package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.util.UUID;

public record BranchRegionResponse(
    UUID id,
    UUID branchId,
    UUID regionId,
    String regionName,
    boolean isPrimary
) implements Serializable {}
