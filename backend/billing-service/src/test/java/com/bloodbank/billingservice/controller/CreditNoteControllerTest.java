package com.bloodbank.billingservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.billingservice.dto.CreditNoteCreateRequest;
import com.bloodbank.billingservice.dto.CreditNoteResponse;
import com.bloodbank.billingservice.enums.CreditNoteStatusEnum;
import com.bloodbank.billingservice.service.CreditNoteService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CreditNoteController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class CreditNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreditNoteService creditNoteService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/credit-notes";
    private UUID creditNoteId;
    private UUID invoiceId;
    private UUID branchId;
    private CreditNoteResponse sampleResponse;
    private CreditNoteCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        creditNoteId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new CreditNoteResponse(
                creditNoteId, invoiceId, "CN-ABCD1234", Instant.now(),
                new BigDecimal("50.00"), "Overcharge", CreditNoteStatusEnum.ISSUED,
                null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new CreditNoteCreateRequest(
                invoiceId, new BigDecimal("50.00"), "Overcharge", null, branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/credit-notes")
    class CreateCreditNote {

        @Test
        @DisplayName("should create credit note as BILLING_CLERK — 201")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldCreateCreditNoteAsBillingClerk() throws Exception {
            when(creditNoteService.createCreditNote(any(CreditNoteCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.creditNoteNumber").value("CN-ABCD1234"));
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
    @DisplayName("GET /api/v1/credit-notes/{id}")
    class GetCreditNoteById {

        @Test
        @DisplayName("should return credit note as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldReturnCreditNoteAsBillingClerk() throws Exception {
            when(creditNoteService.getCreditNoteById(creditNoteId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", creditNoteId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(creditNoteId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", creditNoteId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/credit-notes/invoice/{invoiceId}")
    class GetCreditNotesByInvoice {

        @Test
        @DisplayName("should return credit notes as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnCreditNotesAsBranchManager() throws Exception {
            when(creditNoteService.getCreditNotesByInvoice(invoiceId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/invoice/{invoiceId}", invoiceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/credit-notes/{id}/apply")
    class ApplyCreditNote {

        @Test
        @DisplayName("should apply credit note as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldApplyCreditNoteAsBillingClerk() throws Exception {
            UUID targetInvoiceId = UUID.randomUUID();
            when(creditNoteService.applyCreditNote(eq(creditNoteId), eq(targetInvoiceId)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/apply", creditNoteId)
                            .param("targetInvoiceId", targetInvoiceId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/apply", creditNoteId)
                            .param("targetInvoiceId", UUID.randomUUID().toString()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/credit-notes/{id}/void")
    class VoidCreditNote {

        @Test
        @DisplayName("should void credit note as BILLING_CLERK — 200")
        @WithMockUser(roles = "BILLING_CLERK")
        void shouldVoidCreditNoteAsBillingClerk() throws Exception {
            when(creditNoteService.voidCreditNote(creditNoteId)).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/void", creditNoteId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/void", creditNoteId))
                    .andExpect(status().isForbidden());
        }
    }
}
