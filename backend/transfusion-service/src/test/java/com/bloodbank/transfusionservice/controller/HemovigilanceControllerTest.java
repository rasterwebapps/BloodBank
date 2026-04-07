package com.bloodbank.transfusionservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.transfusionservice.dto.*;
import com.bloodbank.transfusionservice.enums.*;
import com.bloodbank.transfusionservice.service.HemovigilanceService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = HemovigilanceController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class HemovigilanceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private HemovigilanceService hemovigilanceService;
    @MockBean private JwtDecoder jwtDecoder;

    private UUID reportId, investigationId, reactionId, branchId, donorId;
    private HemovigilanceReportResponse sampleReportResponse;
    private LookBackInvestigationResponse sampleInvestigationResponse;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        investigationId = UUID.randomUUID();
        reactionId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        donorId = UUID.randomUUID();

        sampleReportResponse = new HemovigilanceReportResponse(
                reportId, reactionId, "HV-ABCD1234", Instant.now(),
                ImputabilityEnum.PROBABLE, "Dr. Smith", "Doctor",
                null, null, false, null,
                HemovigilanceStatusEnum.OPEN, branchId, LocalDateTime.now(), LocalDateTime.now());

        sampleInvestigationResponse = new LookBackInvestigationResponse(
                investigationId, donorId, null, "LB-ABCD1234",
                Instant.now(), InfectionTypeEnum.HBV, 3, 2, 1,
                LookBackStatusEnum.INITIATED, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested @DisplayName("POST /api/v1/hemovigilance/reports")
    class CreateReport {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should create report as DOCTOR")
        void asDoctor_returns201() throws Exception {
            var request = new HemovigilanceReportCreateRequest(reactionId, ImputabilityEnum.PROBABLE,
                    "Dr. Smith", "Doctor", null, null, false, branchId);
            when(hemovigilanceService.createReport(any())).thenReturn(sampleReportResponse);
            mockMvc.perform(post("/api/v1/hemovigilance/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.reporterName").value("Dr. Smith"));
        }

        @Test @WithMockUser(roles = {"NURSE"})
        @DisplayName("Should deny NURSE")
        void asNurse_returns403() throws Exception {
            var request = new HemovigilanceReportCreateRequest(reactionId, ImputabilityEnum.PROBABLE,
                    "Dr. Smith", "Doctor", null, null, false, branchId);
            mockMvc.perform(post("/api/v1/hemovigilance/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated")
        void unauthenticated_returns401() throws Exception {
            var request = new HemovigilanceReportCreateRequest(reactionId, ImputabilityEnum.PROBABLE,
                    "Dr. Smith", "Doctor", null, null, false, branchId);
            mockMvc.perform(post("/api/v1/hemovigilance/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested @DisplayName("PUT /api/v1/hemovigilance/reports/{id}/status")
    class UpdateReportStatus {
        @Test @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should update report status")
        void success() throws Exception {
            when(hemovigilanceService.updateReportStatus(eq(reportId), eq("UNDER_INVESTIGATION")))
                    .thenReturn(sampleReportResponse);
            mockMvc.perform(put("/api/v1/hemovigilance/reports/" + reportId + "/status")
                            .param("status", "UNDER_INVESTIGATION"))
                    .andExpect(status().isOk());
        }
    }

    @Nested @DisplayName("GET /api/v1/hemovigilance/reports/{id}")
    class GetReportById {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get report by ID")
        void success() throws Exception {
            when(hemovigilanceService.getReportById(reportId)).thenReturn(sampleReportResponse);
            mockMvc.perform(get("/api/v1/hemovigilance/reports/" + reportId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reportNumber").value("HV-ABCD1234"));
        }
    }

    @Nested @DisplayName("POST /api/v1/hemovigilance/lookback")
    class CreateLookBack {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should create lookback investigation")
        void success() throws Exception {
            var request = new LookBackInvestigationCreateRequest(donorId, null, InfectionTypeEnum.HBV,
                    3, 2, 1, null, null, branchId);
            when(hemovigilanceService.createLookBackInvestigation(any())).thenReturn(sampleInvestigationResponse);
            mockMvc.perform(post("/api/v1/hemovigilance/lookback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.infectionType").value("HBV"));
        }
    }

    @Nested @DisplayName("GET /api/v1/hemovigilance/lookback/{id}")
    class GetInvestigationById {
        @Test @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get investigation by ID")
        void success() throws Exception {
            when(hemovigilanceService.getInvestigationById(investigationId)).thenReturn(sampleInvestigationResponse);
            mockMvc.perform(get("/api/v1/hemovigilance/lookback/" + investigationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.investigationNumber").value("LB-ABCD1234"));
        }
    }
}
