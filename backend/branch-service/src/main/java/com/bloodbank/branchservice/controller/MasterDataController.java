package com.bloodbank.branchservice.controller;

import com.bloodbank.branchservice.dto.*;
import com.bloodbank.branchservice.service.MasterDataService;
import com.bloodbank.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/master-data")
@Tag(name = "Master Data", description = "Read-only access to reference/master data")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping("/blood-groups")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active blood groups")
    public ResponseEntity<ApiResponse<List<BloodGroupResponse>>> getAllBloodGroups() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllBloodGroups()));
    }

    @GetMapping("/blood-groups/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get blood group by ID")
    public ResponseEntity<ApiResponse<BloodGroupResponse>> getBloodGroupById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getBloodGroupById(id)));
    }

    @GetMapping("/component-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active component types")
    public ResponseEntity<ApiResponse<List<ComponentTypeResponse>>> getAllComponentTypes() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllComponentTypes()));
    }

    @GetMapping("/component-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get component type by ID")
    public ResponseEntity<ApiResponse<ComponentTypeResponse>> getComponentTypeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getComponentTypeById(id)));
    }

    @GetMapping("/component-types/code/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get component type by code")
    public ResponseEntity<ApiResponse<ComponentTypeResponse>> getComponentTypeByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getComponentTypeByCode(code)));
    }

    @GetMapping("/countries")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active countries")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getAllCountries() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllCountries()));
    }

    @GetMapping("/countries/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get country by ID")
    public ResponseEntity<ApiResponse<CountryResponse>> getCountryById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getCountryById(id)));
    }

    @GetMapping("/regions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active regions")
    public ResponseEntity<ApiResponse<List<RegionResponse>>> getAllRegions() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllRegions()));
    }

    @GetMapping("/regions/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get region by ID")
    public ResponseEntity<ApiResponse<RegionResponse>> getRegionById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getRegionById(id)));
    }

    @GetMapping("/countries/{countryId}/regions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get regions by country")
    public ResponseEntity<ApiResponse<List<RegionResponse>>> getRegionsByCountry(@PathVariable UUID countryId) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getRegionsByCountry(countryId)));
    }

    @GetMapping("/cities")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active cities")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getAllCities() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllCities()));
    }

    @GetMapping("/cities/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get city by ID")
    public ResponseEntity<ApiResponse<CityResponse>> getCityById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getCityById(id)));
    }

    @GetMapping("/regions/{regionId}/cities")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get cities by region")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getCitiesByRegion(@PathVariable UUID regionId) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getCitiesByRegion(regionId)));
    }

    @GetMapping("/deferral-reasons")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active deferral reasons")
    public ResponseEntity<ApiResponse<List<DeferralReasonResponse>>> getAllDeferralReasons() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllDeferralReasons()));
    }

    @GetMapping("/deferral-reasons/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get deferral reason by ID")
    public ResponseEntity<ApiResponse<DeferralReasonResponse>> getDeferralReasonById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getDeferralReasonById(id)));
    }

    @GetMapping("/deferral-reasons/type/{type}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get deferral reasons by type")
    public ResponseEntity<ApiResponse<List<DeferralReasonResponse>>> getDeferralReasonsByType(
            @PathVariable String type) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getDeferralReasonsByType(type)));
    }

    @GetMapping("/reaction-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active reaction types")
    public ResponseEntity<ApiResponse<List<ReactionTypeResponse>>> getAllReactionTypes() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllReactionTypes()));
    }

    @GetMapping("/reaction-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get reaction type by ID")
    public ResponseEntity<ApiResponse<ReactionTypeResponse>> getReactionTypeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getReactionTypeById(id)));
    }

    @GetMapping("/reaction-types/severity/{severity}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get reaction types by severity")
    public ResponseEntity<ApiResponse<List<ReactionTypeResponse>>> getReactionTypesBySeverity(
            @PathVariable String severity) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getReactionTypesBySeverity(severity)));
    }

    @GetMapping("/icd-codes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active ICD codes")
    public ResponseEntity<ApiResponse<List<IcdCodeResponse>>> getAllIcdCodes() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllIcdCodes()));
    }

    @GetMapping("/icd-codes/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get ICD code by ID")
    public ResponseEntity<ApiResponse<IcdCodeResponse>> getIcdCodeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getIcdCodeById(id)));
    }

    @GetMapping("/icd-codes/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get ICD codes by category")
    public ResponseEntity<ApiResponse<List<IcdCodeResponse>>> getIcdCodesByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getIcdCodesByCategory(category)));
    }
}
