package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.enums.*;
import com.bloodbank.inventoryservice.service.LogisticsService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = LogisticsController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class LogisticsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private LogisticsService logisticsService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/logistics";
    private UUID branchId;

    @BeforeEach
    void setUp() {
        branchId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/v1/logistics/transport-requests")
    class CreateTransport {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should create — 201")
        void shouldCreate() throws Exception {
            TransportRequestCreateRequest req = new TransportRequestCreateRequest(
                    UUID.randomUUID(), UUID.randomUUID(), null, null,
                    TransportTypeEnum.ROUTINE, 5, null, "John", "555-1234",
                    "VEH-001", null, branchId);
            TransportRequestResponse resp = new TransportRequestResponse(
                    UUID.randomUUID(), "TRQ-12345678", UUID.randomUUID(), UUID.randomUUID(),
                    null, null, TransportTypeEnum.ROUTINE, 5, Instant.now(),
                    null, null, "John", "555-1234", "VEH-001",
                    TransportStatusEnum.REQUESTED, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(logisticsService.createTransportRequest(any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/transport-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject DONOR — 403")
        void shouldReject() throws Exception {
            TransportRequestCreateRequest req = new TransportRequestCreateRequest(
                    UUID.randomUUID(), null, null, null,
                    TransportTypeEnum.ROUTINE, 5, null, null, null, null, null, branchId);
            mockMvc.perform(post(BASE_URL + "/transport-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/logistics/transport-requests/{id}")
    class GetTransport {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            UUID id = UUID.randomUUID();
            TransportRequestResponse resp = new TransportRequestResponse(
                    id, "TRQ-TEST", UUID.randomUUID(), null, null, null,
                    TransportTypeEnum.ROUTINE, 5, null, null, null,
                    null, null, null, TransportStatusEnum.REQUESTED, null,
                    branchId, LocalDateTime.now(), LocalDateTime.now());
            when(logisticsService.getTransportRequest(id)).thenReturn(resp);
            mockMvc.perform(get(BASE_URL + "/transport-requests/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(id.toString()));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/logistics/transport-requests")
    class ListTransports {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should list — 200")
        void shouldList() throws Exception {
            when(logisticsService.getTransportRequests()).thenReturn(List.of());
            mockMvc.perform(get(BASE_URL + "/transport-requests"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/logistics/transport-requests/{id}/status")
    class UpdateStatus {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should update — 200")
        void shouldUpdate() throws Exception {
            UUID id = UUID.randomUUID();
            TransportRequestResponse resp = new TransportRequestResponse(
                    id, "TRQ-TEST", UUID.randomUUID(), null, null, null,
                    TransportTypeEnum.ROUTINE, 5, null, null, null,
                    null, null, null, TransportStatusEnum.DISPATCHED, null,
                    branchId, LocalDateTime.now(), LocalDateTime.now());
            when(logisticsService.updateTransportStatus(eq(id), eq(TransportStatusEnum.DISPATCHED)))
                    .thenReturn(resp);
            mockMvc.perform(patch(BASE_URL + "/transport-requests/{id}/status", id)
                            .param("status", "DISPATCHED"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/logistics/cold-chain-logs")
    class LogColdChain {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should log — 201")
        void shouldLog() throws Exception {
            ColdChainLogCreateRequest req = new ColdChainLogCreateRequest(
                    UUID.randomUUID(), null, null,
                    BigDecimal.valueOf(4.5), BigDecimal.valueOf(60), "tech1", null, branchId);
            ColdChainLogResponse resp = new ColdChainLogResponse(
                    UUID.randomUUID(), UUID.randomUUID(), null, null,
                    BigDecimal.valueOf(4.5), BigDecimal.valueOf(60), Instant.now(),
                    true, false, "tech1", null, branchId, LocalDateTime.now());
            when(logisticsService.logColdChain(any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/cold-chain-logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/logistics/cold-chain-logs/transport/{transportId}")
    class GetColdChainLogs {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should get logs — 200")
        void shouldGet() throws Exception {
            UUID transportId = UUID.randomUUID();
            when(logisticsService.getColdChainLogs(transportId)).thenReturn(List.of());
            mockMvc.perform(get(BASE_URL + "/cold-chain-logs/transport/{transportId}", transportId))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/logistics/transport-boxes")
    class CreateBox {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should create — 201")
        void shouldCreate() throws Exception {
            TransportBoxCreateRequest req = new TransportBoxCreateRequest(
                    "BOX-001", TransportBoxTypeEnum.INSULATED, 10, "2-6°C", branchId);
            TransportBoxResponse resp = new TransportBoxResponse(
                    UUID.randomUUID(), "BOX-001", TransportBoxTypeEnum.INSULATED, 10,
                    "2-6°C", TransportBoxStatusEnum.AVAILABLE, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(logisticsService.createTransportBox(any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/transport-boxes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/logistics/deliveries")
    class ConfirmDelivery {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should confirm — 201")
        void shouldConfirm() throws Exception {
            UUID transportId = UUID.randomUUID();
            DeliveryConfirmationCreateRequest req = new DeliveryConfirmationCreateRequest(
                    transportId, "Receiver", DeliveryConditionEnum.GOOD,
                    BigDecimal.valueOf(4.0), 5, 0, null, "SIG-001", null, branchId);
            DeliveryConfirmationResponse resp = new DeliveryConfirmationResponse(
                    UUID.randomUUID(), transportId, "Receiver", Instant.now(),
                    DeliveryConditionEnum.GOOD, BigDecimal.valueOf(4.0), 5, 0,
                    null, "SIG-001", null, branchId, LocalDateTime.now());
            when(logisticsService.confirmDelivery(any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/deliveries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }
}
