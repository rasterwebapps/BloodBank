package com.bloodbank.documentservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.documentservice.dto.DocumentVersionResponse;
import com.bloodbank.documentservice.service.DocumentVersionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents/{documentId}/versions")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> createVersion(
            @PathVariable UUID documentId,
            @RequestParam(required = false) String changeDescription,
            @RequestParam(required = false) String uploadedBy) {
        DocumentVersionResponse response = documentVersionService.createVersion(documentId, changeDescription, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document version created successfully"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DocumentVersionResponse>>> getVersions(
            @PathVariable UUID documentId) {
        return ResponseEntity.ok(ApiResponse.success(documentVersionService.getVersions(documentId)));
    }

    @GetMapping("/{versionNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> getVersion(
            @PathVariable UUID documentId, @PathVariable int versionNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                documentVersionService.getVersion(documentId, versionNumber)));
    }
}
