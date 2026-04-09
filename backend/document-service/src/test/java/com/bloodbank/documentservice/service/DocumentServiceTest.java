package com.bloodbank.documentservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.documentservice.dto.DocumentResponse;
import com.bloodbank.documentservice.dto.DocumentUpdateRequest;
import com.bloodbank.documentservice.dto.DocumentUploadRequest;
import com.bloodbank.documentservice.entity.Document;
import com.bloodbank.documentservice.enums.DocumentStatusEnum;
import com.bloodbank.documentservice.enums.DocumentTypeEnum;
import com.bloodbank.documentservice.mapper.DocumentMapper;
import com.bloodbank.documentservice.repository.DocumentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentService documentService;

    private UUID documentId;
    private UUID branchId;
    private UUID entityId;
    private Document document;
    private DocumentResponse documentResponse;
    private DocumentUploadRequest uploadRequest;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        entityId = UUID.randomUUID();

        document = new Document("Donor Consent Form", DocumentTypeEnum.DONOR_CONSENT);
        document.setId(documentId);
        document.setBranchId(branchId);
        document.setDocumentCode("DOC-12345678");
        document.setEntityType("Donor");
        document.setEntityId(entityId);
        document.setStatus(DocumentStatusEnum.ACTIVE);
        document.setCurrentVersion(1);

        documentResponse = new DocumentResponse(
                documentId, branchId, "DOC-12345678", "Donor Consent Form",
                DocumentTypeEnum.DONOR_CONSENT, "Donor", entityId,
                "application/pdf", 1024L, "/documents/file.pdf",
                "bloodbank-documents", "Consent form", "consent,donor",
                false, "admin", 1, DocumentStatusEnum.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now()
        );

        uploadRequest = new DocumentUploadRequest(
                "Donor Consent Form", DocumentTypeEnum.DONOR_CONSENT,
                "Donor", entityId, "Consent form", "consent,donor",
                false, branchId
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create document successfully")
        void shouldCreateDocumentSuccessfully() {
            when(documentMapper.toEntity(uploadRequest)).thenReturn(document);
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentMapper.toResponse(document)).thenReturn(documentResponse);

            DocumentResponse result = documentService.create(uploadRequest);

            assertThat(result).isNotNull();
            assertThat(result.documentName()).isEqualTo("Donor Consent Form");
            assertThat(result.documentType()).isEqualTo(DocumentTypeEnum.DONOR_CONSENT);
            verify(documentRepository).save(any(Document.class));
        }

        @Test
        @DisplayName("should set ACTIVE status and generate document code")
        void shouldSetActiveStatusAndGenerateCode() {
            Document newDoc = new Document("Test Doc", DocumentTypeEnum.OTHER);
            when(documentMapper.toEntity(uploadRequest)).thenReturn(newDoc);
            when(documentRepository.save(any(Document.class))).thenReturn(newDoc);
            when(documentMapper.toResponse(newDoc)).thenReturn(documentResponse);

            documentService.create(uploadRequest);

            assertThat(newDoc.getStatus()).isEqualTo(DocumentStatusEnum.ACTIVE);
            assertThat(newDoc.getDocumentCode()).startsWith("DOC-");
            assertThat(newDoc.getCurrentVersion()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return document when found")
        void shouldReturnDocumentWhenFound() {
            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(documentMapper.toResponse(document)).thenReturn(documentResponse);

            DocumentResponse result = documentService.getById(documentId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(documentId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.getById(documentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByCode")
    class GetByCode {

        @Test
        @DisplayName("should return document by code")
        void shouldReturnByCode() {
            when(documentRepository.findByDocumentCode("DOC-12345678")).thenReturn(Optional.of(document));
            when(documentMapper.toResponse(document)).thenReturn(documentResponse);

            DocumentResponse result = documentService.getByCode("DOC-12345678");

            assertThat(result.documentCode()).isEqualTo("DOC-12345678");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when code not found")
        void shouldThrowWhenCodeNotFound() {
            when(documentRepository.findByDocumentCode("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.getByCode("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateMetadata")
    class UpdateMetadata {

        @Test
        @DisplayName("should update document name")
        void shouldUpdateDocumentName() {
            DocumentUpdateRequest request = new DocumentUpdateRequest("New Name", null, null);

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentMapper.toResponse(document)).thenReturn(documentResponse);

            DocumentResponse result = documentService.updateMetadata(documentId, request);

            assertThat(result).isNotNull();
            assertThat(document.getDocumentName()).isEqualTo("New Name");
            verify(documentRepository).save(any(Document.class));
        }

        @Test
        @DisplayName("should update description")
        void shouldUpdateDescription() {
            DocumentUpdateRequest request = new DocumentUpdateRequest(null, "Updated description", null);

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentMapper.toResponse(document)).thenReturn(documentResponse);

            documentService.updateMetadata(documentId, request);

            assertThat(document.getDescription()).isEqualTo("Updated description");
        }

        @Test
        @DisplayName("should update tags")
        void shouldUpdateTags() {
            DocumentUpdateRequest request = new DocumentUpdateRequest(null, null, "new,tags");

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentMapper.toResponse(document)).thenReturn(documentResponse);

            documentService.updateMetadata(documentId, request);

            assertThat(document.getTags()).isEqualTo("new,tags");
        }

        @Test
        @DisplayName("should not update null fields")
        void shouldNotUpdateNullFields() {
            document.setDocumentName("Original");
            document.setDescription("Original desc");
            document.setTags("original");
            DocumentUpdateRequest request = new DocumentUpdateRequest(null, null, null);

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentMapper.toResponse(document)).thenReturn(documentResponse);

            documentService.updateMetadata(documentId, request);

            assertThat(document.getDocumentName()).isEqualTo("Original");
            assertThat(document.getDescription()).isEqualTo("Original desc");
            assertThat(document.getTags()).isEqualTo("original");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            DocumentUpdateRequest request = new DocumentUpdateRequest("New Name", null, null);
            when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.updateMetadata(documentId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("should soft delete document")
        void shouldSoftDeleteDocument() {
            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

            documentService.softDelete(documentId);

            assertThat(document.getStatus()).isEqualTo(DocumentStatusEnum.DELETED);
            verify(documentRepository).save(document);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.softDelete(documentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByEntityId")
    class GetByEntityId {

        @Test
        @DisplayName("should return documents by entity ID")
        void shouldReturnByEntityId() {
            List<Document> documents = List.of(document);
            List<DocumentResponse> responses = List.of(documentResponse);
            when(documentRepository.findByEntityIdAndStatus(entityId, DocumentStatusEnum.ACTIVE)).thenReturn(documents);
            when(documentMapper.toResponseList(documents)).thenReturn(responses);

            List<DocumentResponse> result = documentService.getByEntityId(entityId);

            assertThat(result).hasSize(1);
            verify(documentRepository).findByEntityIdAndStatus(entityId, DocumentStatusEnum.ACTIVE);
        }
    }
}
