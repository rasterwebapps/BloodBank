package com.bloodbank.documentservice.dto;

import jakarta.validation.constraints.Size;

public record DocumentUpdateRequest(
        @Size(max = 200) String documentName,
        @Size(max = 500) String description,
        @Size(max = 500) String tags
) {}
