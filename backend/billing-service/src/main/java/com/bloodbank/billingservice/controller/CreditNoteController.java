package com.bloodbank.billingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.billingservice.dto.CreditNoteCreateRequest;
import com.bloodbank.billingservice.dto.CreditNoteResponse;
import com.bloodbank.billingservice.service.CreditNoteService;

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
@RequestMapping("/api/v1/credit-notes")
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    public CreditNoteController(CreditNoteService creditNoteService) {
        this.creditNoteService = creditNoteService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> createCreditNote(
            @Valid @RequestBody CreditNoteCreateRequest request) {
        CreditNoteResponse response = creditNoteService.createCreditNote(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Credit note created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> getCreditNoteById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(creditNoteService.getCreditNoteById(id)));
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CreditNoteResponse>>> getCreditNotesByInvoice(
            @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(ApiResponse.success(creditNoteService.getCreditNotesByInvoice(invoiceId)));
    }

    @PatchMapping("/{id}/apply")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> applyCreditNote(
            @PathVariable UUID id, @RequestParam UUID targetInvoiceId) {
        return ResponseEntity.ok(ApiResponse.success(
                creditNoteService.applyCreditNote(id, targetInvoiceId), "Credit note applied successfully"));
    }

    @PatchMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> voidCreditNote(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                creditNoteService.voidCreditNote(id), "Credit note voided successfully"));
    }
}
