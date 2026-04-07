package com.bloodbank.transfusionservice.controller;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.model.enums.RequestStatusEnum;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.transfusionservice.dto.*;
import com.bloodbank.transfusionservice.enums.*;
import com.bloodbank.transfusionservice.service.CrossMatchService;
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

@WebMvcTest(value = CrossMatchController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class CrossMatchControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CrossMatchService crossMatchService;
    @MockBean private JwtDecoder jwtDecoder;

    private UUID requestId, branchId, bloodGroupId, componentTypeId;
    private CrossMatchRequestResponse sampleRequestResponse;
    private CrossMatchRequestCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();

        sampleRequestResponse = new CrossMatchRequestResponse(
                requestId, "CM-ABCD1234", "John Doe", "PAT-001",
                bloodGroupId, null, "Dr. Smith", null, null,
                2, componentTypeId, PriorityEnum.ROUTINE, null,
                RequestStatusEnum.PENDING, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());

        sampleCreateRequest = new CrossMatchRequestCreateRequest(
                "John Doe", "PAT-001", bloodGroupId, null,
                "Dr. Smith", null, null, 2, componentTypeId,
                PriorityEnum.ROUTINE, null, null, branchId);
    }

    @Nested @DisplayName("POST /api/v1/crossmatch/requests")
    class CreateRequest {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should create request as DOCTOR")
        void asDoctor_returns201() throws Exception {
            when(crossMatchService.createRequest(any())).thenReturn(sampleRequestResponse);
            mockMvc.perform(post("/api/v1/crossmatch/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.patientName").value("John Doe"));
        }

        @Test @WithMockUser(roles = {"NURSE"})
        @DisplayName("Should create request as NURSE")
        void asNurse_returns201() throws Exception {
            when(crossMatchService.createRequest(any())).thenReturn(sampleRequestResponse);
            mockMvc.perform(post("/api/v1/crossmatch/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR")
        void asDonor_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/crossmatch/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/crossmatch/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested @DisplayName("GET /api/v1/crossmatch/requests/{id}")
    class GetRequestById {
        @Test @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get request by ID")
        void success() throws Exception {
            when(crossMatchService.getRequestById(requestId)).thenReturn(sampleRequestResponse);
            mockMvc.perform(get("/api/v1/crossmatch/requests/" + requestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.requestNumber").value("CM-ABCD1234"));
        }
    }

    @Nested @DisplayName("GET /api/v1/crossmatch/requests/status/{status}")
    class GetByStatus {
        @Test @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get requests by status")
        void success() throws Exception {
            var paged = new PagedResponse<>(List.of(sampleRequestResponse), 0, 20, 1, 1, true);
            when(crossMatchService.getRequestsByStatus(eq(RequestStatusEnum.PENDING), any())).thenReturn(paged);
            mockMvc.perform(get("/api/v1/crossmatch/requests/status/PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }
}
