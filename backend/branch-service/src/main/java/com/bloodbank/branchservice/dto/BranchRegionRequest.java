package com.bloodbank.branchservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BranchRegionRequest(
    @NotNull UUID regionId,
    boolean isPrimary
) {}
