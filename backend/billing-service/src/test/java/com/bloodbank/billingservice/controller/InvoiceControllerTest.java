package com.bloodbank.billingservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.billingservice.dto.InvoiceCreateRequest;
import com.bloodbank.billingservice.dto.InvoiceResponse;
import com.bloodbank.billingservice.enums.InvoiceStatusEnum;
import com.bloodbank.billingservice.service.InvoiceService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = InvoiceController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/invoices";
    private UUID invoiceId;
    private UUID hospitalId;
    private UUID branchId;
    private InvoiceResponse sampleResponse;
    private InvoiceCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new InvoiceResponse(
                invoiceId, hospitalId, "INV-ABCD1234", Instant.now(),
                LocalDate.now().plusDays(30), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "USD", InvoiceStatusEnum.DRAFT, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new InvoiceCreateRequest(
                hospitalId, LocalDate.now().plusDays(30), "USD", null, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/invoices")
    class CreateInvoice {

        @Test
        @DisplayName("should create invoice as BILLING_CLERK — 201")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldCreateInvoiceAsBillingClerk() throws Exception {
            when(invoiceService.createInvoice(any(InvoiceCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.invoiceNumber").value("INV-ABCD1234"));
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
    @DisplayName("GET /api/v1/invoices/{id}")
    class GetInvoiceById {

        @Test
        @DisplayName("should return invoice as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldReturnInvoiceAsBillingClerk() throws Exception {
            when(invoiceService.getInvoiceById(invoiceId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", invoiceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(invoiceId.toString()));
        }

        @Test
        @DisplayName("should reject as unauthorized role — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsUnauthorizedRole() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", invoiceId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/invoices/status/{status}")
    class GetInvoicesByStatus {

        @Test
        @DisplayName("should return invoices by status as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnInvoicesByStatusAsBranchManager() throws Exception {
            when(invoiceService.getInvoicesByStatus(InvoiceStatusEnum.DRAFT))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/status/{status}", "DRAFT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/invoices/{id}/status")
    class UpdateInvoiceStatus {

        @Test
        @DisplayName("should update invoice status as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldUpdateInvoiceStatusAsBillingClerk() throws Exception {
            when(invoiceService.updateInvoiceStatus(eq(invoiceId), eq(InvoiceStatusEnum.ISSUED)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", invoiceId)
                            .param("status", "ISSUED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/status", invoiceId)
                            .param("status", "ISSUED"))
                    .andExpect(status().isForbidden());
        }
    }
}
