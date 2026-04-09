package com.bloodbank.documentservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.documentservice.dto.DocumentResponse;
import com.bloodbank.documentservice.dto.DocumentUpdateRequest;
import com.bloodbank.documentservice.dto.DocumentUploadRequest;
import com.bloodbank.documentservice.enums.DocumentStatusEnum;
import com.bloodbank.documentservice.enums.DocumentTypeEnum;
import com.bloodbank.documentservice.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = DocumentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/documents";
    private UUID documentId;
    private UUID branchId;
    private UUID entityId;
    private DocumentResponse sampleResponse;
    private DocumentUploadRequest sampleUploadRequest;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        entityId = UUID.randomUUID();

        sampleResponse = new DocumentResponse(
                documentId, branchId, "DOC-12345678", "Donor Consent Form",
                DocumentTypeEnum.DONOR_CONSENT, "Donor", entityId,
                "application/pdf", 1024L, "/documents/file.pdf",
                "bloodbank-documents", "Consent form", "consent,donor",
                false, "admin", 1, DocumentStatusEnum.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleUploadRequest = new DocumentUploadRequest(
                "Donor Consent Form", DocumentTypeEnum.DONOR_CONSENT,
                "Donor", entityId, "Consent form", "consent,donor",
                false, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/documents")
    class CreateDocument {

        @Test
        @DisplayName("should create document as authenticated user — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreateAsAuthenticatedUser() throws Exception {
            when(documentService.create(any(DocumentUploadRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUploadRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.documentCode").value("DOC-12345678"));
        }

        @Test
        @DisplayName("should create document as DONOR — 201")
        @WithMockUser(roles = "DONOR")
        void shouldCreateAsDonor() throws Exception {
            when(documentService.create(any(DocumentUploadRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUploadRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should return 401 when unauthenticated")
        void shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUploadRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/documents/{id}")
    class GetById {

        @Test
        @DisplayName("should return document as authenticated user — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnAsAuthenticatedUser() throws Exception {
            when(documentService.getById(documentId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", documentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(documentId.toString()));
        }

        @Test
        @DisplayName("should return 401 when unauthenticated")
        void shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", documentId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/documents/code/{documentCode}")
    class GetByCode {

        @Test
        @DisplayName("should return document by code as authenticated user — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnByCodeAsAuthenticatedUser() throws Exception {
            when(documentService.getByCode("DOC-12345678")).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/code/{documentCode}", "DOC-12345678"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.documentCode").value("DOC-12345678"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/documents/{id}")
    class UpdateMetadata {

        @Test
        @DisplayName("should update metadata as authenticated user — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldUpdateAsAuthenticatedUser() throws Exception {
            DocumentUpdateRequest updateRequest = new DocumentUpdateRequest("Updated Name", "New desc", "new,tags");
            when(documentService.updateMetadata(eq(documentId), any(DocumentUpdateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}", documentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should return 401 when unauthenticated")
        void shouldReturnUnauthorized() throws Exception {
            DocumentUpdateRequest updateRequest = new DocumentUpdateRequest("Updated Name", null, null);

            mockMvc.perform(put(BASE_URL + "/{id}", documentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/documents/{id}")
    class SoftDelete {

        @Test
        @DisplayName("should soft delete document as authenticated user — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldDeleteAsAuthenticatedUser() throws Exception {
            doNothing().when(documentService).softDelete(documentId);

            mockMvc.perform(delete(BASE_URL + "/{id}", documentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should return 401 when unauthenticated")
        void shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", documentId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/documents/entity/{entityId}")
    class GetByEntityId {

        @Test
        @DisplayName("should return documents by entity ID as authenticated user — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnByEntityIdAsAuthenticatedUser() throws Exception {
            when(documentService.getByEntityId(entityId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/entity/{entityId}", entityId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
