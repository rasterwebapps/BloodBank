package com.bloodbank.hospitalservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackResponse;
import com.bloodbank.hospitalservice.enums.FeedbackCategoryEnum;
import com.bloodbank.hospitalservice.service.FeedbackService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = HospitalFeedbackController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class HospitalFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeedbackService feedbackService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/hospital-feedback";
    private UUID feedbackId;
    private UUID hospitalId;
    private UUID requestId;
    private UUID branchId;
    private HospitalFeedbackResponse sampleResponse;
    private HospitalFeedbackCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        feedbackId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new HospitalFeedbackResponse(
                feedbackId, hospitalId, requestId, Instant.now(),
                4, FeedbackCategoryEnum.SERVICE_QUALITY,
                "Good service overall", null, null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new HospitalFeedbackCreateRequest(
                hospitalId, requestId, 4,
                FeedbackCategoryEnum.SERVICE_QUALITY,
                "Good service overall", branchId
        );
    }

    // ==================== SUBMIT FEEDBACK ====================

    @Nested
    @DisplayName("POST /api/v1/hospital-feedback")
    class SubmitFeedback {

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should submit feedback as HOSPITAL_USER - 201")
        void submitFeedback_asHospitalUser_returns201() throws Exception {
            when(feedbackService.submitFeedback(any(HospitalFeedbackCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.rating").value(4))
                    .andExpect(jsonPath("$.data.category").value("SERVICE_QUALITY"))
                    .andExpect(jsonPath("$.message").value("Feedback submitted successfully"));
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should submit feedback as SUPER_ADMIN - 201")
        void submitFeedback_asSuperAdmin_returns201() throws Exception {
            when(feedbackService.submitFeedback(any(HospitalFeedbackCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should deny BRANCH_ADMIN from submitting feedback - 403")
        void submitFeedback_asBranchAdmin_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from submitting feedback - 403")
        void submitFeedback_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void submitFeedback_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET FEEDBACK BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/hospital-feedback/{id}")
    class GetFeedbackById {

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get feedback by ID as HOSPITAL_USER - 200")
        void getFeedbackById_asHospitalUser_returns200() throws Exception {
            when(feedbackService.getFeedbackById(feedbackId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", feedbackId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(feedbackId.toString()));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get feedback by ID as BRANCH_ADMIN - 200")
        void getFeedbackById_asBranchAdmin_returns200() throws Exception {
            when(feedbackService.getFeedbackById(feedbackId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", feedbackId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting feedback - 403")
        void getFeedbackById_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", feedbackId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void getFeedbackById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", feedbackId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET FEEDBACK BY HOSPITAL ====================

    @Nested
    @DisplayName("GET /api/v1/hospital-feedback/hospital/{hospitalId}")
    class GetFeedbackByHospital {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get feedback by hospital as BRANCH_ADMIN - 200")
        void getFeedbackByHospital_asBranchAdmin_returns200() throws Exception {
            PagedResponse<HospitalFeedbackResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(feedbackService.getFeedbackByHospitalId(eq(hospitalId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/hospital/{hospitalId}", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get feedback by hospital as HOSPITAL_USER - 200")
        void getFeedbackByHospital_asHospitalUser_returns200() throws Exception {
            PagedResponse<HospitalFeedbackResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(feedbackService.getFeedbackByHospitalId(eq(hospitalId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/hospital/{hospitalId}", hospitalId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting feedback by hospital - 403")
        void getFeedbackByHospital_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/hospital/{hospitalId}", hospitalId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET FEEDBACK BY REQUEST ====================

    @Nested
    @DisplayName("GET /api/v1/hospital-feedback/request/{requestId}")
    class GetFeedbackByRequest {

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get feedback by request as HOSPITAL_USER - 200")
        void getFeedbackByRequest_asHospitalUser_returns200() throws Exception {
            when(feedbackService.getFeedbackByRequestId(requestId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/request/{requestId}", requestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get feedback by request as BRANCH_MANAGER - 200")
        void getFeedbackByRequest_asBranchManager_returns200() throws Exception {
            when(feedbackService.getFeedbackByRequestId(requestId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/request/{requestId}", requestId))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting feedback by request - 403")
        void getFeedbackByRequest_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/request/{requestId}", requestId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== RESPOND TO FEEDBACK ====================

    @Nested
    @DisplayName("PUT /api/v1/hospital-feedback/{id}/respond")
    class RespondToFeedback {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should respond to feedback as BRANCH_ADMIN - 200")
        void respondToFeedback_asBranchAdmin_returns200() throws Exception {
            when(feedbackService.respondToFeedback(eq(feedbackId), eq("Thank you"), eq("admin")))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/respond", feedbackId)
                            .param("responseText", "Thank you")
                            .param("respondedBy", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Response submitted successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should respond to feedback as BRANCH_MANAGER - 200")
        void respondToFeedback_asBranchManager_returns200() throws Exception {
            when(feedbackService.respondToFeedback(eq(feedbackId), eq("Thank you"), eq("manager")))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/respond", feedbackId)
                            .param("responseText", "Thank you")
                            .param("respondedBy", "manager"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER from responding to feedback - 403")
        void respondToFeedback_asHospitalUser_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/respond", feedbackId)
                            .param("responseText", "Thank you")
                            .param("respondedBy", "admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from responding to feedback - 403")
        void respondToFeedback_asDonor_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/respond", feedbackId)
                            .param("responseText", "Thank you")
                            .param("respondedBy", "admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void respondToFeedback_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/respond", feedbackId)
                            .param("responseText", "Thank you")
                            .param("respondedBy", "admin"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
