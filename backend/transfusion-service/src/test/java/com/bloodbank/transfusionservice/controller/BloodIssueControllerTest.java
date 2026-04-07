package com.bloodbank.transfusionservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.transfusionservice.dto.*;
import com.bloodbank.transfusionservice.enums.*;
import com.bloodbank.transfusionservice.service.BloodIssueService;
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

@WebMvcTest(value = BloodIssueController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class BloodIssueControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private BloodIssueService bloodIssueService;
    @MockBean private JwtDecoder jwtDecoder;

    private UUID issueId, branchId, componentId;
    private BloodIssueResponse sampleResponse;
    private BloodIssueCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        componentId = UUID.randomUUID();

        sampleResponse = new BloodIssueResponse(
                issueId, "BI-ABCD1234", null, componentId,
                "Jane Doe", "PAT-002", null, "Ward A", null,
                Instant.now(), null, IssueStatusEnum.ISSUED,
                null, null, branchId, LocalDateTime.now(), LocalDateTime.now());

        sampleCreateRequest = new BloodIssueCreateRequest(
                null, componentId, "Jane Doe", "PAT-002",
                null, "Ward A", null, null, branchId);
    }

    @Nested @DisplayName("POST /api/v1/blood-issues")
    class IssueBlood {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should issue blood as DOCTOR")
        void asDoctor_returns201() throws Exception {
            when(bloodIssueService.issueBlood(any())).thenReturn(sampleResponse);
            mockMvc.perform(post("/api/v1/blood-issues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.patientName").value("Jane Doe"));
        }

        @Test @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR")
        void asDonor_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/blood-issues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/blood-issues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested @DisplayName("POST /api/v1/blood-issues/emergency")
    class EmergencyIssue {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should issue emergency blood")
        void success() throws Exception {
            var emergencyReq = new EmergencyIssueCreateRequest(componentId, "Jane Doe", "PAT-002",
                    null, "Ward A", null, EmergencyTypeEnum.TRAUMA,
                    "Dr. Smith", "Major trauma", null, branchId);
            var emergencyResp = new EmergencyIssueResponse(UUID.randomUUID(), issueId,
                    EmergencyTypeEnum.TRAUMA, "Dr. Smith", Instant.now(), "Major trauma",
                    false, null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(bloodIssueService.issueEmergencyBlood(any())).thenReturn(emergencyResp);
            mockMvc.perform(post("/api/v1/blood-issues/emergency")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emergencyReq)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.emergencyType").value("TRAUMA"));
        }
    }

    @Nested @DisplayName("PUT /api/v1/blood-issues/{id}/return")
    class ReturnBlood {
        @Test @WithMockUser(roles = {"NURSE"})
        @DisplayName("Should return blood as NURSE")
        void success() throws Exception {
            var returnedResp = new BloodIssueResponse(issueId, "BI-ABCD1234", null, componentId,
                    "Jane Doe", "PAT-002", null, "Ward A", null,
                    Instant.now(), Instant.now(), IssueStatusEnum.RETURNED,
                    "Not needed", null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(bloodIssueService.returnBlood(eq(issueId), eq("Not needed"))).thenReturn(returnedResp);
            mockMvc.perform(put("/api/v1/blood-issues/" + issueId + "/return")
                            .param("returnReason", "Not needed"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("RETURNED"));
        }
    }

    @Nested @DisplayName("GET /api/v1/blood-issues/{id}")
    class GetById {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get blood issue by ID")
        void success() throws Exception {
            when(bloodIssueService.getById(issueId)).thenReturn(sampleResponse);
            mockMvc.perform(get("/api/v1/blood-issues/" + issueId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.issueNumber").value("BI-ABCD1234"));
        }
    }

    @Nested @DisplayName("GET /api/v1/blood-issues/number/{issueNumber}")
    class GetByNumber {
        @Test @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get blood issue by number")
        void success() throws Exception {
            when(bloodIssueService.getByIssueNumber("BI-ABCD1234")).thenReturn(sampleResponse);
            mockMvc.perform(get("/api/v1/blood-issues/number/BI-ABCD1234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.issueNumber").value("BI-ABCD1234"));
        }
    }
}
