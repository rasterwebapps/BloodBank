package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.enums.LabelTypeEnum;
import com.bloodbank.inventoryservice.enums.ProcessResultEnum;
import com.bloodbank.inventoryservice.enums.ProcessTypeEnum;
import com.bloodbank.inventoryservice.service.BloodComponentService;
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

@WebMvcTest(value = ComponentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class ComponentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private BloodComponentService bloodComponentService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/components";
    private UUID componentId;
    private UUID branchId;
    private BloodComponentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        componentId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        sampleResponse = new BloodComponentResponse(
                componentId, UUID.randomUUID(), UUID.randomUUID(), "BC-ABCD1234",
                UUID.randomUUID(), 200, BigDecimal.valueOf(220.5), Instant.now(),
                Instant.now().plus(35, ChronoUnit.DAYS), ComponentStatusEnum.AVAILABLE,
                null, false, false, branchId, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/v1/components")
    class Create {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should create — 201")
        void shouldCreate() throws Exception {
            when(bloodComponentService.createComponent(any())).thenReturn(sampleResponse);
            BloodComponentCreateRequest req = new BloodComponentCreateRequest(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    200, Instant.now().plus(35, ChronoUnit.DAYS), null, branchId);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject DONOR — 403")
        void shouldReject() throws Exception {
            BloodComponentCreateRequest req = new BloodComponentCreateRequest(
                    UUID.randomUUID(), UUID.randomUUID(), null, 200,
                    Instant.now().plus(35, ChronoUnit.DAYS), null, branchId);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/components/{id}")
    class GetById {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            when(bloodComponentService.getById(componentId)).thenReturn(sampleResponse);
            mockMvc.perform(get(BASE_URL + "/{id}", componentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(componentId.toString()));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/components/blood-unit/{bloodUnitId}")
    class GetByBloodUnit {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            UUID bloodUnitId = UUID.randomUUID();
            when(bloodComponentService.getByBloodUnit(bloodUnitId)).thenReturn(List.of(sampleResponse));
            mockMvc.perform(get(BASE_URL + "/blood-unit/{bloodUnitId}", bloodUnitId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/components/{id}/process")
    class ProcessComponent {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should process — 201")
        void shouldProcess() throws Exception {
            ComponentProcessingCreateRequest req = new ComponentProcessingCreateRequest(
                    componentId, ProcessTypeEnum.SEPARATION, "tech1", "Centrifuge", null, branchId);
            ComponentProcessingResponse resp = new ComponentProcessingResponse(
                    UUID.randomUUID(), componentId, ProcessTypeEnum.SEPARATION, Instant.now(),
                    "tech1", "Centrifuge", null, ProcessResultEnum.SUCCESS, null,
                    branchId, LocalDateTime.now());
            when(bloodComponentService.processComponent(eq(componentId), any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/{id}/process", componentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/components/{id}/label")
    class CreateLabel {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should create label — 201")
        void shouldCreate() throws Exception {
            ComponentLabelCreateRequest req = new ComponentLabelCreateRequest(
                    componentId, LabelTypeEnum.ISBT128, "label-data", "tech1", branchId);
            ComponentLabelResponse resp = new ComponentLabelResponse(
                    UUID.randomUUID(), componentId, LabelTypeEnum.ISBT128, "label-data",
                    Instant.now(), "tech1", 0, branchId, LocalDateTime.now());
            when(bloodComponentService.createLabel(eq(componentId), any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/{id}/label", componentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/components/pooled")
    class CreatePooled {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should create pooled — 201")
        void shouldCreate() throws Exception {
            PooledComponentCreateRequest req = new PooledComponentCreateRequest(
                    UUID.randomUUID(), UUID.randomUUID(), 800, 4,
                    Instant.now().plus(5, ChronoUnit.DAYS), null, "tech1", null, branchId);
            PooledComponentResponse resp = new PooledComponentResponse(
                    UUID.randomUUID(), "PL-ABCD", UUID.randomUUID(), UUID.randomUUID(),
                    800, 4, Instant.now(), Instant.now().plus(5, ChronoUnit.DAYS),
                    ComponentStatusEnum.AVAILABLE, null, "tech1", null, branchId, LocalDateTime.now());
            when(bloodComponentService.createPooledComponent(any())).thenReturn(resp);
            mockMvc.perform(post(BASE_URL + "/pooled")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }
}
