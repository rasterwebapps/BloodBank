package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.complianceservice.dto.LicenseCreateRequest;
import com.bloodbank.complianceservice.dto.LicenseResponse;
import com.bloodbank.complianceservice.enums.LicenseStatusEnum;
import com.bloodbank.complianceservice.enums.LicenseTypeEnum;
import com.bloodbank.complianceservice.service.LicenseService;
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

@WebMvcTest(value = LicenseController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class LicenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LicenseService licenseService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/compliance/licenses";
    private UUID licenseId;
    private UUID branchId;
    private LicenseResponse sampleResponse;
    private LicenseCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        licenseId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new LicenseResponse(
                licenseId, LicenseTypeEnum.BLOOD_BANK, "LIC-12345",
                "Health Authority", LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 12, 31), null, null, null,
                LicenseStatusEnum.ACTIVE, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new LicenseCreateRequest(
                LicenseTypeEnum.BLOOD_BANK, "LIC-12345", "Health Authority",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31),
                null, null, null, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/compliance/licenses")
    class CreateLicense {

        @Test
        @DisplayName("should create license as AUDITOR — 201")
        @WithMockUser(roles = "AUDITOR")
        void shouldCreateAsAuditor() throws Exception {
            when(licenseService.create(any(LicenseCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.licenseNumber").value("LIC-12345"));
        }

        @Test
        @DisplayName("should create license as BRANCH_ADMIN — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreateAsBranchAdmin() throws Exception {
            when(licenseService.create(any(LicenseCreateRequest.class))).thenReturn(sampleResponse);

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
    @DisplayName("GET /api/v1/compliance/licenses/{id}")
    class GetById {

        @Test
        @DisplayName("should return license as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnAsAuditor() throws Exception {
            when(licenseService.getById(licenseId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", licenseId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(licenseId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", licenseId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/licenses/number/{licenseNumber}")
    class GetByNumber {

        @Test
        @DisplayName("should return license by number as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldReturnByNumberAsBranchAdmin() throws Exception {
            when(licenseService.getByLicenseNumber("LIC-12345")).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/number/{licenseNumber}", "LIC-12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.licenseNumber").value("LIC-12345"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/licenses/status/{status}")
    class GetByStatus {

        @Test
        @DisplayName("should return licenses by status as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByStatusAsAuditor() throws Exception {
            when(licenseService.getByStatus(LicenseStatusEnum.ACTIVE)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/status/{status}", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/licenses/type/{type}")
    class GetByType {

        @Test
        @DisplayName("should return licenses by type as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnByTypeAsAuditor() throws Exception {
            when(licenseService.getByType(LicenseTypeEnum.BLOOD_BANK)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/type/{type}", "BLOOD_BANK"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/compliance/licenses/expiring")
    class GetExpiringSoon {

        @Test
        @DisplayName("should return expiring licenses as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldReturnExpiringAsAuditor() throws Exception {
            when(licenseService.getExpiringSoon(any(LocalDate.class))).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/expiring")
                            .param("beforeDate", "2025-06-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/licenses/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("should update license status as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldUpdateStatusAsBranchAdmin() throws Exception {
            when(licenseService.updateStatus(eq(licenseId), eq(LicenseStatusEnum.EXPIRED))).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", licenseId)
                            .param("status", "EXPIRED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/status", licenseId)
                            .param("status", "EXPIRED"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/compliance/licenses/{id}/renew")
    class RenewLicense {

        @Test
        @DisplayName("should renew license as AUDITOR — 200")
        @WithMockUser(roles = "AUDITOR")
        void shouldRenewAsAuditor() throws Exception {
            when(licenseService.renew(eq(licenseId), any(LocalDate.class))).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/renew", licenseId)
                            .param("newExpiryDate", "2026-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as HOSPITAL_USER — 403")
        @WithMockUser(roles = "HOSPITAL_USER")
        void shouldRejectAsHospitalUser() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/renew", licenseId)
                            .param("newExpiryDate", "2026-12-31"))
                    .andExpect(status().isForbidden());
        }
    }
}
