package com.bloodbank.billingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.billingservice.dto.RateCreateRequest;
import com.bloodbank.billingservice.dto.RateResponse;
import com.bloodbank.billingservice.service.RateService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = RateController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class RateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RateService rateService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/rates";
    private UUID rateId;
    private UUID branchId;
    private RateResponse sampleResponse;
    private RateCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        rateId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new RateResponse(
                rateId, null, "BLD-001", "Whole Blood Unit",
                new BigDecimal("150.00"), "USD", new BigDecimal("5.00"),
                LocalDate.now(), null, true,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new RateCreateRequest(
                null, "BLD-001", "Whole Blood Unit",
                new BigDecimal("150.00"), "USD", new BigDecimal("5.00"),
                LocalDate.now(), null, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/rates")
    class CreateRate {

        @Test
        @DisplayName("should create rate as BILLING_CLERK — 201")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldCreateRateAsBillingClerk() throws Exception {
            when(rateService.createRate(any(RateCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.serviceCode").value("BLD-001"));
        }

        @Test
        @DisplayName("should create rate as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreateRateAsBranchManager() throws Exception {
            when(rateService.createRate(any(RateCreateRequest.class))).thenReturn(sampleResponse);

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
    }

    @Nested
    @DisplayName("GET /api/v1/rates/{id}")
    class GetRateById {

        @Test
        @DisplayName("should return rate as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldReturnRateAsBillingClerk() throws Exception {
            when(rateService.getRateById(rateId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", rateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(rateId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", rateId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/rates/branch/{branchId}")
    class GetActiveRatesByBranch {

        @Test
        @DisplayName("should return active rates as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldReturnActiveRatesAsBillingClerk() throws Exception {
            when(rateService.getActiveRatesByBranch(branchId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/branch/{branchId}", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/rates/{id}")
    class UpdateRate {

        @Test
        @DisplayName("should update rate as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldUpdateRateAsBillingClerk() throws Exception {
            when(rateService.updateRate(eq(rateId), any(RateCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}", rateId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/rates/{id}")
    class DeactivateRate {

        @Test
        @DisplayName("should deactivate rate as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldDeactivateRateAsBranchManager() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", rateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", rateId))
                    .andExpect(status().isForbidden());
        }
    }
}
