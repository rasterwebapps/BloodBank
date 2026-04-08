package com.bloodbank.requestmatchingservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.BranchDataFilterAspect;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestResponse;
import com.bloodbank.requestmatchingservice.enums.EmergencyPriorityEnum;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;
import com.bloodbank.requestmatchingservice.service.RequestMatchingService;
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
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
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

@WebMvcTest(value = RequestMatchingController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {GlobalExceptionHandler.class, BranchDataFilterAspect.class}))
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class RequestMatchingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private RequestMatchingService requestMatchingService;
    @MockBean private JwtDecoder jwtDecoder;

    private UUID requestId;
    private UUID branchId;
    private UUID bloodGroupId;
    private UUID componentTypeId;
    private EmergencyRequestResponse sampleResponse;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();

        sampleResponse = new EmergencyRequestResponse(
                requestId, "ER-ABCD1234", null, bloodGroupId, componentTypeId,
                4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                false, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/v1/matching/{requestId}")
    class MatchRequest {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should match request as DOCTOR")
        void asDoctor_returns200() throws Exception {
            when(requestMatchingService.matchRequest(any(UUID.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/matching/" + requestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.requestNumber").value("ER-ABCD1234"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should match request as BRANCH_MANAGER")
        void asBranchManager_returns200() throws Exception {
            when(requestMatchingService.matchRequest(any(UUID.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/matching/" + requestId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on matching endpoint")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/matching/" + requestId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR on matching endpoint")
        void asDonor_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/matching/" + requestId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/matching/" + requestId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/matching/open")
    class GetOpenRequests {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get open requests as DOCTOR")
        void asDoctor_returns200() throws Exception {
            when(requestMatchingService.getOpenRequests()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/matching/open"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].requestNumber").value("ER-ABCD1234"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get open requests as BRANCH_MANAGER")
        void asBranchManager_returns200() throws Exception {
            when(requestMatchingService.getOpenRequests()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/matching/open"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on open requests")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/matching/open"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/matching/open"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
