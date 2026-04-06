package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.util.UUID;

public record RegionResponse(
    UUID id,
    UUID countryId,
    String countryName,
    String regionCode,
    String regionName,
    boolean isActive
) implements Serializable {}
