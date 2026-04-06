package com.bloodbank.labservice.dto;

import jakarta.validation.constraints.NotBlank;

public record TestPanelCreateRequest(
        @NotBlank String panelCode,
        @NotBlank String panelName,
        String description,
        String testNames,
        boolean isMandatory
) {}
