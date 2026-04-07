package com.bloodbank.documentservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.documentservice.dto.DocumentVersionResponse;
import com.bloodbank.documentservice.entity.Document;
import com.bloodbank.documentservice.entity.DocumentVersion;
import com.bloodbank.documentservice.mapper.DocumentVersionMapper;
import com.bloodbank.documentservice.repository.DocumentRepository;
import com.bloodbank.documentservice.repository.DocumentVersionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentVersionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentVersionService.class);

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionMapper documentVersionMapper;

    public DocumentVersionService(DocumentVersionRepository documentVersionRepository,
                                  DocumentRepository documentRepository,
                                  DocumentVersionMapper documentVersionMapper) {
        this.documentVersionRepository = documentVersionRepository;
        this.documentRepository = documentRepository;
        this.documentVersionMapper = documentVersionMapper;
    }

    @Transactional
    public DocumentVersionResponse createVersion(UUID documentId, String changeDescription, String uploadedBy) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        int nextVersion = document.getCurrentVersion() + 1;
        document.setCurrentVersion(nextVersion);
        documentRepository.save(document);

        DocumentVersion version = new DocumentVersion(documentId, nextVersion);
        version.setBranchId(document.getBranchId());
        version.setChangeDescription(changeDescription);
        version.setUploadedBy(uploadedBy);
        version.setUploadedAt(Instant.now());
        version.setMimeType(document.getMimeType());
        version.setFileSizeBytes(document.getFileSizeBytes());

        version = documentVersionRepository.save(version);
        log.info("Created version {} for document {}", nextVersion, documentId);
        return documentVersionMapper.toResponse(version);
    }

    public List<DocumentVersionResponse> getVersions(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document", "id", documentId);
        }
        List<DocumentVersion> versions = documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId);
        return documentVersionMapper.toResponseList(versions);
    }

    public DocumentVersionResponse getVersion(UUID documentId, int versionNumber) {
        DocumentVersion version = documentVersionRepository.findByDocumentIdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentVersion", "versionNumber", versionNumber));
        return documentVersionMapper.toResponse(version);
    }
}
