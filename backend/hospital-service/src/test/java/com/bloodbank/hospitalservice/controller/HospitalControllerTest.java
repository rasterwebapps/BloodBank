package com.bloodbank.hospitalservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.hospitalservice.dto.HospitalCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalResponse;
import com.bloodbank.hospitalservice.enums.HospitalStatusEnum;
import com.bloodbank.hospitalservice.enums.HospitalTypeEnum;
import com.bloodbank.hospitalservice.service.HospitalService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = HospitalController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HospitalService hospitalService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/hospitals";
    private UUID hospitalId;
    private UUID branchId;
    private UUID cityId;
    private HospitalResponse sampleResponse;
    private HospitalCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        cityId = UUID.randomUUID();

        sampleResponse = new HospitalResponse(
                hospitalId, "HSP-ABCD1234", "City General Hospital",
                HospitalTypeEnum.GOVERNMENT, "123 Main St", null,
                cityId, "62701", "+1234567890", "info@citygeneral.com",
                "Dr. Smith", "LIC-001", 500, true,
                HospitalStatusEnum.ACTIVE, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new HospitalCreateRequest(
                "City General Hospital", HospitalTypeEnum.GOVERNMENT,
                "123 Main St", null, cityId, "62701",
                "+1234567890", "info@citygeneral.com", "Dr. Smith",
                "LIC-001", 500, true, branchId
        );
    }

    // ==================== CREATE HOSPITAL ====================

    @Nested
    @DisplayName("POST /api/v1/hospitals")
    class CreateHospital {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should create hospital as BRANCH_ADMIN - 201")
        void createHospital_asBranchAdmin_returns201() throws Exception {
            when(hospitalService.createHospital(any(HospitalCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.hospitalName").value("City General Hospital"))
                    .andExpect(jsonPath("$.data.hospitalCode").value("HSP-ABCD1234"))
                    .andExpect(jsonPath("$.message").value("Hospital registered successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should create hospital as BRANCH_MANAGER - 201")
        void createHospital_asBranchManager_returns201() throws Exception {
            when(hospitalService.createHospital(any(HospitalCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should create hospital as SUPER_ADMIN - 201")
        void createHospital_asSuperAdmin_returns201() throws Exception {
            when(hospitalService.createHospital(any(HospitalCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER from creating hospital - 403")
        void createHospital_asHospitalUser_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from creating hospital - 403")
        void createHospital_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void createHospital_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== UPDATE HOSPITAL ====================

    @Nested
    @DisplayName("PUT /api/v1/hospitals/{id}")
    class UpdateHospital {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should update hospital as BRANCH_ADMIN - 200")
        void updateHospital_asBranchAdmin_returns200() throws Exception {
            when(hospitalService.updateHospital(eq(hospitalId), any(HospitalCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.hospitalName").value("City General Hospital"))
                    .andExpect(jsonPath("$.message").value("Hospital updated successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from updating hospital - 403")
        void updateHospital_asDonor_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void updateHospital_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET HOSPITAL BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/hospitals/{id}")
    class GetHospitalById {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get hospital by ID as BRANCH_ADMIN - 200")
        void getHospitalById_asBranchAdmin_returns200() throws Exception {
            when(hospitalService.getHospitalById(hospitalId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(hospitalId.toString()));
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get hospital by ID as HOSPITAL_USER - 200")
        void getHospitalById_asHospitalUser_returns200() throws Exception {
            when(hospitalService.getHospitalById(hospitalId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", hospitalId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting hospital - 403")
        void getHospitalById_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", hospitalId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void getHospitalById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", hospitalId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET ALL HOSPITALS ====================

    @Nested
    @DisplayName("GET /api/v1/hospitals")
    class GetAllHospitals {

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should list hospitals as BRANCH_MANAGER - 200")
        void getAllHospitals_asBranchManager_returns200() throws Exception {
            PagedResponse<HospitalResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(hospitalService.getAllHospitals(any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from listing hospitals - 403")
        void getAllHospitals_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== SEARCH HOSPITALS ====================

    @Nested
    @DisplayName("GET /api/v1/hospitals/search")
    class SearchHospitals {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should search hospitals as BRANCH_ADMIN - 200")
        void searchHospitals_asBranchAdmin_returns200() throws Exception {
            PagedResponse<HospitalResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(hospitalService.searchHospitals(eq("City"), any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/search")
                            .param("name", "City"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].hospitalName").value("City General Hospital"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from searching hospitals - 403")
        void searchHospitals_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/search")
                            .param("name", "City"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET HOSPITALS BY STATUS ====================

    @Nested
    @DisplayName("GET /api/v1/hospitals/status/{status}")
    class GetHospitalsByStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get hospitals by status as BRANCH_ADMIN - 200")
        void getByStatus_asBranchAdmin_returns200() throws Exception {
            PagedResponse<HospitalResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(hospitalService.getHospitalsByStatus(eq(HospitalStatusEnum.ACTIVE), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/status/{status}", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting hospitals by status - 403")
        void getByStatus_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/status/{status}", "ACTIVE"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET HOSPITAL BY CODE ====================

    @Nested
    @DisplayName("GET /api/v1/hospitals/code/{hospitalCode}")
    class GetHospitalByCode {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get hospital by code as BRANCH_ADMIN - 200")
        void getByCode_asBranchAdmin_returns200() throws Exception {
            when(hospitalService.getHospitalByCode("HSP-ABCD1234")).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/code/{hospitalCode}", "HSP-ABCD1234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.hospitalCode").value("HSP-ABCD1234"));
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get hospital by code as HOSPITAL_USER - 200")
        void getByCode_asHospitalUser_returns200() throws Exception {
            when(hospitalService.getHospitalByCode("HSP-ABCD1234")).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/code/{hospitalCode}", "HSP-ABCD1234"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting hospital by code - 403")
        void getByCode_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/code/{hospitalCode}", "HSP-ABCD1234"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== UPDATE HOSPITAL STATUS ====================

    @Nested
    @DisplayName("PUT /api/v1/hospitals/{id}/status")
    class UpdateHospitalStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should update hospital status as BRANCH_ADMIN - 200")
        void updateStatus_asBranchAdmin_returns200() throws Exception {
            when(hospitalService.updateHospitalStatus(eq(hospitalId), eq(HospitalStatusEnum.INACTIVE)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/status", hospitalId)
                            .param("status", "INACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Hospital status updated successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from updating hospital status - 403")
        void updateStatus_asDonor_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/status", hospitalId)
                            .param("status", "INACTIVE"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER from updating hospital status - 403")
        void updateStatus_asHospitalUser_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/status", hospitalId)
                            .param("status", "INACTIVE"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void updateStatus_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/status", hospitalId)
                            .param("status", "INACTIVE"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
