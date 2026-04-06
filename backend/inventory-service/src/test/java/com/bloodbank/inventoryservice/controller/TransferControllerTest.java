package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.inventoryservice.dto.StockTransferCreateRequest;
import com.bloodbank.inventoryservice.dto.StockTransferResponse;
import com.bloodbank.inventoryservice.enums.TransferStatusEnum;
import com.bloodbank.inventoryservice.service.StockTransferService;
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

@WebMvcTest(value = TransferController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class TransferControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private StockTransferService stockTransferService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/transfers";
    private UUID transferId;
    private UUID branchId;
    private StockTransferResponse sampleResponse;

    @BeforeEach
    void setUp() {
        transferId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        sampleResponse = new StockTransferResponse(
                transferId, "TR-ABCD1234", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), null, Instant.now(), null, null,
                TransferStatusEnum.REQUESTED, "tech1", null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/v1/transfers")
    class Create {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should create — 201")
        void shouldCreate() throws Exception {
            when(stockTransferService.createTransfer(any())).thenReturn(sampleResponse);
            StockTransferCreateRequest req = new StockTransferCreateRequest(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, "tech1", null, branchId);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject DONOR — 403")
        void shouldReject() throws Exception {
            StockTransferCreateRequest req = new StockTransferCreateRequest(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, "tech1", null, branchId);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/transfers/{id}")
    class GetById {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            when(stockTransferService.getById(transferId)).thenReturn(sampleResponse);
            mockMvc.perform(get(BASE_URL + "/{id}", transferId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(transferId.toString()));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/transfers/{id}/approve")
    class Approve {
        @Test
        @WithMockUser(roles = "BRANCH_MANAGER")
        @DisplayName("should approve — 200")
        void shouldApprove() throws Exception {
            when(stockTransferService.approveTransfer(eq(transferId), eq("mgr1"))).thenReturn(sampleResponse);
            mockMvc.perform(patch(BASE_URL + "/{id}/approve", transferId).param("approvedBy", "mgr1"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject DONOR — 403")
        void shouldReject() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/approve", transferId).param("approvedBy", "mgr1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/transfers/{id}/ship")
    class Ship {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should ship — 200")
        void shouldShip() throws Exception {
            when(stockTransferService.shipTransfer(transferId)).thenReturn(sampleResponse);
            mockMvc.perform(patch(BASE_URL + "/{id}/ship", transferId)).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/transfers/{id}/receive")
    class Receive {
        @Test
        @WithMockUser(roles = "BRANCH_MANAGER")
        @DisplayName("should receive — 200")
        void shouldReceive() throws Exception {
            when(stockTransferService.receiveTransfer(transferId)).thenReturn(sampleResponse);
            mockMvc.perform(patch(BASE_URL + "/{id}/receive", transferId)).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/transfers/{id}/cancel")
    class Cancel {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should cancel — 200")
        void shouldCancel() throws Exception {
            when(stockTransferService.cancelTransfer(transferId)).thenReturn(sampleResponse);
            mockMvc.perform(patch(BASE_URL + "/{id}/cancel", transferId)).andExpect(status().isOk());
        }
    }
}
