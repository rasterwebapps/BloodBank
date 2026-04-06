package com.bloodbank.donorservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.donorservice.dto.DonorLoyaltyResponse;
import com.bloodbank.donorservice.dto.LoyaltyRedeemRequest;
import com.bloodbank.donorservice.service.DonorLoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/donors/{donorId}/loyalty")
@Tag(name = "Donor Loyalty", description = "Donor loyalty and rewards")
public class DonorLoyaltyController {

    private final DonorLoyaltyService donorLoyaltyService;

    public DonorLoyaltyController(DonorLoyaltyService donorLoyaltyService) {
        this.donorLoyaltyService = donorLoyaltyService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DONOR','RECEPTIONIST','BRANCH_MANAGER')")
    @Operation(summary = "Get or create loyalty record for a donor")
    public ResponseEntity<ApiResponse<DonorLoyaltyResponse>> getLoyalty(
            @PathVariable UUID donorId) {
        return ResponseEntity.ok(ApiResponse.success(donorLoyaltyService.getOrCreateLoyalty(donorId)));
    }

    @PostMapping("/award")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','BRANCH_MANAGER')")
    @Operation(summary = "Award loyalty points to a donor")
    public ResponseEntity<ApiResponse<DonorLoyaltyResponse>> awardPoints(
            @PathVariable UUID donorId,
            @RequestParam int points) {
        return ResponseEntity.ok(ApiResponse.success(
                donorLoyaltyService.awardPoints(donorId, points), "Points awarded successfully"));
    }

    @PostMapping("/redeem")
    @PreAuthorize("hasAnyRole('DONOR','RECEPTIONIST')")
    @Operation(summary = "Redeem loyalty points")
    public ResponseEntity<ApiResponse<DonorLoyaltyResponse>> redeemPoints(
            @PathVariable UUID donorId,
            @Valid @RequestBody LoyaltyRedeemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                donorLoyaltyService.redeemPoints(donorId, request.points()), "Points redeemed successfully"));
    }
}
