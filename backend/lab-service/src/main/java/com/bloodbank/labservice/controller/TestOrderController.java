package com.bloodbank.labservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.labservice.dto.TestOrderCreateRequest;
import com.bloodbank.labservice.dto.TestOrderResponse;
import com.bloodbank.labservice.enums.OrderStatusEnum;
import com.bloodbank.labservice.service.TestOrderService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test-orders")
public class TestOrderController {

    private final TestOrderService testOrderService;

    public TestOrderController(TestOrderService testOrderService) {
        this.testOrderService = testOrderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<TestOrderResponse>> createOrder(
            @Valid @RequestBody TestOrderCreateRequest request) {
        TestOrderResponse response = testOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Test order created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<TestOrderResponse>> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(testOrderService.getOrderById(id)));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<TestOrderResponse>>> getOrdersByBranch(
            @PathVariable UUID branchId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(testOrderService.getOrdersByBranchId(branchId, pageable)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<TestOrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(testOrderService.getOrdersByStatus(status)));
    }

    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<TestOrderResponse>>> getOrdersByDonor(
            @PathVariable UUID donorId) {
        return ResponseEntity.ok(ApiResponse.success(testOrderService.getOrdersByDonorId(donorId)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<TestOrderResponse>> updateOrderStatus(
            @PathVariable UUID id, @RequestParam OrderStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(
                testOrderService.updateOrderStatus(id, status), "Test order status updated"));
    }
}
