package com.bloodbank.requestmatchingservice.controller;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.BranchDataFilterAspect;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.requestmatchingservice.dto.DisasterEventCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DisasterEventResponse;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationResponse;
import com.bloodbank.requestmatchingservice.enums.DisasterSeverityEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterStatusEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterTypeEnum;
import com.bloodbank.requestmatchingservice.enums.MobilizationStatusEnum;
import com.bloodbank.requestmatchingservice.enums.MobilizationTypeEnum;
import com.bloodbank.requestmatchingservice.service.DisasterResponseService;
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

@WebMvcTest(value = DisasterResponseController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {GlobalExceptionHandler.class, BranchDataFilterAspect.class}))
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class DisasterResponseControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DisasterResponseService disasterResponseService;
    @MockBean private JwtDecoder jwtDecoder;

    private UUID disasterId;
    private UUID branchId;
    private UUID donorId;
    private UUID mobilizationId;
    private DisasterEventResponse sampleDisasterResponse;
    private DisasterEventCreateRequest sampleDisasterCreateRequest;
    private DonorMobilizationResponse sampleMobilizationResponse;
    private DonorMobilizationCreateRequest sampleMobilizationCreateRequest;

    @BeforeEach
    void setUp() {
        disasterId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        donorId = UUID.randomUUID();
        mobilizationId = UUID.randomUUID();

        sampleDisasterResponse = new DisasterEventResponse(
                disasterId, "DE-ABCD1234", "Earthquake Relief",
                DisasterTypeEnum.NATURAL_DISASTER, DisasterSeverityEnum.HIGH,
                "City Center", null, Instant.now(), null, 200, 500,
                "Coordinator A", "+123456789", DisasterStatusEnum.ACTIVE,
                null, branchId, LocalDateTime.now(), LocalDateTime.now());

        sampleDisasterCreateRequest = new DisasterEventCreateRequest(
                "Earthquake Relief", DisasterTypeEnum.NATURAL_DISASTER,
                DisasterSeverityEnum.HIGH, "City Center", null,
                Instant.now(), 200, 500, "Coordinator A", "+123456789",
                null, branchId);

        sampleMobilizationResponse = new DonorMobilizationResponse(
                mobilizationId, disasterId, null, donorId,
                MobilizationTypeEnum.SMS, Instant.now(), null, null,
                null, false, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());

        sampleMobilizationCreateRequest = new DonorMobilizationCreateRequest(
                disasterId, null, donorId, MobilizationTypeEnum.SMS,
                Instant.now().plusSeconds(7200), null, branchId);
    }

    @Nested
    @DisplayName("POST /api/v1/disasters")
    class CreateDisasterEvent {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should create disaster event as SUPER_ADMIN")
        void asSuperAdmin_returns201() throws Exception {
            when(disasterResponseService.createDisasterEvent(any())).thenReturn(sampleDisasterResponse);

            mockMvc.perform(post("/api/v1/disasters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDisasterCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.eventName").value("Earthquake Relief"));
        }

        @Test
        @WithMockUser(roles = {"REGIONAL_ADMIN"})
        @DisplayName("Should create disaster event as REGIONAL_ADMIN")
        void asRegionalAdmin_returns201() throws Exception {
            when(disasterResponseService.createDisasterEvent(any())).thenReturn(sampleDisasterResponse);

            mockMvc.perform(post("/api/v1/disasters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDisasterCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on disaster creation")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/disasters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDisasterCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should deny DOCTOR on disaster creation")
        void asDoctor_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/disasters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDisasterCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/disasters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDisasterCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/disasters/{id}")
    class GetById {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get disaster event by ID as SUPER_ADMIN")
        void asSuperAdmin_returns200() throws Exception {
            when(disasterResponseService.getDisasterEventById(disasterId))
                    .thenReturn(sampleDisasterResponse);

            mockMvc.perform(get("/api/v1/disasters/" + disasterId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.eventCode").value("DE-ABCD1234"));
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on get disaster")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/disasters/" + disasterId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/disasters/" + disasterId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/disasters/code/{eventCode}")
    class GetByEventCode {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get disaster by event code")
        void success() throws Exception {
            when(disasterResponseService.getByEventCode("DE-ABCD1234"))
                    .thenReturn(sampleDisasterResponse);

            mockMvc.perform(get("/api/v1/disasters/code/DE-ABCD1234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.eventCode").value("DE-ABCD1234"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/disasters/status/{status}")
    class GetByStatus {

        @Test
        @WithMockUser(roles = {"REGIONAL_ADMIN"})
        @DisplayName("Should get disasters by status")
        void success() throws Exception {
            PagedResponse<DisasterEventResponse> paged = new PagedResponse<>(
                    List.of(sampleDisasterResponse), 0, 20, 1, 1, true);
            when(disasterResponseService.getByStatus(eq(DisasterStatusEnum.ACTIVE), any()))
                    .thenReturn(paged);

            mockMvc.perform(get("/api/v1/disasters/status/ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/disasters/active")
    class GetActiveEvents {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get active events")
        void success() throws Exception {
            when(disasterResponseService.getActiveEvents())
                    .thenReturn(List.of(sampleDisasterResponse));

            mockMvc.perform(get("/api/v1/disasters/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/disasters/{id}/escalate")
    class EscalateEvent {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should escalate disaster event as SUPER_ADMIN")
        void asSuperAdmin_returns200() throws Exception {
            DisasterEventResponse escalatedResponse = new DisasterEventResponse(
                    disasterId, "DE-ABCD1234", "Earthquake Relief",
                    DisasterTypeEnum.NATURAL_DISASTER, DisasterSeverityEnum.CRITICAL,
                    "City Center", null, Instant.now(), null, 200, 500,
                    "Coordinator A", "+123456789", DisasterStatusEnum.ESCALATED,
                    null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(disasterResponseService.escalateEvent(disasterId)).thenReturn(escalatedResponse);

            mockMvc.perform(put("/api/v1/disasters/" + disasterId + "/escalate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.severity").value("CRITICAL"));
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on escalation")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(put("/api/v1/disasters/" + disasterId + "/escalate"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/disasters/{id}/close")
    class CloseEvent {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should close disaster event as SUPER_ADMIN")
        void success() throws Exception {
            DisasterEventResponse closedResponse = new DisasterEventResponse(
                    disasterId, "DE-ABCD1234", "Earthquake Relief",
                    DisasterTypeEnum.NATURAL_DISASTER, DisasterSeverityEnum.HIGH,
                    "City Center", null, Instant.now(), Instant.now(), 200, 500,
                    "Coordinator A", "+123456789", DisasterStatusEnum.CLOSED,
                    null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(disasterResponseService.closeEvent(disasterId)).thenReturn(closedResponse);

            mockMvc.perform(put("/api/v1/disasters/" + disasterId + "/close"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CLOSED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/disasters/{disasterEventId}/mobilizations")
    class MobilizeDonor {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should mobilize donor as SUPER_ADMIN")
        void asSuperAdmin_returns201() throws Exception {
            when(disasterResponseService.mobilizeDonor(any())).thenReturn(sampleMobilizationResponse);

            mockMvc.perform(post("/api/v1/disasters/" + disasterId + "/mobilizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleMobilizationCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER on mobilization")
        void asHospitalUser_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/disasters/" + disasterId + "/mobilizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleMobilizationCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/disasters/" + disasterId + "/mobilizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleMobilizationCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/disasters/mobilizations/{id}")
    class GetMobilizationById {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get mobilization by ID")
        void success() throws Exception {
            when(disasterResponseService.getMobilizationById(mobilizationId))
                    .thenReturn(sampleMobilizationResponse);

            mockMvc.perform(get("/api/v1/disasters/mobilizations/" + mobilizationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.donorId").value(donorId.toString()));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/disasters/{disasterEventId}/mobilizations")
    class GetMobilizationsByDisaster {

        @Test
        @WithMockUser(roles = {"REGIONAL_ADMIN"})
        @DisplayName("Should get mobilizations for disaster event")
        void success() throws Exception {
            PagedResponse<DonorMobilizationResponse> paged = new PagedResponse<>(
                    List.of(sampleMobilizationResponse), 0, 20, 1, 1, true);
            when(disasterResponseService.getMobilizationsByDisasterPaged(eq(disasterId), any()))
                    .thenReturn(paged);

            mockMvc.perform(get("/api/v1/disasters/" + disasterId + "/mobilizations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/disasters/mobilizations/{id}/response")
    class RecordResponse {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should record donor response")
        void success() throws Exception {
            DonorMobilizationResponse acceptedResponse = new DonorMobilizationResponse(
                    mobilizationId, disasterId, null, donorId,
                    MobilizationTypeEnum.SMS, Instant.now(), MobilizationStatusEnum.ACCEPTED,
                    Instant.now(), null, false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(disasterResponseService.recordResponse(eq(mobilizationId), eq(MobilizationStatusEnum.ACCEPTED)))
                    .thenReturn(acceptedResponse);

            mockMvc.perform(put("/api/v1/disasters/mobilizations/" + mobilizationId + "/response")
                            .param("response", "ACCEPTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.response").value("ACCEPTED"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/disasters/mobilizations/{id}/complete")
    class MarkDonationCompleted {

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should mark donation as completed")
        void success() throws Exception {
            UUID collectionId = UUID.randomUUID();
            DonorMobilizationResponse completedResponse = new DonorMobilizationResponse(
                    mobilizationId, disasterId, null, donorId,
                    MobilizationTypeEnum.SMS, Instant.now(), MobilizationStatusEnum.ACCEPTED,
                    Instant.now(), null, true, collectionId, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(disasterResponseService.markDonationCompleted(eq(mobilizationId), any(UUID.class)))
                    .thenReturn(completedResponse);

            mockMvc.perform(put("/api/v1/disasters/mobilizations/" + mobilizationId + "/complete")
                            .param("collectionId", collectionId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.donationCompleted").value(true));
        }
    }
}
