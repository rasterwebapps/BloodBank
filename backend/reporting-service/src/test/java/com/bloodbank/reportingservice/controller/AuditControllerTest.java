package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.reportingservice.dto.AuditLogCreateRequest;
import com.bloodbank.reportingservice.dto.AuditLogResponse;
import com.bloodbank.reportingservice.enums.AuditActionEnum;
import com.bloodbank.reportingservice.service.AuditService;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuditController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class AuditControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuditService auditService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/audit-logs";
    private UUID entityId;
    private AuditLogResponse sampleResponse;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        sampleResponse = new AuditLogResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Donation", entityId,
                AuditActionEnum.CREATE, "user-1", "Test User", "AUDITOR",
                "127.0.0.1", null, null, "Donation created",
                Instant.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/v1/audit-logs")
    class Create {
        @Test
        @WithMockUser(roles = "AUDITOR")
        @DisplayName("should create as AUDITOR — 201")
        void shouldCreate() throws Exception {
            AuditLogCreateRequest request = new AuditLogCreateRequest(
                    UUID.randomUUID(), "Donation", entityId, AuditActionEnum.CREATE,
                    "user-1", "Test User", "AUDITOR", "127.0.0.1", null, null, "Created");
            when(auditService.createAuditLog(any())).thenReturn(sampleResponse);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject as DONOR — 403")
        void shouldReject() throws Exception {
            AuditLogCreateRequest request = new AuditLogCreateRequest(
                    UUID.randomUUID(), "Donation", entityId, AuditActionEnum.CREATE,
                    "user-1", "Test User", "AUDITOR", "127.0.0.1", null, null, "Created");
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/audit-logs/entity/{entityType}/{entityId}")
    class GetByEntity {
        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            when(auditService.getByEntityId("Donation", entityId)).thenReturn(List.of(sampleResponse));
            mockMvc.perform(get(BASE_URL + "/entity/Donation/{entityId}", entityId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/audit-logs/actor/{actorId}")
    class GetByActor {
        @Test
        @WithMockUser(roles = "REGIONAL_ADMIN")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            when(auditService.getByActorId("user-1")).thenReturn(List.of(sampleResponse));
            mockMvc.perform(get(BASE_URL + "/actor/user-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
