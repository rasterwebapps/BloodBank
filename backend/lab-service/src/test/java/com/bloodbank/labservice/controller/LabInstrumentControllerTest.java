package com.bloodbank.labservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.labservice.dto.LabInstrumentCreateRequest;
import com.bloodbank.labservice.dto.LabInstrumentResponse;
import com.bloodbank.labservice.enums.InstrumentStatusEnum;
import com.bloodbank.labservice.enums.InstrumentTypeEnum;
import com.bloodbank.labservice.service.LabInstrumentService;
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

@WebMvcTest(value = LabInstrumentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class LabInstrumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LabInstrumentService labInstrumentService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/instruments";
    private UUID instrumentId;
    private UUID branchId;
    private LabInstrumentResponse sampleResponse;
    private LabInstrumentCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        instrumentId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new LabInstrumentResponse(
                instrumentId, "INS-ABCD1234", "Blood Analyzer X1", InstrumentTypeEnum.ANALYZER,
                "Beckman Coulter", "DXH 900", "SN-12345",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 12, 1), InstrumentStatusEnum.ACTIVE,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new LabInstrumentCreateRequest(
                "Blood Analyzer X1", InstrumentTypeEnum.ANALYZER,
                "Beckman Coulter", "DXH 900", "SN-12345",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 12, 1), branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/instruments")
    class CreateInstrument {

        @Test
        @DisplayName("should create instrument as LAB_TECHNICIAN — 201")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldCreateInstrumentAsLabTechnician() throws Exception {
            when(labInstrumentService.createInstrument(any(LabInstrumentCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.instrumentName").value("Blood Analyzer X1"));
        }

        @Test
        @DisplayName("should create instrument as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreateInstrumentAsBranchManager() throws Exception {
            when(labInstrumentService.createInstrument(any(LabInstrumentCreateRequest.class)))
                    .thenReturn(sampleResponse);

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
        @DisplayName("should reject unauthenticated request — 401")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/instruments/{id}")
    class GetInstrumentById {

        @Test
        @DisplayName("should return instrument as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnInstrumentAsLabTechnician() throws Exception {
            when(labInstrumentService.getInstrumentById(instrumentId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", instrumentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(instrumentId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", instrumentId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/instruments/branch/{branchId}")
    class GetInstrumentsByBranch {

        @Test
        @DisplayName("should return instruments by branch as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnInstrumentsByBranchAsBranchManager() throws Exception {
            when(labInstrumentService.getInstrumentsByBranch(branchId))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/branch/{branchId}", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/instruments/{id}/status")
    class UpdateInstrumentStatus {

        @Test
        @DisplayName("should update status as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldUpdateStatusAsBranchManager() throws Exception {
            when(labInstrumentService.updateInstrumentStatus(eq(instrumentId), eq(InstrumentStatusEnum.UNDER_MAINTENANCE)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", instrumentId)
                            .param("status", "UNDER_MAINTENANCE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should update status as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldUpdateStatusAsLabTechnician() throws Exception {
            when(labInstrumentService.updateInstrumentStatus(eq(instrumentId), eq(InstrumentStatusEnum.RETIRED)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", instrumentId)
                            .param("status", "RETIRED"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/status", instrumentId)
                            .param("status", "RETIRED"))
                    .andExpect(status().isForbidden());
        }
    }
}
