package com.bloodbank.notificationservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.notificationservice.dto.NotificationTemplateCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationTemplateResponse;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.service.TemplateService;
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

@WebMvcTest(value = TemplateController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TemplateService templateService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/notification-templates";
    private UUID templateId;
    private NotificationTemplateResponse sampleResponse;
    private NotificationTemplateCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        templateId = UUID.randomUUID();

        sampleResponse = new NotificationTemplateResponse(
                templateId, "DONATION_THANK_YOU", "Donation Thank You",
                ChannelEnum.EMAIL, "Thank you for your donation", "Dear {{name}}, thank you!",
                "name", "en", true, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new NotificationTemplateCreateRequest(
                "DONATION_THANK_YOU", "Donation Thank You",
                ChannelEnum.EMAIL, "Thank you for your donation",
                "Dear {{name}}, thank you!", "name", "en"
        );
    }

    @Nested
    @DisplayName("POST /api/v1/notification-templates")
    class CreateTemplate {

        @Test
        @DisplayName("should create template as BRANCH_ADMIN — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreateTemplateAsBranchAdmin() throws Exception {
            when(templateService.create(any(NotificationTemplateCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.templateCode").value("DONATION_THANK_YOU"));
        }

        @Test
        @DisplayName("should create template as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreateTemplateAsBranchManager() throws Exception {
            when(templateService.create(any(NotificationTemplateCreateRequest.class))).thenReturn(sampleResponse);

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
    @DisplayName("GET /api/v1/notification-templates/{id}")
    class GetById {

        @Test
        @DisplayName("should return template as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldReturnTemplateAsBranchAdmin() throws Exception {
            when(templateService.getById(templateId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", templateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(templateId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", templateId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notification-templates")
    class GetAll {

        @Test
        @DisplayName("should return all templates as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnAllTemplatesAsBranchManager() throws Exception {
            when(templateService.getAll()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notification-templates/active")
    class GetActiveTemplates {

        @Test
        @DisplayName("should return active templates as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldReturnActiveTemplatesAsBranchAdmin() throws Exception {
            when(templateService.getActiveTemplates()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/notification-templates/{id}/deactivate")
    class Deactivate {

        @Test
        @DisplayName("should deactivate template as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldDeactivateTemplateAsBranchAdmin() throws Exception {
            when(templateService.deactivate(templateId)).thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", templateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
