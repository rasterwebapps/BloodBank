package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.complianceservice.dto.RecallCreateRequest;
import com.bloodbank.complianceservice.dto.RecallResponse;
import com.bloodbank.complianceservice.enums.RecallSeverityEnum;
import com.bloodbank.complianceservice.enums.RecallStatusEnum;
import com.bloodbank.complianceservice.enums.RecallTypeEnum;
import com.bloodbank.complianceservice.service.RecallService;
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

@WebMvcTest(value = RecallController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class RecallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecallService recallService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/compliance/recalls";
    private UUID recallId;
    private UUID branchId;
    private RecallResponse sampleResponse;
    private RecallCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        recallId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new RecallResponse(
                recallId, "RCL-12345678", RecallTypeEnum.PRODUCT_RECALL,
                "Contamination detected", RecallSeverityEnum.CLASS_I,
                Instant.now(), null, 5, 0, 0, false, null,
                null, null, RecallStatusEnum.INITIATED, null, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new RecallCreateRequest(
                RecallTypeEnum.PRODUCT_RECALL, "Contamination detected",
                RecallSeverityEnum.CLASS_I, null, 5, null, null, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/compliance/recalls")
    class CreateRecall {

        @Test
        @DisplayName("should create recall as AUDITOR — 201")
        @WithMockUser(roles = "AUDITOR")
        void shouldCreateAsAuditor() throws Exception {
            when(recallService.create(any(RecallCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.recallNumber").value("RCL-12345678"));
        }

        @Test
        @DisplayName("should create recall as SUPER_ADMIN — 201")
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldCreateAsSuperAdmin() throws Exception {
            when(recallService.create(any(RecallCreateRequest.class))).thenReturn(sampleResponse);

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
    @DisplayName("GET /api/v1/compliance/recalls/{id}")
    class GetById {

        @Test
        @DisplayName("should return recall as SUPER_ADMIN — 200")
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldReturnAsSuperAdmin() throws Exception {
            when(recallService.getById(recallId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", recallId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(recallId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", recallId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/recalls/number/{number}")
    class GetByNumber {

        @Test
        @DisplayName("should return recall by number as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByNumberAsAuditor() throws Exception {
            when(recallService.getByRecallNumber("RCL-12345678")).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/number/{number}", "RCL-12345678"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.recallNumber").value("RCL-12345678"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/recalls/status/{status}")
    class GetByStatus {

        @Test
        @DisplayName("should return recalls by status as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByStatusAsAuditor() throws Exception {
            when(recallService.getByStatus(RecallStatusEnum.INITIATED)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/status/{status}", "INITIATED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/recalls/type/{type}")
    class GetByType {

        @Test
        @DisplayName("should return recalls by type as SUPER_ADMIN — 200")
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldReturnByTypeAsSuperAdmin() throws Exception {
            when(recallService.getByType(RecallTypeEnum.PRODUCT_RECALL)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/type/{type}", "PRODUCT_RECALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/recalls/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("should update status as SUPER_ADMIN — 200")
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldUpdateStatusAsSuperAdmin() throws Exception {
            when(recallService.updateStatus(eq(recallId), eq(RecallStatusEnum.IN_PROGRESS))).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", recallId)
                            .param("status", "IN_PROGRESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/status", recallId)
                            .param("status", "IN_PROGRESS"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/recalls/{id}/close")
    class CloseRecall {

        @Test
        @DisplayName("should close recall as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldCloseAsAuditor() throws Exception {
            when(recallService.close(eq(recallId), eq("admin"))).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/close", recallId)
                            .param("closedBy", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as HOSPITAL_USER — 403")
        @WithMockUser(roles = "HOSPITAL_USER")
        void shouldRejectAsHospitalUser() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/close", recallId)
                            .param("closedBy", "admin"))
                    .andExpect(status().isForbidden());
        }
    }
}
