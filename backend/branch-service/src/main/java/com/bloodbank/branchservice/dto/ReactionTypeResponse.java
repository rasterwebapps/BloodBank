package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.util.UUID;

public record ReactionTypeResponse(
    UUID id,
    String reactionCode,
    String reactionName,
    String severity,
    String description,
    boolean isActive
) implements Serializable {}
