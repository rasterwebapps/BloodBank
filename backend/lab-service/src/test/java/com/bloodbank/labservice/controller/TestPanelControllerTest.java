package com.bloodbank.labservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.labservice.dto.TestPanelCreateRequest;
import com.bloodbank.labservice.dto.TestPanelResponse;
import com.bloodbank.labservice.service.TestPanelService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TestPanelController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class TestPanelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestPanelService testPanelService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/test-panels";
    private UUID panelId;
    private TestPanelResponse sampleResponse;
    private TestPanelCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        panelId = UUID.randomUUID();

        sampleResponse = new TestPanelResponse(
                panelId, "TTI", "TTI Panel", "Transmissible infection tests",
                "HIV,HBV,HCV,Syphilis", true, true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new TestPanelCreateRequest(
                "TTI", "TTI Panel", "Transmissible infection tests",
                "HIV,HBV,HCV,Syphilis", true
        );
    }

    @Nested
    @DisplayName("POST /api/v1/test-panels")
    class CreatePanel {

        @Test
        @DisplayName("should create panel as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreatePanelAsBranchManager() throws Exception {
            when(testPanelService.createPanel(any(TestPanelCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.panelCode").value("TTI"));
        }

        @Test
        @DisplayName("should create panel as BRANCH_ADMIN — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreatePanelAsBranchAdmin() throws Exception {
            when(testPanelService.createPanel(any(TestPanelCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should reject as LAB_TECHNICIAN — 403")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldRejectAsLabTechnician() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/test-panels")
    class GetActivePanels {

        @Test
        @DisplayName("should return active panels as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnActivePanelsAsLabTechnician() throws Exception {
            when(testPanelService.getActivePanels()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("should return active panels as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnActivePanelsAsBranchManager() throws Exception {
            when(testPanelService.getActivePanels()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/test-panels/mandatory")
    class GetMandatoryPanels {

        @Test
        @DisplayName("should return mandatory panels as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnMandatoryPanelsAsLabTechnician() throws Exception {
            when(testPanelService.getMandatoryPanels()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/mandatory"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/test-panels/{id}")
    class GetPanelById {

        @Test
        @DisplayName("should return panel as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnPanelAsLabTechnician() throws Exception {
            when(testPanelService.getPanelById(panelId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", panelId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(panelId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", panelId))
                    .andExpect(status().isForbidden());
        }
    }
}
