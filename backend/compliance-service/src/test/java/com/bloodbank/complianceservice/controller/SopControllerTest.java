package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.complianceservice.dto.SopCreateRequest;
import com.bloodbank.complianceservice.dto.SopResponse;
import com.bloodbank.complianceservice.enums.SopCategoryEnum;
import com.bloodbank.complianceservice.enums.SopStatusEnum;
import com.bloodbank.complianceservice.service.SopService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SopController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class SopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SopService sopService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/compliance/sops";
    private UUID sopId;
    private UUID branchId;
    private UUID frameworkId;
    private SopResponse sampleResponse;
    private SopCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        sopId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        frameworkId = UUID.randomUUID();

        sampleResponse = new SopResponse(
                sopId, "SOP-001", "Blood Collection SOP",
                SopCategoryEnum.COLLECTION, frameworkId, "1.0",
                LocalDate.of(2024, 1, 1), null, null, null,
                null, SopStatusEnum.DRAFT, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new SopCreateRequest(
                "SOP-001", "Blood Collection SOP",
                SopCategoryEnum.COLLECTION, frameworkId,
                "1.0", LocalDate.of(2024, 1, 1), null, null, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/compliance/sops")
    class CreateSop {

        @Test
        @DisplayName("should create SOP as AUDITOR — 201")
        @WithMockUser(roles = "AUDITOR")
        void shouldCreateAsAuditor() throws Exception {
            when(sopService.create(any(SopCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sopCode").value("SOP-001"));
        }

        @Test
        @DisplayName("should create SOP as BRANCH_ADMIN — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreateAsBranchAdmin() throws Exception {
            when(sopService.create(any(SopCreateRequest.class))).thenReturn(sampleResponse);

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
    @DisplayName("GET /api/v1/compliance/sops/{id}")
    class GetById {

        @Test
        @DisplayName("should return SOP as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldReturnAsBranchAdmin() throws Exception {
            when(sopService.getById(sopId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", sopId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(sopId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", sopId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/sops/framework/{frameworkId}")
    class GetByFramework {

        @Test
        @DisplayName("should return SOPs by framework as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByFrameworkAsAuditor() throws Exception {
            when(sopService.getByFrameworkId(frameworkId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/framework/{frameworkId}", frameworkId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/sops/status/{status}")
    class GetByStatus {

        @Test
        @DisplayName("should return SOPs by status as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByStatusAsAuditor() throws Exception {
            when(sopService.getByStatus(SopStatusEnum.DRAFT)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/status/{status}", "DRAFT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/sops/category/{category}")
    class GetByCategory {

        @Test
        @DisplayName("should return SOPs by category as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldReturnByCategoryAsBranchAdmin() throws Exception {
            when(sopService.getByCategory(SopCategoryEnum.COLLECTION)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/category/{category}", "COLLECTION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/sops/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("should update status as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldUpdateStatusAsAuditor() throws Exception {
            when(sopService.updateStatus(eq(sopId), eq(SopStatusEnum.REVIEW))).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", sopId)
                            .param("status", "REVIEW"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/status", sopId)
                            .param("status", "REVIEW"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/sops/{id}/approve")
    class ApproveSop {

        @Test
        @DisplayName("should approve SOP as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldApproveAsBranchAdmin() throws Exception {
            when(sopService.approve(eq(sopId), eq("admin"))).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/approve", sopId)
                            .param("approvedBy", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/sops/{id}/retire")
    class RetireSop {

        @Test
        @DisplayName("should retire SOP as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldRetireAsAuditor() throws Exception {
            when(sopService.retire(sopId)).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/retire", sopId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as HOSPITAL_USER — 403")
        @WithMockUser(roles = "HOSPITAL_USER")
        void shouldRejectAsHospitalUser() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/retire", sopId))
                    .andExpect(status().isForbidden());
        }
    }
}
