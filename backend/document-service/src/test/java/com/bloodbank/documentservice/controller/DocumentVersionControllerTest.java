package com.bloodbank.documentservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.documentservice.dto.DocumentVersionResponse;
import com.bloodbank.documentservice.service.DocumentVersionService;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = DocumentVersionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class DocumentVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentVersionService documentVersionService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UUID documentId;
    private UUID versionId;
    private UUID branchId;
    private DocumentVersionResponse sampleVersionResponse;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        versionId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleVersionResponse = new DocumentVersionResponse(
                versionId, branchId, documentId, 2,
                "/documents/v2/file.pdf", 2048L, "application/pdf",
                "Updated content", "admin", Instant.now(),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("POST /api/v1/documents/{documentId}/versions")
    class CreateVersion {

        @Test
        @DisplayName("should create version as authenticated user — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreateVersionAsAuthenticatedUser() throws Exception {
            when(documentVersionService.createVersion(eq(documentId), eq("Updated content"), eq("admin")))
                    .thenReturn(sampleVersionResponse);

            mockMvc.perform(post("/api/v1/documents/{documentId}/versions", documentId)
                            .param("changeDescription", "Updated content")
                            .param("uploadedBy", "admin"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.versionNumber").value(2));
        }

        @Test
        @DisplayName("should return 401 when unauthenticated")
        void shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(post("/api/v1/documents/{documentId}/versions", documentId)
                            .param("changeDescription", "desc"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/documents/{documentId}/versions")
    class GetVersions {

        @Test
        @DisplayName("should return versions as authenticated user — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnVersionsAsAuthenticatedUser() throws Exception {
            when(documentVersionService.getVersions(documentId)).thenReturn(List.of(sampleVersionResponse));

            mockMvc.perform(get("/api/v1/documents/{documentId}/versions", documentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("should return 401 when unauthenticated")
        void shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/documents/{documentId}/versions", documentId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/documents/{documentId}/versions/{versionNumber}")
    class GetVersion {

        @Test
        @DisplayName("should return specific version as authenticated user — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnSpecificVersionAsAuthenticatedUser() throws Exception {
            when(documentVersionService.getVersion(documentId, 2)).thenReturn(sampleVersionResponse);

            mockMvc.perform(get("/api/v1/documents/{documentId}/versions/{versionNumber}", documentId, 2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.versionNumber").value(2));
        }

        @Test
        @DisplayName("should return 401 when unauthenticated")
        void shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/documents/{documentId}/versions/{versionNumber}", documentId, 2))
                    .andExpect(status().isUnauthorized());
        }
    }
}
