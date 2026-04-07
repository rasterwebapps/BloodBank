package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.reportingservice.dto.ChainOfCustodyCreateRequest;
import com.bloodbank.reportingservice.dto.ChainOfCustodyResponse;
import com.bloodbank.reportingservice.enums.CustodyEventEnum;
import com.bloodbank.reportingservice.service.ChainOfCustodyService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ChainOfCustodyController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class ChainOfCustodyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ChainOfCustodyService chainOfCustodyService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/chain-of-custody";
    private UUID entityId;
    private UUID branchId;
    private ChainOfCustodyResponse sampleResponse;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        sampleResponse = new ChainOfCustodyResponse(
                UUID.randomUUID(), branchId, "BloodUnit", entityId,
                CustodyEventEnum.COLLECTED, "Room A", "Lab",
                "handler-1", new BigDecimal("4.50"), Instant.now(),
                null, LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/v1/chain-of-custody")
    class AddEvent {
        @Test
        @WithMockUser(roles = "AUDITOR")
        @DisplayName("should create — 201")
        void shouldCreate() throws Exception {
            ChainOfCustodyCreateRequest request = new ChainOfCustodyCreateRequest(
                    branchId, "BloodUnit", entityId, CustodyEventEnum.COLLECTED,
                    "Room A", "Lab", "handler-1", new BigDecimal("4.50"), null);
            when(chainOfCustodyService.addEvent(any())).thenReturn(sampleResponse);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject as DONOR — 403")
        void shouldReject() throws Exception {
            ChainOfCustodyCreateRequest request = new ChainOfCustodyCreateRequest(
                    branchId, "BloodUnit", entityId, CustodyEventEnum.COLLECTED,
                    "Room A", "Lab", "handler-1", new BigDecimal("4.50"), null);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/chain-of-custody/entity/{entityType}/{entityId}")
    class GetByEntity {
        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            when(chainOfCustodyService.getByEntityId("BloodUnit", entityId))
                    .thenReturn(List.of(sampleResponse));
            mockMvc.perform(get(BASE_URL + "/entity/BloodUnit/{entityId}", entityId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/chain-of-custody/full-chain/{entityId}")
    class GetFullChain {
        @Test
        @WithMockUser(roles = "AUDITOR")
        @DisplayName("should return full chain — 200")
        void shouldReturn() throws Exception {
            when(chainOfCustodyService.getFullChain(entityId)).thenReturn(List.of(sampleResponse));
            mockMvc.perform(get(BASE_URL + "/full-chain/{entityId}", entityId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
