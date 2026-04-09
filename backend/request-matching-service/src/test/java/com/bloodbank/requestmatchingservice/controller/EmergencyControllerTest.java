package com.bloodbank.requestmatchingservice.controller;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.BranchDataFilterAspect;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestCreateRequest;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestResponse;
import com.bloodbank.requestmatchingservice.enums.EmergencyPriorityEnum;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;
import com.bloodbank.requestmatchingservice.service.EmergencyService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = EmergencyController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {GlobalExceptionHandler.class, BranchDataFilterAspect.class}))
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class EmergencyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private EmergencyService emergencyService;
    @MockBean private JwtDecoder jwtDecoder;

    private UUID requestId;
    private UUID branchId;
    private UUID bloodGroupId;
    private UUID componentTypeId;
    private UUID hospitalId;
    private EmergencyRequestResponse sampleResponse;
    private EmergencyRequestCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();

        sampleResponse = new EmergencyRequestResponse(
                requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                "Dr. Smith", Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                false, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());

        sampleCreateRequest = new EmergencyRequestCreateRequest(
                hospitalId, bloodGroupId, componentTypeId, 4,
                EmergencyPriorityEnum.EMERGENCY, "John Doe", "Trauma",
                "Dr. Smith", Instant.now().plusSeconds(3600), null, null, branchId);
    }

    @Nested
    @DisplayName("POST /api/v1/emergencies")
    class CreateEmergencyRequest {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should create emergency request as DOCTOR")
        void asDoctor_returns201() throws Exception {
            when(emergencyService.createEmergencyRequest(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/emergencies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.patientName").value("John Doe"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should create emergency request as BRANCH_MANAGER")
        void asBranchManager_returns201() throws Exception {
            when(emergencyService.createEmergencyRequest(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/emergencies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on emergency creation")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/emergencies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR on emergency creation")
        void asDonor_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/emergencies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/emergencies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/emergencies/{id}")
    class GetById {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get emergency by ID as DOCTOR")
        void asDoctor_returns200() throws Exception {
            when(emergencyService.getById(requestId)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/emergencies/" + requestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.requestNumber").value("ER-ABCD1234"));
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on get emergency")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/emergencies/" + requestId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/emergencies/" + requestId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/emergencies/number/{requestNumber}")
    class GetByRequestNumber {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get emergency by request number")
        void asDoctor_returns200() throws Exception {
            when(emergencyService.getByRequestNumber("ER-ABCD1234")).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/emergencies/number/ER-ABCD1234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.requestNumber").value("ER-ABCD1234"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/emergencies/status/{status}")
    class GetByStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get emergencies by status")
        void success() throws Exception {
            PagedResponse<EmergencyRequestResponse> paged = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(emergencyService.getByStatus(eq(EmergencyStatusEnum.OPEN), any())).thenReturn(paged);

            mockMvc.perform(get("/api/v1/emergencies/status/OPEN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/emergencies/{id}/escalate")
    class Escalate {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should escalate emergency as DOCTOR")
        void asDoctor_returns200() throws Exception {
            EmergencyRequestResponse escalatedResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.CRITICAL, "John Doe", null,
                    "Dr. Smith", Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyService.escalate(requestId)).thenReturn(escalatedResponse);

            mockMvc.perform(put("/api/v1/emergencies/" + requestId + "/escalate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.priority").value("CRITICAL"));
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on escalation")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(put("/api/v1/emergencies/" + requestId + "/escalate"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/emergencies/{id}/cancel")
    class Cancel {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should cancel emergency as DOCTOR")
        void asDoctor_returns200() throws Exception {
            EmergencyRequestResponse cancelledResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                    "Dr. Smith", Instant.now().plusSeconds(3600), EmergencyStatusEnum.CANCELLED,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyService.cancel(requestId)).thenReturn(cancelledResponse);

            mockMvc.perform(put("/api/v1/emergencies/" + requestId + "/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/emergencies/{id}/broadcast")
    class MarkBroadcastSent {

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should mark broadcast as sent")
        void success() throws Exception {
            EmergencyRequestResponse broadcastResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                    "Dr. Smith", Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                    true, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyService.markBroadcastSent(requestId)).thenReturn(broadcastResponse);

            mockMvc.perform(put("/api/v1/emergencies/" + requestId + "/broadcast"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.broadcastSent").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/emergencies/hospital/{hospitalId}")
    class GetByHospital {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get emergencies by hospital")
        void success() throws Exception {
            when(emergencyService.getByHospital(hospitalId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/emergencies/hospital/" + hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
