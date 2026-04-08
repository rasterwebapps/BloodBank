package com.bloodbank.hospitalservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.hospitalservice.dto.HospitalRequestCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalRequestResponse;
import com.bloodbank.hospitalservice.enums.HospitalRequestStatusEnum;
import com.bloodbank.hospitalservice.enums.PriorityEnum;
import com.bloodbank.hospitalservice.service.BloodRequestService;
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

@WebMvcTest(value = HospitalRequestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class HospitalRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BloodRequestService bloodRequestService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/hospital-requests";
    private UUID requestId;
    private UUID hospitalId;
    private UUID branchId;
    private UUID bloodGroupId;
    private UUID componentTypeId;
    private HospitalRequestResponse sampleResponse;
    private HospitalRequestCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();

        sampleResponse = new HospitalRequestResponse(
                requestId, hospitalId, "REQ-ABCD1234",
                "Jane Doe", "PAT-001", bloodGroupId, componentTypeId,
                3, PriorityEnum.URGENT, Instant.now(),
                "Surgery preparation", null, "Dr. Johnson", "DOC-001",
                HospitalRequestStatusEnum.PENDING, 0, null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new HospitalRequestCreateRequest(
                hospitalId, "Jane Doe", "PAT-001", bloodGroupId,
                componentTypeId, 3, PriorityEnum.URGENT,
                Instant.now(), "Surgery preparation", null,
                "Dr. Johnson", "DOC-001", null, branchId
        );
    }

    // ==================== CREATE REQUEST ====================

    @Nested
    @DisplayName("POST /api/v1/hospital-requests")
    class CreateRequest {

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should create request as HOSPITAL_USER - 201")
        void createRequest_asHospitalUser_returns201() throws Exception {
            when(bloodRequestService.createRequest(any(HospitalRequestCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.patientName").value("Jane Doe"))
                    .andExpect(jsonPath("$.data.requestNumber").value("REQ-ABCD1234"))
                    .andExpect(jsonPath("$.message").value("Blood request submitted successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should create request as BRANCH_ADMIN - 201")
        void createRequest_asBranchAdmin_returns201() throws Exception {
            when(bloodRequestService.createRequest(any(HospitalRequestCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should create request as BRANCH_MANAGER - 201")
        void createRequest_asBranchManager_returns201() throws Exception {
            when(bloodRequestService.createRequest(any(HospitalRequestCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should create request as DOCTOR - 201")
        void createRequest_asDoctor_returns201() throws Exception {
            when(bloodRequestService.createRequest(any(HospitalRequestCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from creating request - 403")
        void createRequest_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should deny RECEPTIONIST from creating request - 403")
        void createRequest_asReceptionist_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void createRequest_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET REQUEST BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/hospital-requests/{id}")
    class GetRequestById {

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get request by ID as HOSPITAL_USER - 200")
        void getRequestById_asHospitalUser_returns200() throws Exception {
            when(bloodRequestService.getRequestById(requestId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", requestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(requestId.toString()));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get request by ID as BRANCH_MANAGER - 200")
        void getRequestById_asBranchManager_returns200() throws Exception {
            when(bloodRequestService.getRequestById(requestId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", requestId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting request - 403")
        void getRequestById_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", requestId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void getRequestById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", requestId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET ALL REQUESTS ====================

    @Nested
    @DisplayName("GET /api/v1/hospital-requests")
    class GetAllRequests {

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should list all requests as BRANCH_MANAGER - 200")
        void getAllRequests_asBranchManager_returns200() throws Exception {
            PagedResponse<HospitalRequestResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(bloodRequestService.getAllRequests(any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should list all requests as HOSPITAL_USER - 200")
        void getAllRequests_asHospitalUser_returns200() throws Exception {
            PagedResponse<HospitalRequestResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(bloodRequestService.getAllRequests(any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from listing requests - 403")
        void getAllRequests_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET REQUEST BY NUMBER ====================

    @Nested
    @DisplayName("GET /api/v1/hospital-requests/number/{requestNumber}")
    class GetRequestByNumber {

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get request by number as HOSPITAL_USER - 200")
        void getRequestByNumber_asHospitalUser_returns200() throws Exception {
            when(bloodRequestService.getRequestByNumber("REQ-ABCD1234")).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/number/{requestNumber}", "REQ-ABCD1234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.requestNumber").value("REQ-ABCD1234"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting request by number - 403")
        void getRequestByNumber_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/number/{requestNumber}", "REQ-ABCD1234"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET REQUESTS BY HOSPITAL ====================

    @Nested
    @DisplayName("GET /api/v1/hospital-requests/hospital/{hospitalId}")
    class GetRequestsByHospital {

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get requests by hospital as HOSPITAL_USER - 200")
        void getRequestsByHospital_asHospitalUser_returns200() throws Exception {
            PagedResponse<HospitalRequestResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(bloodRequestService.getRequestsByHospitalId(eq(hospitalId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/hospital/{hospitalId}", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting requests by hospital - 403")
        void getRequestsByHospital_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/hospital/{hospitalId}", hospitalId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET REQUESTS BY STATUS ====================

    @Nested
    @DisplayName("GET /api/v1/hospital-requests/status/{status}")
    class GetRequestsByStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get requests by status as BRANCH_ADMIN - 200")
        void getRequestsByStatus_asBranchAdmin_returns200() throws Exception {
            PagedResponse<HospitalRequestResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(bloodRequestService.getRequestsByStatus(eq(HospitalRequestStatusEnum.PENDING), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/status/{status}", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting requests by status - 403")
        void getRequestsByStatus_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/status/{status}", "PENDING"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER from getting requests by status - 403")
        void getRequestsByStatus_asHospitalUser_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/status/{status}", "PENDING"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== UPDATE REQUEST STATUS ====================

    @Nested
    @DisplayName("PUT /api/v1/hospital-requests/{id}/status")
    class UpdateRequestStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should update request status as BRANCH_ADMIN - 200")
        void updateRequestStatus_asBranchAdmin_returns200() throws Exception {
            when(bloodRequestService.updateRequestStatus(eq(requestId), eq(HospitalRequestStatusEnum.APPROVED), any()))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/status", requestId)
                            .param("status", "APPROVED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Request status updated successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should update request status as BRANCH_MANAGER - 200")
        void updateRequestStatus_asBranchManager_returns200() throws Exception {
            when(bloodRequestService.updateRequestStatus(eq(requestId), eq(HospitalRequestStatusEnum.APPROVED), any()))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/status", requestId)
                            .param("status", "APPROVED"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER from updating request status - 403")
        void updateRequestStatus_asHospitalUser_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/status", requestId)
                            .param("status", "APPROVED"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from updating request status - 403")
        void updateRequestStatus_asDonor_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/status", requestId)
                            .param("status", "APPROVED"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void updateRequestStatus_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/status", requestId)
                            .param("status", "APPROVED"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== CANCEL REQUEST ====================

    @Nested
    @DisplayName("PUT /api/v1/hospital-requests/{id}/cancel")
    class CancelRequest {

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should cancel request as HOSPITAL_USER - 200")
        void cancelRequest_asHospitalUser_returns200() throws Exception {
            when(bloodRequestService.cancelRequest(requestId)).thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/cancel", requestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Request cancelled successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should cancel request as BRANCH_ADMIN - 200")
        void cancelRequest_asBranchAdmin_returns200() throws Exception {
            when(bloodRequestService.cancelRequest(requestId)).thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/cancel", requestId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from cancelling request - 403")
        void cancelRequest_asDonor_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/cancel", requestId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void cancelRequest_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/cancel", requestId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
