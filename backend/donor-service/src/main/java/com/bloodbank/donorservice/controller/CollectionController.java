package com.bloodbank.donorservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.model.enums.CollectionStatusEnum;
import com.bloodbank.donorservice.dto.AdverseReactionCreateRequest;
import com.bloodbank.donorservice.dto.AdverseReactionResponse;
import com.bloodbank.donorservice.dto.CollectionCompleteRequest;
import com.bloodbank.donorservice.dto.CollectionCreateRequest;
import com.bloodbank.donorservice.dto.CollectionResponse;
import com.bloodbank.donorservice.dto.CollectionSampleCreateRequest;
import com.bloodbank.donorservice.dto.CollectionSampleResponse;
import com.bloodbank.donorservice.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections")
@Tag(name = "Blood Collection", description = "Blood collection operations")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','BRANCH_MANAGER')")
    @Operation(summary = "Start a new blood collection")
    public ResponseEntity<ApiResponse<CollectionResponse>> startCollection(
            @Valid @RequestBody CollectionCreateRequest request) {
        CollectionResponse response = collectionService.startCollection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Collection started successfully"));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','BRANCH_MANAGER')")
    @Operation(summary = "Complete a blood collection")
    public ResponseEntity<ApiResponse<CollectionResponse>> completeCollection(
            @PathVariable UUID id,
            @Valid @RequestBody CollectionCompleteRequest request) {
        CollectionResponse response = collectionService.completeCollection(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Collection completed successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','BRANCH_MANAGER','LAB_TECHNICIAN')")
    @Operation(summary = "Get collection by ID")
    public ResponseEntity<ApiResponse<CollectionResponse>> getCollectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getCollectionById(id)));
    }

    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','BRANCH_MANAGER','DOCTOR')")
    @Operation(summary = "Get collections by donor")
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getCollectionsByDonor(
            @PathVariable UUID donorId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getCollectionsByDonor(donorId)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','BRANCH_MANAGER')")
    @Operation(summary = "Get collections by status")
    public ResponseEntity<ApiResponse<PagedResponse<CollectionResponse>>> getCollectionsByStatus(
            @PathVariable CollectionStatusEnum status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getCollectionsByStatus(status, pageable)));
    }

    @PostMapping("/{collectionId}/adverse-reactions")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','DOCTOR','NURSE')")
    @Operation(summary = "Record an adverse reaction during collection")
    public ResponseEntity<ApiResponse<AdverseReactionResponse>> recordAdverseReaction(
            @PathVariable UUID collectionId,
            @Valid @RequestBody AdverseReactionCreateRequest request) {
        AdverseReactionResponse response = collectionService.recordAdverseReaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Adverse reaction recorded successfully"));
    }

    @GetMapping("/{collectionId}/adverse-reactions")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get adverse reactions for a collection")
    public ResponseEntity<ApiResponse<List<AdverseReactionResponse>>> getAdverseReactions(
            @PathVariable UUID collectionId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getAdverseReactions(collectionId)));
    }

    @PostMapping("/{collectionId}/samples")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','LAB_TECHNICIAN')")
    @Operation(summary = "Add a sample to a collection")
    public ResponseEntity<ApiResponse<CollectionSampleResponse>> addSample(
            @PathVariable UUID collectionId,
            @Valid @RequestBody CollectionSampleCreateRequest request) {
        CollectionSampleResponse response = collectionService.addSample(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Sample added successfully"));
    }

    @GetMapping("/{collectionId}/samples")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','LAB_TECHNICIAN','BRANCH_MANAGER')")
    @Operation(summary = "Get samples for a collection")
    public ResponseEntity<ApiResponse<List<CollectionSampleResponse>>> getSamples(
            @PathVariable UUID collectionId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getSamples(collectionId)));
    }
}
