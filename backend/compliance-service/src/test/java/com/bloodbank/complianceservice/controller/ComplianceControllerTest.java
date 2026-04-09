package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkCreateRequest;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkResponse;
import com.bloodbank.complianceservice.service.ComplianceService;
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

@WebMvcTest(value = ComplianceController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplianceService complianceService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/compliance/frameworks";
    private UUID frameworkId;
    private RegulatoryFrameworkResponse sampleResponse;
    private RegulatoryFrameworkCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        frameworkId = UUID.randomUUID();
        UUID countryId = UUID.randomUUID();

        sampleResponse = new RegulatoryFrameworkResponse(
                frameworkId, "AABB-001", "AABB Standards", "AABB",
                countryId, "Blood bank standards",
                LocalDate.of(2024, 1, 1), "1.0",
                "https://example.com/aabb", true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new RegulatoryFrameworkCreateRequest(
                "AABB-001", "AABB Standards", "AABB",
                countryId, "Blood bank standards",
                LocalDate.of(2024, 1, 1), "1.0",
                "https://example.com/aabb"
        );
    }

    @Nested
    @DisplayName("POST /api/v1/compliance/frameworks")
    class CreateFramework {

        @Test
        @DisplayName("should create framework as AUDITOR — 201")
        @WithMockUser(roles = "AUDITOR")
        void shouldCreateAsAuditor() throws Exception {
            when(complianceService.create(any(RegulatoryFrameworkCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.frameworkCode").value("AABB-001"));
        }

        @Test
        @DisplayName("should create framework as SUPER_ADMIN — 201")
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldCreateAsSuperAdmin() throws Exception {
            when(complianceService.create(any(RegulatoryFrameworkCreateRequest.class))).thenReturn(sampleResponse);

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
    @DisplayName("GET /api/v1/compliance/frameworks/{id}")
    class GetById {

        @Test
        @DisplayName("should return framework as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnAsAuditor() throws Exception {
            when(complianceService.getById(frameworkId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", frameworkId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(frameworkId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", frameworkId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/frameworks")
    class GetAll {

        @Test
        @DisplayName("should return all frameworks as SUPER_ADMIN — 200")
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldReturnAllAsSuperAdmin() throws Exception {
            when(complianceService.getAll()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/frameworks/code/{code}")
    class GetByCode {

        @Test
        @DisplayName("should return framework by code as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByCodeAsAuditor() throws Exception {
            when(complianceService.getByFrameworkCode("AABB-001")).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/code/{code}", "AABB-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.frameworkCode").value("AABB-001"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/frameworks/active")
    class GetActiveFrameworks {

        @Test
        @DisplayName("should return active frameworks as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnActiveAsAuditor() throws Exception {
            when(complianceService.getActiveFrameworks()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/compliance/frameworks/{id}")
    class UpdateFramework {

        @Test
        @DisplayName("should update framework as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldUpdateAsAuditor() throws Exception {
            when(complianceService.update(eq(frameworkId), any(RegulatoryFrameworkCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}", frameworkId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}", frameworkId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/frameworks/{id}/deactivate")
    class DeactivateFramework {

        @Test
        @DisplayName("should deactivate framework as SUPER_ADMIN — 200")
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldDeactivateAsSuperAdmin() throws Exception {
            when(complianceService.deactivate(frameworkId)).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", frameworkId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as HOSPITAL_USER — 403")
        @WithMockUser(roles = "HOSPITAL_USER")
        void shouldRejectAsHospitalUser() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", frameworkId))
                    .andExpect(status().isForbidden());
        }
    }
}
