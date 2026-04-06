package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.inventoryservice.dto.BloodUnitCreateRequest;
import com.bloodbank.inventoryservice.dto.BloodUnitResponse;
import com.bloodbank.inventoryservice.dto.BloodUnitStatusUpdateRequest;
import com.bloodbank.inventoryservice.enums.TtiStatusEnum;
import com.bloodbank.inventoryservice.service.BloodUnitService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = BloodUnitController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class BloodUnitControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private BloodUnitService bloodUnitService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/blood-units";
    private UUID unitId;
    private UUID branchId;
    private BloodUnitResponse sampleResponse;
    private BloodUnitCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        unitId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        UUID donorId = UUID.randomUUID();
        UUID bloodGroupId = UUID.randomUUID();

        sampleResponse = new BloodUnitResponse(
                unitId, collectionId, donorId, "BU-ABCD1234", bloodGroupId,
                "POSITIVE", 450, Instant.now(), Instant.now().plus(42, ChronoUnit.DAYS),
                BloodUnitStatusEnum.QUARANTINED, TtiStatusEnum.PENDING, null,
                branchId, LocalDateTime.now(), LocalDateTime.now());

        createRequest = new BloodUnitCreateRequest(
                collectionId, donorId, bloodGroupId, "POSITIVE", 450,
                Instant.now(), Instant.now().plus(42, ChronoUnit.DAYS), null, branchId);
    }

    @Nested
    @DisplayName("POST /api/v1/blood-units")
    class Create {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should create as INVENTORY_MANAGER — 201")
        void shouldCreate() throws Exception {
            when(bloodUnitService.createBloodUnit(any())).thenReturn(sampleResponse);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.unitNumber").value("BU-ABCD1234"));
        }

        @Test
        @WithMockUser(roles = "LAB_TECHNICIAN")
        @DisplayName("should create as LAB_TECHNICIAN — 201")
        void shouldCreateAsLabTech() throws Exception {
            when(bloodUnitService.createBloodUnit(any())).thenReturn(sampleResponse);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject as DONOR — 403")
        void shouldReject() throws Exception {
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/blood-units/{id}")
    class GetById {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            when(bloodUnitService.getById(unitId)).thenReturn(sampleResponse);
            mockMvc.perform(get(BASE_URL + "/{id}", unitId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(unitId.toString()));
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject as DONOR — 403")
        void shouldReject() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", unitId)).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/blood-units/status/{status}")
    class GetByStatus {
        @Test
        @WithMockUser(roles = "LAB_TECHNICIAN")
        @DisplayName("should return by status — 200")
        void shouldReturn() throws Exception {
            when(bloodUnitService.getByStatus(BloodUnitStatusEnum.AVAILABLE)).thenReturn(List.of(sampleResponse));
            mockMvc.perform(get(BASE_URL + "/status/{status}", "AVAILABLE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/blood-units/{id}/status")
    class UpdateStatus {
        @Test
        @WithMockUser(roles = "INVENTORY_MANAGER")
        @DisplayName("should update — 200")
        void shouldUpdate() throws Exception {
            when(bloodUnitService.updateStatus(eq(unitId), eq(BloodUnitStatusEnum.AVAILABLE)))
                    .thenReturn(sampleResponse);
            BloodUnitStatusUpdateRequest req = new BloodUnitStatusUpdateRequest(BloodUnitStatusEnum.AVAILABLE);
            mockMvc.perform(patch(BASE_URL + "/{id}/status", unitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject — 403")
        void shouldReject() throws Exception {
            BloodUnitStatusUpdateRequest req = new BloodUnitStatusUpdateRequest(BloodUnitStatusEnum.AVAILABLE);
            mockMvc.perform(patch(BASE_URL + "/{id}/status", unitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }
}
