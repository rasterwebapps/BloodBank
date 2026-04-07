package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.reportingservice.dto.ReportMetadataCreateRequest;
import com.bloodbank.reportingservice.dto.ReportMetadataResponse;
import com.bloodbank.reportingservice.dto.ReportScheduleCreateRequest;
import com.bloodbank.reportingservice.dto.ReportScheduleResponse;
import com.bloodbank.reportingservice.enums.OutputFormatEnum;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;
import com.bloodbank.reportingservice.service.ReportScheduleService;
import com.bloodbank.reportingservice.service.ReportService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ReportController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ReportService reportService;
    @MockBean private ReportScheduleService reportScheduleService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/reports";
    private UUID reportId;
    private ReportMetadataResponse sampleResponse;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        sampleResponse = new ReportMetadataResponse(
                reportId, null, "RPT-001", "Daily Report",
                ReportTypeEnum.OPERATIONAL, "Daily report", null, null,
                OutputFormatEnum.PDF, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/v1/reports")
    class Create {
        @Test
        @WithMockUser(roles = "AUDITOR")
        @DisplayName("should create — 201")
        void shouldCreate() throws Exception {
            ReportMetadataCreateRequest request = new ReportMetadataCreateRequest(
                    null, "RPT-001", "Daily Report", ReportTypeEnum.OPERATIONAL,
                    "Daily report", null, null, OutputFormatEnum.PDF);
            when(reportService.create(any())).thenReturn(sampleResponse);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject — 403")
        void shouldReject() throws Exception {
            ReportMetadataCreateRequest request = new ReportMetadataCreateRequest(
                    null, "RPT-001", "Daily Report", ReportTypeEnum.OPERATIONAL,
                    "Daily report", null, null, OutputFormatEnum.PDF);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/{id}")
    class GetById {
        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            when(reportService.getById(reportId)).thenReturn(sampleResponse);
            mockMvc.perform(get(BASE_URL + "/{id}", reportId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reportCode").value("RPT-001"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/active")
    class GetAllActive {
        @Test
        @WithMockUser(roles = "REGIONAL_ADMIN")
        @DisplayName("should return active — 200")
        void shouldReturn() throws Exception {
            when(reportService.getAllActive()).thenReturn(List.of(sampleResponse));
            mockMvc.perform(get(BASE_URL + "/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/reports/{id}")
    class Update {
        @Test
        @WithMockUser(roles = "AUDITOR")
        @DisplayName("should update — 200")
        void shouldUpdate() throws Exception {
            ReportMetadataCreateRequest request = new ReportMetadataCreateRequest(
                    null, "RPT-001", "Updated", ReportTypeEnum.OPERATIONAL,
                    "Updated", null, null, OutputFormatEnum.EXCEL);
            when(reportService.update(eq(reportId), any())).thenReturn(sampleResponse);
            mockMvc.perform(put(BASE_URL + "/{id}", reportId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/reports/{id}")
    class Delete {
        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("should delete — 200")
        void shouldDelete() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", reportId))
                    .andExpect(status().isOk());
        }
    }
}
