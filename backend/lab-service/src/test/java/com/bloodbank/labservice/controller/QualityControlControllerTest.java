package com.bloodbank.labservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.labservice.dto.QualityControlCreateRequest;
import com.bloodbank.labservice.dto.QualityControlResponse;
import com.bloodbank.labservice.enums.QcLevelEnum;
import com.bloodbank.labservice.enums.QcStatusEnum;
import com.bloodbank.labservice.service.QualityControlService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = QualityControlController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class QualityControlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QualityControlService qualityControlService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/qc";
    private UUID recordId;
    private UUID instrumentId;
    private UUID branchId;
    private QualityControlResponse sampleResponse;
    private QualityControlCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new QualityControlResponse(
                recordId, instrumentId, Instant.now(), QcLevelEnum.NORMAL,
                "Hemoglobin", "14.0", "14.2", true, null, "tech1",
                QcStatusEnum.COMPLETED, branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new QualityControlCreateRequest(
                instrumentId, QcLevelEnum.NORMAL, "Hemoglobin",
                "14.0", "14.2", true, null, "tech1", branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/qc")
    class CreateRecord {

        @Test
        @DisplayName("should create QC record as LAB_TECHNICIAN — 201")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldCreateRecordAsLabTechnician() throws Exception {
            when(qualityControlService.createRecord(any(QualityControlCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.testName").value("Hemoglobin"));
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
    @DisplayName("GET /api/v1/qc/{id}")
    class GetRecordById {

        @Test
        @DisplayName("should return record as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnRecordAsLabTechnician() throws Exception {
            when(qualityControlService.getRecordById(recordId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", recordId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(recordId.toString()));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/qc/instrument/{instrumentId}")
    class GetRecordsByInstrument {

        @Test
        @DisplayName("should return records by instrument as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnRecordsByInstrumentAsBranchManager() throws Exception {
            when(qualityControlService.getRecordsByInstrument(instrumentId))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/instrument/{instrumentId}", instrumentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/instrument/{instrumentId}", instrumentId))
                    .andExpect(status().isForbidden());
        }
    }
}
