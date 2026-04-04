package com.bloodbank.branchservice.controller;

import com.bloodbank.branchservice.dto.*;
import com.bloodbank.branchservice.service.BranchService;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = BranchController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BranchService branchService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/branches";
    private UUID branchId;
    private UUID cityId;
    private UUID regionId;
    private BranchResponse sampleBranchResponse;
    private BranchCreateRequest sampleCreateRequest;
    private BranchUpdateRequest sampleUpdateRequest;

    @BeforeEach
    void setUp() {
        branchId = UUID.randomUUID();
        cityId = UUID.randomUUID();
        regionId = UUID.randomUUID();

        sampleBranchResponse = new BranchResponse(
                branchId, "BR-001", "Main Branch", "COLLECTION_CENTER",
                "123 Main St", "Suite 100", cityId, "Springfield",
                "62701", "+1234567890", "branch@bloodbank.org",
                "LIC-2024-001", LocalDate.of(2025, 12, 31),
                new BigDecimal("39.7817"), new BigDecimal("-89.6501"),
                "ACTIVE", null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new BranchCreateRequest(
                "BR-001", "Main Branch", "COLLECTION_CENTER",
                "123 Main St", "Suite 100", cityId, "62701",
                "+1234567890", "branch@bloodbank.org",
                "LIC-2024-001", LocalDate.of(2025, 12, 31),
                new BigDecimal("39.7817"), new BigDecimal("-89.6501"),
                null
        );

        sampleUpdateRequest = new BranchUpdateRequest(
                "Updated Branch", "STORAGE_CENTER",
                "456 Oak Ave", null, cityId, "62702",
                "+0987654321", "updated@bloodbank.org",
                "LIC-2024-002", LocalDate.of(2026, 6, 30),
                new BigDecimal("40.0000"), new BigDecimal("-90.0000"),
                null
        );
    }

    // ==================== CREATE BRANCH ====================

    @Nested
    @DisplayName("POST /api/v1/branches")
    class CreateBranch {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should create branch when user is SUPER_ADMIN")
        void createBranch_asSuperAdmin_returns201() throws Exception {
            when(branchService.createBranch(any(BranchCreateRequest.class)))
                    .thenReturn(sampleBranchResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(branchId.toString()))
                    .andExpect(jsonPath("$.data.branchCode").value("BR-001"))
                    .andExpect(jsonPath("$.data.branchName").value("Main Branch"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.message").value("Branch created successfully"));

            verify(branchService).createBranch(any(BranchCreateRequest.class));
        }

        @Test
        @WithMockUser(roles = {"REGIONAL_ADMIN"})
        @DisplayName("Should create branch when user is REGIONAL_ADMIN")
        void createBranch_asRegionalAdmin_returns201() throws Exception {
            when(branchService.createBranch(any(BranchCreateRequest.class)))
                    .thenReturn(sampleBranchResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should deny branch creation for BRANCH_ADMIN")
        void createBranch_asBranchAdmin_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(branchService);
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny branch creation for DONOR role")
        void createBranch_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(branchService);
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated user")
        void createBranch_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should reject invalid request with missing required fields")
        void createBranch_invalidRequest_returns400() throws Exception {
            BranchCreateRequest invalidRequest = new BranchCreateRequest(
                    null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(branchService);
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should reject request with invalid email")
        void createBranch_invalidEmail_returns400() throws Exception {
            BranchCreateRequest invalidEmail = new BranchCreateRequest(
                    "BR-002", "Test Branch", "COLLECTION_CENTER",
                    "123 Main St", null, null, null,
                    null, "not-an-email", null, null,
                    null, null, null
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidEmail)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== UPDATE BRANCH ====================

    @Nested
    @DisplayName("PUT /api/v1/branches/{id}")
    class UpdateBranch {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should update branch as SUPER_ADMIN")
        void updateBranch_asSuperAdmin_returns200() throws Exception {
            when(branchService.updateBranch(eq(branchId), any(BranchUpdateRequest.class)))
                    .thenReturn(sampleBranchResponse);

            mockMvc.perform(put(BASE_URL + "/{id}", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(branchId.toString()))
                    .andExpect(jsonPath("$.message").value("Branch updated successfully"));

            verify(branchService).updateBranch(eq(branchId), any(BranchUpdateRequest.class));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should update branch as BRANCH_ADMIN")
        void updateBranch_asBranchAdmin_returns200() throws Exception {
            when(branchService.updateBranch(eq(branchId), any(BranchUpdateRequest.class)))
                    .thenReturn(sampleBranchResponse);

            mockMvc.perform(put(BASE_URL + "/{id}", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should deny update for BRANCH_MANAGER")
        void updateBranch_asBranchManager_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(branchService);
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated user")
        void updateBranch_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET BRANCH BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/branches/{id}")
    class GetBranchById {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get branch by ID as SUPER_ADMIN")
        void getBranchById_asSuperAdmin_returns200() throws Exception {
            when(branchService.getBranchById(branchId)).thenReturn(sampleBranchResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(branchId.toString()))
                    .andExpect(jsonPath("$.data.branchCode").value("BR-001"))
                    .andExpect(jsonPath("$.data.branchName").value("Main Branch"))
                    .andExpect(jsonPath("$.data.branchType").value("COLLECTION_CENTER"))
                    .andExpect(jsonPath("$.data.addressLine1").value("123 Main St"))
                    .andExpect(jsonPath("$.data.email").value("branch@bloodbank.org"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            verify(branchService).getBranchById(branchId);
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get branch by ID as BRANCH_MANAGER")
        void getBranchById_asBranchManager_returns200() throws Exception {
            when(branchService.getBranchById(branchId)).thenReturn(sampleBranchResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny access for DONOR role")
        void getBranchById_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", branchId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated user")
        void getBranchById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", branchId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET BRANCH BY CODE ====================

    @Nested
    @DisplayName("GET /api/v1/branches/code/{code}")
    class GetBranchByCode {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get branch by code")
        void getBranchByCode_asSuperAdmin_returns200() throws Exception {
            when(branchService.getBranchByCode("BR-001")).thenReturn(sampleBranchResponse);

            mockMvc.perform(get(BASE_URL + "/code/{code}", "BR-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.branchCode").value("BR-001"));

            verify(branchService).getBranchByCode("BR-001");
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get branch by code as BRANCH_ADMIN")
        void getBranchByCode_asBranchAdmin_returns200() throws Exception {
            when(branchService.getBranchByCode("BR-001")).thenReturn(sampleBranchResponse);

            mockMvc.perform(get(BASE_URL + "/code/{code}", "BR-001"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny access for DONOR role")
        void getBranchByCode_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/code/{code}", "BR-001"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET ALL BRANCHES ====================

    @Nested
    @DisplayName("GET /api/v1/branches")
    class GetAllBranches {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get all branches paginated")
        void getAllBranches_asSuperAdmin_returns200() throws Exception {
            PagedResponse<BranchResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleBranchResponse), 0, 20, 1L, 1, true
            );
            when(branchService.getAllBranches(any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].branchCode").value("BR-001"))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.last").value(true));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should allow BRANCH_MANAGER to list branches")
        void getAllBranches_asBranchManager_returns200() throws Exception {
            PagedResponse<BranchResponse> pagedResponse = new PagedResponse<>(
                    List.of(), 0, 20, 0L, 0, true
            );
            when(branchService.getAllBranches(any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR role listing branches")
        void getAllBranches_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated user")
        void getAllBranches_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== SEARCH BRANCHES ====================

    @Nested
    @DisplayName("GET /api/v1/branches/search")
    class SearchBranches {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should search branches by name")
        void searchBranches_asSuperAdmin_returns200() throws Exception {
            PagedResponse<BranchResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleBranchResponse), 0, 20, 1L, 1, true
            );
            when(branchService.searchBranches(eq("Main"), any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/search")
                            .param("name", "Main"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].branchName").value("Main Branch"));

            verify(branchService).searchBranches(eq("Main"), any());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR role searching branches")
        void searchBranches_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/search").param("name", "Main"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET BRANCHES BY STATUS ====================

    @Nested
    @DisplayName("GET /api/v1/branches/status/{status}")
    class GetBranchesByStatus {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get branches by status as SUPER_ADMIN")
        void getBranchesByStatus_asSuperAdmin_returns200() throws Exception {
            when(branchService.getBranchesByStatus("ACTIVE"))
                    .thenReturn(List.of(sampleBranchResponse));

            mockMvc.perform(get(BASE_URL + "/status/{status}", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

            verify(branchService).getBranchesByStatus("ACTIVE");
        }

        @Test
        @WithMockUser(roles = {"REGIONAL_ADMIN"})
        @DisplayName("Should allow REGIONAL_ADMIN to get branches by status")
        void getBranchesByStatus_asRegionalAdmin_returns200() throws Exception {
            when(branchService.getBranchesByStatus("INACTIVE"))
                    .thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/status/{status}", "INACTIVE"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should deny BRANCH_ADMIN from getting branches by status")
        void getBranchesByStatus_asBranchAdmin_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/status/{status}", "ACTIVE"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should deny BRANCH_MANAGER from getting branches by status")
        void getBranchesByStatus_asBranchManager_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/status/{status}", "ACTIVE"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET BRANCHES BY TYPE ====================

    @Nested
    @DisplayName("GET /api/v1/branches/type/{branchType}")
    class GetBranchesByType {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get branches by type as SUPER_ADMIN")
        void getBranchesByType_asSuperAdmin_returns200() throws Exception {
            when(branchService.getBranchesByType("COLLECTION_CENTER"))
                    .thenReturn(List.of(sampleBranchResponse));

            mockMvc.perform(get(BASE_URL + "/type/{branchType}", "COLLECTION_CENTER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].branchType").value("COLLECTION_CENTER"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should deny BRANCH_ADMIN from getting branches by type")
        void getBranchesByType_asBranchAdmin_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/type/{branchType}", "COLLECTION_CENTER"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== ACTIVATE/DEACTIVATE BRANCH ====================

    @Nested
    @DisplayName("PATCH /api/v1/branches/{id}/activate and deactivate")
    class ActivateDeactivateBranch {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should activate branch as SUPER_ADMIN")
        void activateBranch_asSuperAdmin_returns200() throws Exception {
            when(branchService.activateBranch(branchId)).thenReturn(sampleBranchResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/activate", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Branch activated successfully"));

            verify(branchService).activateBranch(branchId);
        }

        @Test
        @WithMockUser(roles = {"REGIONAL_ADMIN"})
        @DisplayName("Should activate branch as REGIONAL_ADMIN")
        void activateBranch_asRegionalAdmin_returns200() throws Exception {
            when(branchService.activateBranch(branchId)).thenReturn(sampleBranchResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/activate", branchId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should deny activation for BRANCH_ADMIN")
        void activateBranch_asBranchAdmin_returns403() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/activate", branchId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should deactivate branch as SUPER_ADMIN")
        void deactivateBranch_asSuperAdmin_returns200() throws Exception {
            when(branchService.deactivateBranch(branchId)).thenReturn(sampleBranchResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Branch deactivated successfully"));

            verify(branchService).deactivateBranch(branchId);
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should deny deactivation for BRANCH_MANAGER")
        void deactivateBranch_asBranchManager_returns403() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", branchId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated activate request")
        void activateBranch_unauthenticated_returns401() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/activate", branchId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== OPERATING HOURS ====================

    @Nested
    @DisplayName("Operating Hours endpoints")
    class OperatingHoursEndpoints {

        private BranchOperatingHoursRequest operatingHoursRequest;
        private BranchOperatingHoursResponse operatingHoursResponse;

        @BeforeEach
        void setUp() {
            operatingHoursRequest = new BranchOperatingHoursRequest(
                    "MONDAY", LocalTime.of(8, 0), LocalTime.of(17, 0), false
            );
            operatingHoursResponse = new BranchOperatingHoursResponse(
                    UUID.randomUUID(), branchId, "MONDAY",
                    LocalTime.of(8, 0), LocalTime.of(17, 0), false
            );
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should add operating hours as SUPER_ADMIN")
        void addOperatingHours_asSuperAdmin_returns201() throws Exception {
            when(branchService.addOperatingHours(eq(branchId), any(BranchOperatingHoursRequest.class)))
                    .thenReturn(operatingHoursResponse);

            mockMvc.perform(post(BASE_URL + "/{branchId}/operating-hours", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(operatingHoursRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.dayOfWeek").value("MONDAY"))
                    .andExpect(jsonPath("$.data.branchId").value(branchId.toString()))
                    .andExpect(jsonPath("$.message").value("Operating hours added successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should add operating hours as BRANCH_ADMIN")
        void addOperatingHours_asBranchAdmin_returns201() throws Exception {
            when(branchService.addOperatingHours(eq(branchId), any(BranchOperatingHoursRequest.class)))
                    .thenReturn(operatingHoursResponse);

            mockMvc.perform(post(BASE_URL + "/{branchId}/operating-hours", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(operatingHoursRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should deny BRANCH_MANAGER from adding operating hours")
        void addOperatingHours_asBranchManager_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{branchId}/operating-hours", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(operatingHoursRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get operating hours")
        void getOperatingHours_asSuperAdmin_returns200() throws Exception {
            when(branchService.getOperatingHours(branchId))
                    .thenReturn(List.of(operatingHoursResponse));

            mockMvc.perform(get(BASE_URL + "/{branchId}/operating-hours", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].dayOfWeek").value("MONDAY"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should allow BRANCH_MANAGER to get operating hours")
        void getOperatingHours_asBranchManager_returns200() throws Exception {
            when(branchService.getOperatingHours(branchId))
                    .thenReturn(List.of(operatingHoursResponse));

            mockMvc.perform(get(BASE_URL + "/{branchId}/operating-hours", branchId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting operating hours")
        void getOperatingHours_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{branchId}/operating-hours", branchId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should reject invalid operating hours request")
        void addOperatingHours_invalidRequest_returns400() throws Exception {
            BranchOperatingHoursRequest invalidRequest = new BranchOperatingHoursRequest(
                    null, null, null, false
            );

            mockMvc.perform(post(BASE_URL + "/{branchId}/operating-hours", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== EQUIPMENT ====================

    @Nested
    @DisplayName("Equipment endpoints")
    class EquipmentEndpoints {

        private BranchEquipmentRequest equipmentRequest;
        private BranchEquipmentResponse equipmentResponse;

        @BeforeEach
        void setUp() {
            equipmentRequest = new BranchEquipmentRequest(
                    "Blood Refrigerator", "STORAGE", "SN-12345",
                    "MedCorp", "BR-2000", LocalDate.of(2023, 1, 15),
                    LocalDate.of(2024, 6, 1), LocalDate.of(2024, 12, 1),
                    "OPERATIONAL"
            );
            equipmentResponse = new BranchEquipmentResponse(
                    UUID.randomUUID(), branchId, "Blood Refrigerator", "STORAGE",
                    "SN-12345", "MedCorp", "BR-2000",
                    LocalDate.of(2023, 1, 15), LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 12, 1), "OPERATIONAL"
            );
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should add equipment as SUPER_ADMIN")
        void addEquipment_asSuperAdmin_returns201() throws Exception {
            when(branchService.addEquipment(eq(branchId), any(BranchEquipmentRequest.class)))
                    .thenReturn(equipmentResponse);

            mockMvc.perform(post(BASE_URL + "/{branchId}/equipment", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(equipmentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.equipmentName").value("Blood Refrigerator"))
                    .andExpect(jsonPath("$.data.equipmentType").value("STORAGE"))
                    .andExpect(jsonPath("$.data.serialNumber").value("SN-12345"))
                    .andExpect(jsonPath("$.message").value("Equipment added successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should add equipment as BRANCH_ADMIN")
        void addEquipment_asBranchAdmin_returns201() throws Exception {
            when(branchService.addEquipment(eq(branchId), any(BranchEquipmentRequest.class)))
                    .thenReturn(equipmentResponse);

            mockMvc.perform(post(BASE_URL + "/{branchId}/equipment", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(equipmentRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should deny BRANCH_MANAGER from adding equipment")
        void addEquipment_asBranchManager_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{branchId}/equipment", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(equipmentRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get equipment for a branch")
        void getEquipment_asSuperAdmin_returns200() throws Exception {
            when(branchService.getEquipment(branchId))
                    .thenReturn(List.of(equipmentResponse));

            mockMvc.perform(get(BASE_URL + "/{branchId}/equipment", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].equipmentName").value("Blood Refrigerator"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should allow BRANCH_MANAGER to get equipment")
        void getEquipment_asBranchManager_returns200() throws Exception {
            when(branchService.getEquipment(branchId))
                    .thenReturn(List.of(equipmentResponse));

            mockMvc.perform(get(BASE_URL + "/{branchId}/equipment", branchId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting equipment")
        void getEquipment_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{branchId}/equipment", branchId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should reject invalid equipment request")
        void addEquipment_invalidRequest_returns400() throws Exception {
            BranchEquipmentRequest invalidRequest = new BranchEquipmentRequest(
                    null, null, null, null, null,
                    null, null, null, null
            );

            mockMvc.perform(post(BASE_URL + "/{branchId}/equipment", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== BRANCH REGIONS ====================

    @Nested
    @DisplayName("Branch Region endpoints")
    class BranchRegionEndpoints {

        private BranchRegionRequest regionRequest;
        private BranchRegionResponse regionResponse;

        @BeforeEach
        void setUp() {
            regionRequest = new BranchRegionRequest(regionId, true);
            regionResponse = new BranchRegionResponse(
                    UUID.randomUUID(), branchId, regionId, "Midwest", true
            );
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should add region to branch as SUPER_ADMIN")
        void addBranchRegion_asSuperAdmin_returns201() throws Exception {
            when(branchService.addBranchRegion(eq(branchId), any(BranchRegionRequest.class)))
                    .thenReturn(regionResponse);

            mockMvc.perform(post(BASE_URL + "/{branchId}/regions", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regionRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.regionId").value(regionId.toString()))
                    .andExpect(jsonPath("$.data.regionName").value("Midwest"))
                    .andExpect(jsonPath("$.data.isPrimary").value(true))
                    .andExpect(jsonPath("$.message").value("Region assigned to branch successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should add region to branch as BRANCH_ADMIN")
        void addBranchRegion_asBranchAdmin_returns201() throws Exception {
            when(branchService.addBranchRegion(eq(branchId), any(BranchRegionRequest.class)))
                    .thenReturn(regionResponse);

            mockMvc.perform(post(BASE_URL + "/{branchId}/regions", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regionRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should deny BRANCH_MANAGER from adding region")
        void addBranchRegion_asBranchManager_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{branchId}/regions", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regionRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get regions for a branch")
        void getBranchRegions_asSuperAdmin_returns200() throws Exception {
            when(branchService.getBranchRegions(branchId))
                    .thenReturn(List.of(regionResponse));

            mockMvc.perform(get(BASE_URL + "/{branchId}/regions", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].regionName").value("Midwest"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should allow BRANCH_MANAGER to get branch regions")
        void getBranchRegions_asBranchManager_returns200() throws Exception {
            when(branchService.getBranchRegions(branchId))
                    .thenReturn(List.of(regionResponse));

            mockMvc.perform(get(BASE_URL + "/{branchId}/regions", branchId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should remove region from branch")
        void removeBranchRegion_asSuperAdmin_returns200() throws Exception {
            doNothing().when(branchService).removeBranchRegion(branchId, regionId);

            mockMvc.perform(delete(BASE_URL + "/{branchId}/regions/{regionId}", branchId, regionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Region removed from branch successfully"));

            verify(branchService).removeBranchRegion(branchId, regionId);
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should allow BRANCH_ADMIN to remove region")
        void removeBranchRegion_asBranchAdmin_returns200() throws Exception {
            doNothing().when(branchService).removeBranchRegion(branchId, regionId);

            mockMvc.perform(delete(BASE_URL + "/{branchId}/regions/{regionId}", branchId, regionId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should deny BRANCH_MANAGER from removing region")
        void removeBranchRegion_asBranchManager_returns403() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{branchId}/regions/{regionId}", branchId, regionId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting branch regions")
        void getBranchRegions_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{branchId}/regions", branchId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated region removal")
        void removeBranchRegion_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{branchId}/regions/{regionId}", branchId, regionId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should reject region request with null regionId")
        void addBranchRegion_nullRegionId_returns400() throws Exception {
            BranchRegionRequest invalidRequest = new BranchRegionRequest(null, false);

            mockMvc.perform(post(BASE_URL + "/{branchId}/regions", branchId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}
