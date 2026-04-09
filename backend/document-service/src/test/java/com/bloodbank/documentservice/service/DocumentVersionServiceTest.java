package com.bloodbank.documentservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.documentservice.dto.DocumentVersionResponse;
import com.bloodbank.documentservice.entity.Document;
import com.bloodbank.documentservice.entity.DocumentVersion;
import com.bloodbank.documentservice.enums.DocumentTypeEnum;
import com.bloodbank.documentservice.mapper.DocumentVersionMapper;
import com.bloodbank.documentservice.repository.DocumentRepository;
import com.bloodbank.documentservice.repository.DocumentVersionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
class DocumentVersionServiceTest {

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentVersionMapper documentVersionMapper;

    @InjectMocks
    private DocumentVersionService documentVersionService;

    private UUID documentId;
    private UUID versionId;
    private UUID branchId;
    private Document document;
    private DocumentVersion version;
    private DocumentVersionResponse versionResponse;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        versionId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        document = new Document("Test Document", DocumentTypeEnum.DONOR_CONSENT);
        document.setId(documentId);
        document.setBranchId(branchId);
        document.setCurrentVersion(1);
        document.setMimeType("application/pdf");
        document.setFileSizeBytes(2048L);

        version = new DocumentVersion(documentId, 2);
        version.setId(versionId);
        version.setBranchId(branchId);
        version.setChangeDescription("Updated content");
        version.setUploadedBy("admin");
        version.setUploadedAt(Instant.now());
        version.setMimeType("application/pdf");
        version.setFileSizeBytes(2048L);

        versionResponse = new DocumentVersionResponse(
                versionId, branchId, documentId, 2,
                null, 2048L, "application/pdf",
                "Updated content", "admin", Instant.now(),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("createVersion")
    class CreateVersion {

        @Test
        @DisplayName("should create new version and increment document version")
        void shouldCreateNewVersion() {
            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentVersionRepository.save(any(DocumentVersion.class))).thenReturn(version);
            when(documentVersionMapper.toResponse(version)).thenReturn(versionResponse);

            DocumentVersionResponse result = documentVersionService.createVersion(
                    documentId, "Updated content", "admin");

            assertThat(result).isNotNull();
            assertThat(result.versionNumber()).isEqualTo(2);
            assertThat(document.getCurrentVersion()).isEqualTo(2);
            verify(documentRepository).save(document);
            verify(documentVersionRepository).save(any(DocumentVersion.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when document not found")
        void shouldThrowWhenDocumentNotFound() {
            when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentVersionService.createVersion(
                    documentId, "Description", "admin"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getVersions")
    class GetVersions {

        @Test
        @DisplayName("should return all versions for a document")
        void shouldReturnAllVersions() {
            List<DocumentVersion> versions = List.of(version);
            List<DocumentVersionResponse> responses = List.of(versionResponse);

            when(documentRepository.existsById(documentId)).thenReturn(true);
            when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId)).thenReturn(versions);
            when(documentVersionMapper.toResponseList(versions)).thenReturn(responses);

            List<DocumentVersionResponse> result = documentVersionService.getVersions(documentId);

            assertThat(result).hasSize(1);
            verify(documentVersionRepository).findByDocumentIdOrderByVersionNumberDesc(documentId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when document not found")
        void shouldThrowWhenDocumentNotFound() {
            when(documentRepository.existsById(documentId)).thenReturn(false);

            assertThatThrownBy(() -> documentVersionService.getVersions(documentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getVersion")
    class GetVersion {

        @Test
        @DisplayName("should return specific version")
        void shouldReturnSpecificVersion() {
            when(documentVersionRepository.findByDocumentIdAndVersionNumber(documentId, 2))
                    .thenReturn(Optional.of(version));
            when(documentVersionMapper.toResponse(version)).thenReturn(versionResponse);

            DocumentVersionResponse result = documentVersionService.getVersion(documentId, 2);

            assertThat(result).isNotNull();
            assertThat(result.versionNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when version not found")
        void shouldThrowWhenVersionNotFound() {
            when(documentVersionRepository.findByDocumentIdAndVersionNumber(documentId, 99))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentVersionService.getVersion(documentId, 99))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
