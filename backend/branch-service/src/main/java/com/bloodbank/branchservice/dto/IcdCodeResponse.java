package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.util.UUID;

public record IcdCodeResponse(
    UUID id,
    String icdCode,
    String description,
    String category,
    boolean isActive
) implements Serializable {}
