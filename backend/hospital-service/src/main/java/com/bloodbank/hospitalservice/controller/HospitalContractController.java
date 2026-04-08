package com.bloodbank.hospitalservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.hospitalservice.dto.HospitalContractCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalContractResponse;
import com.bloodbank.hospitalservice.enums.ContractStatusEnum;
import com.bloodbank.hospitalservice.service.ContractService;

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
@RequestMapping("/api/v1/hospitals/{hospitalId}/contracts")
@Tag(name = "Hospital Contract Management", description = "Hospital contract lifecycle operations")
public class HospitalContractController {

    private final ContractService contractService;

    public HospitalContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Create a new hospital contract")
    public ResponseEntity<ApiResponse<HospitalContractResponse>> createContract(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody HospitalContractCreateRequest request) {
        HospitalContractResponse response = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Contract created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "List contracts for a hospital")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalContractResponse>>> getContracts(
            @PathVariable UUID hospitalId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getContractsByHospitalId(hospitalId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get contract by ID")
    public ResponseEntity<ApiResponse<HospitalContractResponse>> getContractById(
            @PathVariable UUID hospitalId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getContractById(id)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get active contracts for a hospital")
    public ResponseEntity<ApiResponse<List<HospitalContractResponse>>> getActiveContracts(
            @PathVariable UUID hospitalId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getActiveContracts(hospitalId)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Update contract status")
    public ResponseEntity<ApiResponse<HospitalContractResponse>> updateContractStatus(
            @PathVariable UUID hospitalId,
            @PathVariable UUID id,
            @RequestParam ContractStatusEnum status) {
        HospitalContractResponse response = contractService.updateContractStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Contract status updated successfully"));
    }
}
