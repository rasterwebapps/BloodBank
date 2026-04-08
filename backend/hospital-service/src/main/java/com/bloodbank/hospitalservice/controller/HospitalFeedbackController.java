package com.bloodbank.hospitalservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackResponse;
import com.bloodbank.hospitalservice.service.FeedbackService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hospital-feedback")
@Tag(name = "Hospital Feedback Management", description = "Submit and respond to hospital feedback")
public class HospitalFeedbackController {

    private final FeedbackService feedbackService;

    public HospitalFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HOSPITAL_USER')")
    @Operation(summary = "Submit hospital feedback")
    public ResponseEntity<ApiResponse<HospitalFeedbackResponse>> submitFeedback(
            @Valid @RequestBody HospitalFeedbackCreateRequest request) {
        HospitalFeedbackResponse response = feedbackService.submitFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Feedback submitted successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','HOSPITAL_USER')")
    @Operation(summary = "Get feedback by ID")
    public ResponseEntity<ApiResponse<HospitalFeedbackResponse>> getFeedbackById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getFeedbackById(id)));
    }

    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','HOSPITAL_USER')")
    @Operation(summary = "Get feedback by hospital")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalFeedbackResponse>>> getFeedbackByHospital(
            @PathVariable UUID hospitalId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getFeedbackByHospitalId(hospitalId, pageable)));
    }

    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','HOSPITAL_USER')")
    @Operation(summary = "Get feedback for a specific request")
    public ResponseEntity<ApiResponse<List<HospitalFeedbackResponse>>> getFeedbackByRequest(
            @PathVariable UUID requestId) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getFeedbackByRequestId(requestId)));
    }

    @PutMapping("/{id}/respond")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Respond to hospital feedback")
    public ResponseEntity<ApiResponse<HospitalFeedbackResponse>> respondToFeedback(
            @PathVariable UUID id,
            @RequestParam String responseText,
            @RequestParam String respondedBy) {
        HospitalFeedbackResponse response = feedbackService.respondToFeedback(id, responseText, respondedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Response submitted successfully"));
    }
}
