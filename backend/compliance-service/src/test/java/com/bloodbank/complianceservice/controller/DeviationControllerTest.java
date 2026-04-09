package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.complianceservice.dto.CorrectiveActionRequest;
import com.bloodbank.complianceservice.dto.DeviationCreateRequest;
import com.bloodbank.complianceservice.dto.DeviationResponse;
import com.bloodbank.complianceservice.enums.DeviationCategoryEnum;
import com.bloodbank.complianceservice.enums.DeviationSeverityEnum;
import com.bloodbank.complianceservice.enums.DeviationStatusEnum;
import com.bloodbank.complianceservice.enums.DeviationTypeEnum;
import com.bloodbank.complianceservice.service.DeviationService;
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

@WebMvcTest(value = DeviationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class DeviationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviationService deviationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/compliance/deviations";
    private UUID deviationId;
    private UUID branchId;
    private DeviationResponse sampleResponse;
    private DeviationCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        deviationId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new DeviationResponse(
                deviationId, "DEV-12345678", DeviationTypeEnum.NON_CONFORMANCE,
                DeviationSeverityEnum.MAJOR, DeviationCategoryEnum.COLLECTION,
                "Temperature deviation", "Fridge temperature exceeded limits",
                Instant.now(), null, null, null, null, null,
                null, null, DeviationStatusEnum.OPEN, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new DeviationCreateRequest(
                DeviationTypeEnum.NON_CONFORMANCE, DeviationSeverityEnum.MAJOR,
                DeviationCategoryEnum.COLLECTION, "Temperature deviation",
                "Fridge temperature exceeded limits", null, null, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/compliance/deviations")
    class CreateDeviation {

        @Test
        @DisplayName("should create deviation as AUDITOR — 201")
        @WithMockUser(roles = "AUDITOR")
        void shouldCreateAsAuditor() throws Exception {
            when(deviationService.create(any(DeviationCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Temperature deviation"));
        }

        @Test
        @DisplayName("should create deviation as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreateAsBranchManager() throws Exception {
            when(deviationService.create(any(DeviationCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should reject as HOSPITAL_USER — 403")
        @WithMockUser(roles = "HOSPITAL_USER")
        void shouldRejectAsHospitalUser() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 401 when unauthenticated")
        void shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/deviations/{id}")
    class GetById {

        @Test
        @DisplayName("should return deviation as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnAsBranchManager() throws Exception {
            when(deviationService.getById(deviationId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", deviationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(deviationId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", deviationId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/deviations/number/{number}")
    class GetByNumber {

        @Test
        @DisplayName("should return deviation by number as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByNumberAsAuditor() throws Exception {
            when(deviationService.getByDeviationNumber("DEV-12345678")).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/number/{number}", "DEV-12345678"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.deviationNumber").value("DEV-12345678"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/deviations/status/{status}")
    class GetByStatus {

        @Test
        @DisplayName("should return deviations by status as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByStatusAsAuditor() throws Exception {
            when(deviationService.getByStatus(DeviationStatusEnum.OPEN)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/status/{status}", "OPEN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/deviations/severity/{severity}")
    class GetBySeverity {

        @Test
        @DisplayName("should return deviations by severity as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnBySeverityAsBranchManager() throws Exception {
            when(deviationService.getBySeverity(DeviationSeverityEnum.MAJOR)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/severity/{severity}", "MAJOR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/deviations/{id}/investigate")
    class Investigate {

        @Test
        @DisplayName("should start investigation as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldInvestigateAsAuditor() throws Exception {
            when(deviationService.investigate(deviationId)).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/investigate", deviationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/investigate", deviationId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/deviations/{id}/corrective-action")
    class AddCorrectiveAction {

        @Test
        @DisplayName("should add corrective action as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldAddCorrectiveActionAsBranchManager() throws Exception {
            CorrectiveActionRequest request = new CorrectiveActionRequest(
                    "Faulty thermostat", "Replace thermostat", "Monthly calibration"
            );
            when(deviationService.addCorrectiveAction(eq(deviationId), any(CorrectiveActionRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/corrective-action", deviationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/deviations/{id}/close")
    class CloseDeviation {

        @Test
        @DisplayName("should close deviation as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldCloseAsAuditor() throws Exception {
            when(deviationService.close(eq(deviationId), eq("admin"))).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/close", deviationId)
                            .param("closedBy", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as HOSPITAL_USER — 403")
        @WithMockUser(roles = "HOSPITAL_USER")
        void shouldRejectAsHospitalUser() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/close", deviationId)
                            .param("closedBy", "admin"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/deviations/{id}/reopen")
    class ReopenDeviation {

        @Test
        @DisplayName("should reopen deviation as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReopenAsBranchManager() throws Exception {
            when(deviationService.reopen(deviationId)).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/reopen", deviationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
