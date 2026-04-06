package com.bloodbank.labservice.dto;

import jakarta.validation.constraints.NotBlank;

public record TestResultApprovalRequest(
        @NotBlank String verifiedBy
) {}
