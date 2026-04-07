package com.bloodbank.billingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.billingservice.dto.InvoiceCreateRequest;
import com.bloodbank.billingservice.dto.InvoiceResponse;
import com.bloodbank.billingservice.dto.LineItemCreateRequest;
import com.bloodbank.billingservice.dto.LineItemResponse;
import com.bloodbank.billingservice.enums.InvoiceStatusEnum;
import com.bloodbank.billingservice.service.InvoiceService;

import jakarta.validation.Valid;

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
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody InvoiceCreateRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Invoice created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoiceById(id)));
    }

    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByHospital(
            @PathVariable UUID hospitalId) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoicesByHospital(hospitalId)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStatus(
            @PathVariable InvoiceStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoicesByStatus(status)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoiceStatus(
            @PathVariable UUID id, @RequestParam InvoiceStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(
                invoiceService.updateInvoiceStatus(id, status), "Invoice status updated"));
    }

    @PostMapping("/{id}/line-items")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<LineItemResponse>> addLineItem(
            @PathVariable UUID id, @Valid @RequestBody LineItemCreateRequest request) {
        LineItemResponse response = invoiceService.addLineItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Line item added successfully"));
    }

    @PostMapping("/{id}/recalculate")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> recalculateTotals(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                invoiceService.recalculateInvoiceTotals(id), "Invoice totals recalculated"));
    }
}
