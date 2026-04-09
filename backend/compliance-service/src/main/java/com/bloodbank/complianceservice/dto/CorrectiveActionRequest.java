package com.bloodbank.complianceservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CorrectiveActionRequest(
        @NotBlank String rootCause,
        @NotBlank String correctiveAction,
        String preventiveAction
) {}
