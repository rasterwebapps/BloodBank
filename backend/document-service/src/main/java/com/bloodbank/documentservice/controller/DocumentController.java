package com.bloodbank.documentservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.documentservice.dto.DocumentResponse;
import com.bloodbank.documentservice.dto.DocumentUpdateRequest;
import com.bloodbank.documentservice.dto.DocumentUploadRequest;
import com.bloodbank.documentservice.service.DocumentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentResponse>> create(
            @Valid @RequestBody DocumentUploadRequest request) {
        DocumentResponse response = documentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getById(id)));
    }

    @GetMapping("/code/{documentCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentResponse>> getByCode(@PathVariable String documentCode) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getByCode(documentCode)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateMetadata(
            @PathVariable UUID id, @Valid @RequestBody DocumentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                documentService.updateMetadata(id, request), "Document updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable UUID id) {
        documentService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted successfully"));
    }

    @GetMapping("/entity/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getByEntityId(@PathVariable UUID entityId) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getByEntityId(entityId)));
    }
}
