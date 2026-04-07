package com.bloodbank.notificationservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.notificationservice.dto.CampaignCreateRequest;
import com.bloodbank.notificationservice.dto.CampaignResponse;
import com.bloodbank.notificationservice.enums.CampaignStatusEnum;
import com.bloodbank.notificationservice.enums.CampaignTypeEnum;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.service.CampaignService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CampaignController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CampaignService campaignService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/campaigns";
    private UUID campaignId;
    private CampaignResponse sampleResponse;
    private CampaignCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        campaignId = UUID.randomUUID();

        sampleResponse = new CampaignResponse(
                campaignId, null, "SUMMER_DRIVE_2024", "Summer Blood Drive 2024",
                CampaignTypeEnum.DONATION_DRIVE, ChannelEnum.EMAIL, null, null,
                null, null, null, null, 0, 0, 0, 0,
                CampaignStatusEnum.DRAFT, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new CampaignCreateRequest(
                null, "SUMMER_DRIVE_2024", "Summer Blood Drive 2024",
                CampaignTypeEnum.DONATION_DRIVE, ChannelEnum.EMAIL,
                null, null, null, null
        );
    }

    @Nested
    @DisplayName("POST /api/v1/campaigns")
    class CreateCampaign {

        @Test
        @DisplayName("should create campaign as BRANCH_ADMIN — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreateCampaignAsBranchAdmin() throws Exception {
            when(campaignService.create(any(CampaignCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.campaignCode").value("SUMMER_DRIVE_2024"));
        }

        @Test
        @DisplayName("should create campaign as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreateCampaignAsBranchManager() throws Exception {
            when(campaignService.create(any(CampaignCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
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
    @DisplayName("GET /api/v1/campaigns/{id}")
    class GetById {

        @Test
        @DisplayName("should return campaign as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldReturnCampaignAsBranchAdmin() throws Exception {
            when(campaignService.getById(campaignId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", campaignId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(campaignId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", campaignId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/campaigns?status=DRAFT")
    class GetByStatus {

        @Test
        @DisplayName("should return campaigns by status as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnCampaignsByStatus() throws Exception {
            when(campaignService.getByStatus(CampaignStatusEnum.DRAFT)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL).param("status", "DRAFT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/campaigns/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("should update campaign status as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldUpdateCampaignStatusAsBranchAdmin() throws Exception {
            when(campaignService.updateStatus(eq(campaignId), eq(CampaignStatusEnum.IN_PROGRESS)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/status", campaignId)
                            .param("status", "IN_PROGRESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
