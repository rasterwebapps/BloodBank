package com.bloodbank.labservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.model.enums.TestResultEnum;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.labservice.dto.TestResultApprovalRequest;
import com.bloodbank.labservice.dto.TestResultCreateRequest;
import com.bloodbank.labservice.dto.TestResultResponse;
import com.bloodbank.labservice.service.TestResultService;
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

@WebMvcTest(value = TestResultController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class TestResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestResultService testResultService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/test-results";
    private UUID resultId;
    private UUID testOrderId;
    private UUID branchId;
    private UUID instrumentId;
    private TestResultResponse sampleResponse;
    private TestResultCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        resultId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();

        sampleResponse = new TestResultResponse(
                resultId, testOrderId, "HIV", "ELISA", "0.1",
                TestResultEnum.NON_REACTIVE, false, "S/CO", "0.0-0.9",
                instrumentId, "tech1", null, Instant.now(), null,
                "notes", branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new TestResultCreateRequest(
                testOrderId, "HIV", "ELISA", "0.1",
                TestResultEnum.NON_REACTIVE, false, "S/CO", "0.0-0.9",
                instrumentId, "tech1", "notes", branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/test-results")
    class CreateResult {

        @Test
        @DisplayName("should create result as LAB_TECHNICIAN — 201")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldCreateResultAsLabTechnician() throws Exception {
            when(testResultService.createResult(any(TestResultCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.testName").value("HIV"));
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
    }

    @Nested
    @DisplayName("POST /api/v1/test-results/{id}/approve")
    class ApproveResult {

        @Test
        @DisplayName("should approve result as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldApproveResultAsLabTechnician() throws Exception {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("reviewer1");
            when(testResultService.approveResult(eq(resultId), any(TestResultApprovalRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL + "/{id}/approve", resultId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(approvalRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject approval as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectApprovalAsDonor() throws Exception {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("reviewer1");

            mockMvc.perform(post(BASE_URL + "/{id}/approve", resultId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(approvalRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/test-results/order/{testOrderId}")
    class GetResultsByOrder {

        @Test
        @DisplayName("should return results by order as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnResultsByOrderAsBranchManager() throws Exception {
            when(testResultService.getResultsByOrderId(testOrderId))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/order/{testOrderId}", testOrderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("should return results by order as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnResultsByOrderAsLabTechnician() throws Exception {
            when(testResultService.getResultsByOrderId(testOrderId))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/order/{testOrderId}", testOrderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/test-results/{id}")
    class GetResultById {

        @Test
        @DisplayName("should return result as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnResultAsLabTechnician() throws Exception {
            when(testResultService.getResultById(resultId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", resultId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(resultId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", resultId))
                    .andExpect(status().isForbidden());
        }
    }
}
