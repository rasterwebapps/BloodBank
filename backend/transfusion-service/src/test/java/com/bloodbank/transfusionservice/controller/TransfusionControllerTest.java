package com.bloodbank.transfusionservice.controller;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.common.model.enums.TransfusionStatusEnum;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.transfusionservice.dto.*;
import com.bloodbank.transfusionservice.enums.*;
import com.bloodbank.transfusionservice.service.TransfusionService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TransfusionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class TransfusionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private TransfusionService transfusionService;
    @MockBean private JwtDecoder jwtDecoder;

    private UUID transfusionId, bloodIssueId, branchId;
    private TransfusionResponse sampleResponse;
    private TransfusionCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        transfusionId = UUID.randomUUID();
        bloodIssueId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new TransfusionResponse(
                transfusionId, bloodIssueId, "John Doe", "PAT-001",
                null, Instant.now(), null, null,
                "Nurse A", null, null, null,
                TransfusionStatusEnum.IN_PROGRESS, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());

        sampleCreateRequest = new TransfusionCreateRequest(
                bloodIssueId, "John Doe", "PAT-001", null,
                "Nurse A", null, null, null, branchId);
    }

    @Nested @DisplayName("POST /api/v1/transfusions")
    class StartTransfusion {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should start transfusion as DOCTOR")
        void asDoctor_returns201() throws Exception {
            when(transfusionService.startTransfusion(any())).thenReturn(sampleResponse);
            mockMvc.perform(post("/api/v1/transfusions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.patientName").value("John Doe"));
        }

        @Test @WithMockUser(roles = {"NURSE"})
        @DisplayName("Should start transfusion as NURSE")
        void asNurse_returns201() throws Exception {
            when(transfusionService.startTransfusion(any())).thenReturn(sampleResponse);
            mockMvc.perform(post("/api/v1/transfusions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test @WithMockUser(roles = {"BILLING_CLERK"})
        @DisplayName("Should deny BILLING_CLERK")
        void asBillingClerk_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/transfusions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/transfusions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested @DisplayName("PUT /api/v1/transfusions/{id}/complete")
    class CompleteTransfusion {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should complete transfusion")
        void success() throws Exception {
            var completeReq = new TransfusionCompleteRequest(450, "BP: 120/80", "Done");
            var completedResp = new TransfusionResponse(transfusionId, bloodIssueId, "John Doe", "PAT-001",
                    null, Instant.now(), Instant.now(), 450, "Nurse A", null, null, "BP: 120/80",
                    TransfusionStatusEnum.COMPLETED, TransfusionOutcomeEnum.SUCCESSFUL, "Done", branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(transfusionService.completeTransfusion(eq(transfusionId), any())).thenReturn(completedResp);
            mockMvc.perform(put("/api/v1/transfusions/" + transfusionId + "/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(completeReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }
    }

    @Nested @DisplayName("POST /api/v1/transfusions/reactions")
    class ReportReaction {
        @Test @WithMockUser(roles = {"NURSE"})
        @DisplayName("Should report reaction as NURSE")
        void success() throws Exception {
            UUID reactionTypeId = UUID.randomUUID();
            var reactionReq = new TransfusionReactionCreateRequest(transfusionId, reactionTypeId,
                    Instant.now(), "Fever", SeverityEnum.MODERATE, "Paracetamol",
                    ReactionOutcomeEnum.RESOLVED, "Nurse A", branchId);
            var reactionResp = new TransfusionReactionResponse(UUID.randomUUID(), transfusionId,
                    reactionTypeId, Instant.now(), "Fever", SeverityEnum.MODERATE, "Paracetamol",
                    ReactionOutcomeEnum.RESOLVED, "Nurse A", branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(transfusionService.reportReaction(any())).thenReturn(reactionResp);
            mockMvc.perform(post("/api/v1/transfusions/reactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reactionReq)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.severity").value("MODERATE"));
        }
    }

    @Nested @DisplayName("GET /api/v1/transfusions/{id}")
    class GetById {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get transfusion by ID")
        void success() throws Exception {
            when(transfusionService.getById(transfusionId)).thenReturn(sampleResponse);
            mockMvc.perform(get("/api/v1/transfusions/" + transfusionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.patientName").value("John Doe"));
        }
    }

    @Nested @DisplayName("GET /api/v1/transfusions/patient/{patientId}")
    class GetByPatient {
        @Test @WithMockUser(roles = {"NURSE"})
        @DisplayName("Should get transfusions by patient")
        void success() throws Exception {
            var paged = new PagedResponse<>(List.of(sampleResponse), 0, 20, 1, 1, true);
            when(transfusionService.getByPatient(eq("PAT-001"), any())).thenReturn(paged);
            mockMvc.perform(get("/api/v1/transfusions/patient/PAT-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }
}
