package com.bloodbank.billingservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.billingservice.dto.PaymentCreateRequest;
import com.bloodbank.billingservice.dto.PaymentResponse;
import com.bloodbank.billingservice.enums.PaymentMethodEnum;
import com.bloodbank.billingservice.enums.PaymentStatusEnum;
import com.bloodbank.billingservice.service.PaymentService;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/payments";
    private UUID paymentId;
    private UUID invoiceId;
    private UUID branchId;
    private PaymentResponse sampleResponse;
    private PaymentCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new PaymentResponse(
                paymentId, invoiceId, "PAY-ABCD1234", Instant.now(),
                new BigDecimal("100.00"), "USD", PaymentMethodEnum.CASH,
                null, PaymentStatusEnum.COMPLETED, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new PaymentCreateRequest(
                invoiceId, new BigDecimal("100.00"), "USD",
                PaymentMethodEnum.CASH, null, null, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/payments")
    class RecordPayment {

        @Test
        @DisplayName("should record payment as BILLING_CLERK — 201")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldRecordPaymentAsBillingClerk() throws Exception {
            when(paymentService.recordPayment(any(PaymentCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.paymentNumber").value("PAY-ABCD1234"));
        }

        @Test
        @DisplayName("should record payment as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldRecordPaymentAsBranchManager() throws Exception {
            when(paymentService.recordPayment(any(PaymentCreateRequest.class))).thenReturn(sampleResponse);

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
    @DisplayName("GET /api/v1/payments/{id}")
    class GetPaymentById {

        @Test
        @DisplayName("should return payment as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldReturnPaymentAsBillingClerk() throws Exception {
            when(paymentService.getPaymentById(paymentId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(paymentId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", paymentId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/invoice/{invoiceId}")
    class GetPaymentsByInvoice {

        @Test
        @DisplayName("should return payments for invoice as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldReturnPaymentsForInvoice() throws Exception {
            when(paymentService.getPaymentsByInvoice(invoiceId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/invoice/{invoiceId}", invoiceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
