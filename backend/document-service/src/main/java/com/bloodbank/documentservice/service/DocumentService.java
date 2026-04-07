package com.bloodbank.documentservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.documentservice.dto.DocumentResponse;
import com.bloodbank.documentservice.dto.DocumentUpdateRequest;
import com.bloodbank.documentservice.dto.DocumentUploadRequest;
import com.bloodbank.documentservice.entity.Document;
import com.bloodbank.documentservice.enums.DocumentStatusEnum;
import com.bloodbank.documentservice.mapper.DocumentMapper;
import com.bloodbank.documentservice.repository.DocumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    public DocumentService(DocumentRepository documentRepository, DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
    }

    @Transactional
    public DocumentResponse create(DocumentUploadRequest request) {
        log.info("Creating document: name={}, type={}", request.documentName(), request.documentType());
        Document document = documentMapper.toEntity(request);
        document.setDocumentCode("DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        document.setStatus(DocumentStatusEnum.ACTIVE);
        document.setCurrentVersion(1);
        document.setBranchId(request.branchId());
        document = documentRepository.save(document);
        return documentMapper.toResponse(document);
    }

    public DocumentResponse getById(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return documentMapper.toResponse(document);
    }

    public DocumentResponse getByCode(String documentCode) {
        Document document = documentRepository.findByDocumentCode(documentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "documentCode", documentCode));
        return documentMapper.toResponse(document);
    }

    @Transactional
    public DocumentResponse updateMetadata(UUID id, DocumentUpdateRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        if (request.documentName() != null) {
            document.setDocumentName(request.documentName());
        }
        if (request.description() != null) {
            document.setDescription(request.description());
        }
        if (request.tags() != null) {
            document.setTags(request.tags());
        }
        document = documentRepository.save(document);
        log.info("Updated document metadata: id={}", id);
        return documentMapper.toResponse(document);
    }

    @Transactional
    public void softDelete(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        document.setStatus(DocumentStatusEnum.DELETED);
        documentRepository.save(document);
        log.info("Soft deleted document: id={}", id);
    }

    public List<DocumentResponse> getByEntityId(UUID entityId) {
        List<Document> documents = documentRepository.findByEntityIdAndStatus(entityId, DocumentStatusEnum.ACTIVE);
        return documentMapper.toResponseList(documents);
    }
}
