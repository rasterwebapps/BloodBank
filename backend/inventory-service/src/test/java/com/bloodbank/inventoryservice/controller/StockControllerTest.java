package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.enums.DisposalReasonEnum;
import com.bloodbank.inventoryservice.enums.ReservationStatusEnum;
import com.bloodbank.inventoryservice.service.StockService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = StockController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class StockControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private StockService stockService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/stock";
    private UUID branchId;

    @BeforeEach
    void setUp() {
        branchId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /api/v1/stock/levels")
    class GetStockLevels {
        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should allow any authenticated — 200")
        void shouldAllow() throws Exception {
            when(stockService.getStockLevels(any(), any(), any()))
                    .thenReturn(List.of(new StockLevelResponse(UUID.randomUUID(), null, branchId, 10)));
            mockMvc.perform(get(BASE_URL + "/levels"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/stock/dispatch")
    class Dispatch {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should dispatch — 200")
        void shouldDispatch() throws Exception {
            UUID componentTypeId = UUID.randomUUID();
            BloodComponentResponse resp = new BloodComponentResponse(
                    UUID.randomUUID(), UUID.randomUUID(), componentTypeId, "BC-1234",
                    UUID.randomUUID(), 200, BigDecimal.valueOf(220.5), Instant.now(),
                    Instant.now().plus(30, ChronoUnit.DAYS), ComponentStatusEnum.ISSUED,
                    null, false, false, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(stockService.dispatchComponent(any(), any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/dispatch")
                            .param("componentTypeId", componentTypeId.toString()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject DONOR — 403")
        void shouldReject() throws Exception {
            mockMvc.perform(post(BASE_URL + "/dispatch")
                            .param("componentTypeId", UUID.randomUUID().toString()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stock/expiring")
    class GetExpiring {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should return expiring — 200")
        void shouldReturn() throws Exception {
            when(stockService.getExpiringUnits(anyInt())).thenReturn(List.of());
            mockMvc.perform(get(BASE_URL + "/expiring").param("days", "7"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "BRANCH_MANAGER")
        @DisplayName("should allow BRANCH_MANAGER — 200")
        void shouldAllowBM() throws Exception {
            when(stockService.getExpiringUnits(anyInt())).thenReturn(List.of());
            mockMvc.perform(get(BASE_URL + "/expiring").param("days", "7"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/stock/dispose")
    class Dispose {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should dispose — 201")
        void shouldDispose() throws Exception {
            UnitDisposalCreateRequest req = new UnitDisposalCreateRequest(
                    UUID.randomUUID(), null, DisposalReasonEnum.EXPIRED,
                    "tech1", "mgr1", "Expired", branchId);
            UnitDisposalResponse resp = new UnitDisposalResponse(
                    UUID.randomUUID(), UUID.randomUUID(), null, DisposalReasonEnum.EXPIRED,
                    Instant.now(), "tech1", "mgr1", "Expired", branchId, LocalDateTime.now());
            when(stockService.disposeUnit(any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/dispose")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/stock/reserve")
    class Reserve {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should reserve — 201")
        void shouldReserve() throws Exception {
            UUID compId = UUID.randomUUID();
            UnitReservationCreateRequest req = new UnitReservationCreateRequest(
                    compId, "Patient", Instant.now().plus(1, ChronoUnit.HOURS), "tech1", null, branchId);
            UnitReservationResponse resp = new UnitReservationResponse(
                    UUID.randomUUID(), compId, "Patient", Instant.now(),
                    Instant.now().plus(1, ChronoUnit.HOURS), ReservationStatusEnum.ACTIVE,
                    "tech1", null, branchId, LocalDateTime.now());
            when(stockService.reserveComponent(any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/reserve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }
}
