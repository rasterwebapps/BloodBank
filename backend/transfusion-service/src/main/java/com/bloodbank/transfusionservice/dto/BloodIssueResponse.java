package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.IssueStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record BloodIssueResponse(
    UUID id,
    String issueNumber,
    UUID crossmatchRequestId,
    UUID componentId,
    String patientName,
    String patientId,
    UUID hospitalId,
    String issuedTo,
    String issuedBy,
    Instant issueDate,
    Instant returnDate,
    IssueStatusEnum status,
    String returnReason,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
