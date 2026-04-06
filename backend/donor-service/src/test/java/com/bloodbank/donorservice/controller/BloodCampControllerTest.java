package com.bloodbank.donorservice.controller;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.donorservice.dto.*;
import com.bloodbank.donorservice.enums.CampDonorStatusEnum;
import com.bloodbank.donorservice.enums.CampStatusEnum;
import com.bloodbank.donorservice.service.BloodCampService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = BloodCampController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class BloodCampControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BloodCampService bloodCampService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/camps";
    private UUID campId;
    private UUID branchId;
    private UUID collectionId;
    private BloodCampResponse sampleCampResponse;
    private BloodCampCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        campId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        collectionId = UUID.randomUUID();

        sampleCampResponse = new BloodCampResponse(
                campId, "CAMP-001", "Community Blood Drive", "Red Cross",
                "+1234567890", "Community Center", "123 Main St",
                UUID.randomUUID(), new BigDecimal("39.7817"), new BigDecimal("-89.6501"),
                LocalDate.now().plusDays(7), null, null,
                50, 0, 0, CampStatusEnum.PLANNED,
                "COORD-001", "Annual drive", branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new BloodCampCreateRequest(
                "Community Blood Drive", "Red Cross", "+1234567890",
                "Community Center", "123 Main St", UUID.randomUUID(),
                new BigDecimal("39.7817"), new BigDecimal("-89.6501"),
                LocalDate.now().plusDays(7), 50, "COORD-001",
                "Annual drive", branchId
        );
    }

    // ==================== CREATE CAMP ====================

    @Nested
    @DisplayName("POST /api/v1/camps")
    class CreateCamp {

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("Should create camp as CAMP_COORDINATOR")
        void createCamp_asCampCoordinator_returns201() throws Exception {
            when(bloodCampService.createCamp(any(BloodCampCreateRequest.class)))
                    .thenReturn(sampleCampResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(campId.toString()))
                    .andExpect(jsonPath("$.data.campName").value("Community Blood Drive"))
                    .andExpect(jsonPath("$.message").value("Blood camp created successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from creating camp")
        void createCamp_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void createCamp_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== UPDATE CAMP ====================

    @Nested
    @DisplayName("PUT /api/v1/camps/{id}")
    class UpdateCamp {

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("Should update camp as CAMP_COORDINATOR")
        void updateCamp_asCampCoordinator_returns200() throws Exception {
            BloodCampUpdateRequest updateRequest = new BloodCampUpdateRequest(
                    "Updated Blood Drive", "Red Cross Updated", "+0987654321",
                    "New Venue", "456 Oak Ave", UUID.randomUUID(),
                    new BigDecimal("40.0"), new BigDecimal("-90.0"),
                    LocalDate.now().plusDays(14), 75, "COORD-002", "Updated notes"
            );
            when(bloodCampService.updateCamp(eq(campId), any(BloodCampUpdateRequest.class)))
                    .thenReturn(sampleCampResponse);

            mockMvc.perform(put(BASE_URL + "/{id}", campId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Blood camp updated successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from updating camp")
        void updateCamp_asDonor_returns403() throws Exception {
            BloodCampUpdateRequest updateRequest = new BloodCampUpdateRequest(
                    "Updated", null, null, null, null, null,
                    null, null, null, null, null, null
            );
            mockMvc.perform(put(BASE_URL + "/{id}", campId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET CAMP BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/camps/{id}")
    class GetCampById {

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("Should get camp by ID as CAMP_COORDINATOR")
        void getCampById_asCampCoordinator_returns200() throws Exception {
            when(bloodCampService.getCampById(campId)).thenReturn(sampleCampResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", campId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.campCode").value("CAMP-001"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting camp")
        void getCampById_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", campId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET CAMPS BY STATUS ====================

    @Nested
    @DisplayName("GET /api/v1/camps/status/{status}")
    class GetCampsByStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get camps by status as BRANCH_MANAGER")
        void getCampsByStatus_asBranchManager_returns200() throws Exception {
            PagedResponse<BloodCampResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleCampResponse), 0, 20, 1, 1, true
            );
            when(bloodCampService.getCampsByStatus(eq(CampStatusEnum.PLANNED), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/status/{status}", "PLANNED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting camps by status")
        void getCampsByStatus_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/status/{status}", "PLANNED"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== START CAMP ====================

    @Nested
    @DisplayName("PATCH /api/v1/camps/{id}/start")
    class StartCamp {

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("Should start camp as CAMP_COORDINATOR")
        void startCamp_asCampCoordinator_returns200() throws Exception {
            when(bloodCampService.startCamp(campId)).thenReturn(sampleCampResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/start", campId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Blood camp started successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from starting camp")
        void startCamp_asDonor_returns403() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/start", campId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== COMPLETE CAMP ====================

    @Nested
    @DisplayName("PATCH /api/v1/camps/{id}/complete")
    class CompleteCamp {

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("Should complete camp as CAMP_COORDINATOR")
        void completeCamp_asCampCoordinator_returns200() throws Exception {
            when(bloodCampService.completeCamp(campId)).thenReturn(sampleCampResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/complete", campId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Blood camp completed successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from completing camp")
        void completeCamp_asDonor_returns403() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/complete", campId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== CAMP RESOURCES ====================

    @Nested
    @DisplayName("Camp Resource endpoints")
    class CampResourceEndpoints {

        private CampResourceCreateRequest resourceRequest;
        private CampResourceResponse resourceResponse;

        @BeforeEach
        void setUp() {
            resourceRequest = new CampResourceCreateRequest(
                    campId, "EQUIPMENT", "Blood Pressure Monitor", 5, "Standard", branchId
            );
            resourceResponse = new CampResourceResponse(
                    UUID.randomUUID(), campId, "EQUIPMENT", "Blood Pressure Monitor",
                    5, "Standard", branchId, LocalDateTime.now()
            );
        }

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("Should add resource as CAMP_COORDINATOR")
        void addResource_asCampCoordinator_returns201() throws Exception {
            when(bloodCampService.addResource(any(CampResourceCreateRequest.class)))
                    .thenReturn(resourceResponse);

            mockMvc.perform(post(BASE_URL + "/{campId}/resources", campId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resourceRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Resource added successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get resources as BRANCH_MANAGER")
        void getResources_asBranchManager_returns200() throws Exception {
            when(bloodCampService.getResources(campId))
                    .thenReturn(List.of(resourceResponse));

            mockMvc.perform(get(BASE_URL + "/{campId}/resources", campId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from adding resource")
        void addResource_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{campId}/resources", campId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resourceRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== CAMP DONORS ====================

    @Nested
    @DisplayName("Camp Donor endpoints")
    class CampDonorEndpoints {

        private CampDonorCreateRequest donorRequest;
        private CampDonorResponse donorResponse;

        @BeforeEach
        void setUp() {
            UUID donorId = UUID.randomUUID();
            donorRequest = new CampDonorCreateRequest(campId, donorId, branchId);
            donorResponse = new CampDonorResponse(
                    UUID.randomUUID(), campId, donorId, LocalDateTime.now(),
                    CampDonorStatusEnum.REGISTERED, branchId, LocalDateTime.now()
            );
        }

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("Should register donor for camp as CAMP_COORDINATOR")
        void registerDonor_asCampCoordinator_returns201() throws Exception {
            when(bloodCampService.registerDonor(any(CampDonorCreateRequest.class)))
                    .thenReturn(donorResponse);

            mockMvc.perform(post(BASE_URL + "/{campId}/donors", campId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(donorRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Donor registered for camp successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get camp donors as BRANCH_MANAGER")
        void getCampDonors_asBranchManager_returns200() throws Exception {
            when(bloodCampService.getCampDonors(campId))
                    .thenReturn(List.of(donorResponse));

            mockMvc.perform(get(BASE_URL + "/{campId}/donors", campId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from registering for camp")
        void registerDonor_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{campId}/donors", campId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(donorRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== LINK COLLECTION ====================

    @Nested
    @DisplayName("POST /api/v1/camps/{campId}/collections/{collectionId}")
    class LinkCollection {

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("Should link collection as CAMP_COORDINATOR")
        void linkCollection_asCampCoordinator_returns200() throws Exception {
            doNothing().when(bloodCampService).linkCollection(campId, collectionId);

            mockMvc.perform(post(BASE_URL + "/{campId}/collections/{collectionId}", campId, collectionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Collection linked to camp successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from linking collection")
        void linkCollection_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{campId}/collections/{collectionId}", campId, collectionId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void linkCollection_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{campId}/collections/{collectionId}", campId, collectionId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
