package com.bloodbank.notificationservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.notificationservice.dto.NotificationPreferenceCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationPreferenceResponse;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.service.PreferenceService;
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

@WebMvcTest(value = PreferenceController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class PreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PreferenceService preferenceService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/notification-preferences";
    private UUID preferenceId;
    private UUID userId;
    private NotificationPreferenceResponse sampleResponse;
    private NotificationPreferenceCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        preferenceId = UUID.randomUUID();
        userId = UUID.randomUUID();

        sampleResponse = new NotificationPreferenceResponse(
                preferenceId, userId, ChannelEnum.EMAIL, "DONATION_COMPLETED",
                true, null, null, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new NotificationPreferenceCreateRequest(
                userId, ChannelEnum.EMAIL, "DONATION_COMPLETED",
                true, null, null
        );
    }

    @Nested
    @DisplayName("POST /api/v1/notification-preferences")
    class CreatePreference {

        @Test
        @DisplayName("should create preference as BRANCH_ADMIN — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreatePreferenceAsBranchAdmin() throws Exception {
            when(preferenceService.create(any(NotificationPreferenceCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.channel").value("EMAIL"));
        }

        @Test
        @DisplayName("should create preference as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreatePreferenceAsBranchManager() throws Exception {
            when(preferenceService.create(any(NotificationPreferenceCreateRequest.class)))
                    .thenReturn(sampleResponse);

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
    @DisplayName("GET /api/v1/notification-preferences/{id}")
    class GetById {

        @Test
        @DisplayName("should return preference as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldReturnPreferenceAsBranchAdmin() throws Exception {
            when(preferenceService.getById(preferenceId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", preferenceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(preferenceId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", preferenceId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notification-preferences/user/{userId}")
    class GetByUserId {

        @Test
        @DisplayName("should return preferences for user as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnPreferencesForUser() throws Exception {
            when(preferenceService.getByUserId(userId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/notification-preferences/{id}/toggle")
    class TogglePreference {

        @Test
        @DisplayName("should toggle preference as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldTogglePreferenceAsBranchAdmin() throws Exception {
            when(preferenceService.togglePreference(preferenceId)).thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/toggle", preferenceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
