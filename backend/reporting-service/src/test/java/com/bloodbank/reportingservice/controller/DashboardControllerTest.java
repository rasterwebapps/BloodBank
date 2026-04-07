package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.reportingservice.dto.DashboardWidgetCreateRequest;
import com.bloodbank.reportingservice.dto.DashboardWidgetResponse;
import com.bloodbank.reportingservice.enums.WidgetTypeEnum;
import com.bloodbank.reportingservice.service.DashboardService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = DashboardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DashboardService dashboardService;
    @MockBean private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/dashboard-widgets";
    private UUID widgetId;
    private DashboardWidgetResponse sampleResponse;

    @BeforeEach
    void setUp() {
        widgetId = UUID.randomUUID();
        sampleResponse = new DashboardWidgetResponse(
                widgetId, null, "WDG-STOCK", "Stock Overview",
                WidgetTypeEnum.CHART, "inventory-service", null, null,
                300, null, 1, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/v1/dashboard-widgets")
    class Create {
        @Test
        @WithMockUser(roles = "AUDITOR")
        @DisplayName("should create — 201")
        void shouldCreate() throws Exception {
            DashboardWidgetCreateRequest request = new DashboardWidgetCreateRequest(
                    null, "WDG-STOCK", "Stock Overview", WidgetTypeEnum.CHART,
                    "inventory-service", null, null, 300, null, 1);
            when(dashboardService.create(any())).thenReturn(sampleResponse);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DONOR")
        @DisplayName("should reject — 403")
        void shouldReject() throws Exception {
            DashboardWidgetCreateRequest request = new DashboardWidgetCreateRequest(
                    null, "WDG-STOCK", "Stock Overview", WidgetTypeEnum.CHART,
                    "inventory-service", null, null, 300, null, 1);
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/dashboard-widgets/{id}")
    class GetById {
        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("should return — 200")
        void shouldReturn() throws Exception {
            when(dashboardService.getById(widgetId)).thenReturn(sampleResponse);
            mockMvc.perform(get(BASE_URL + "/{id}", widgetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.widgetCode").value("WDG-STOCK"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/dashboard-widgets/active")
    class GetActive {
        @Test
        @WithMockUser(roles = "REGIONAL_ADMIN")
        @DisplayName("should return active — 200")
        void shouldReturn() throws Exception {
            when(dashboardService.getActiveWidgets()).thenReturn(List.of(sampleResponse));
            mockMvc.perform(get(BASE_URL + "/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/dashboard-widgets/{id}")
    class Update {
        @Test
        @WithMockUser(roles = "AUDITOR")
        @DisplayName("should update — 200")
        void shouldUpdate() throws Exception {
            DashboardWidgetCreateRequest request = new DashboardWidgetCreateRequest(
                    null, "WDG-STOCK", "Updated", WidgetTypeEnum.TABLE,
                    "inventory-service", null, null, 600, null, 2);
            when(dashboardService.update(eq(widgetId), any())).thenReturn(sampleResponse);
            mockMvc.perform(put(BASE_URL + "/{id}", widgetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/dashboard-widgets/{id}")
    class Delete {
        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("should delete — 200")
        void shouldDelete() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", widgetId))
                    .andExpect(status().isOk());
        }
    }
}
