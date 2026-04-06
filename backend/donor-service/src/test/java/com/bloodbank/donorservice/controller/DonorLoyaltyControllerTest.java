package com.bloodbank.donorservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.donorservice.dto.DonorLoyaltyResponse;
import com.bloodbank.donorservice.dto.LoyaltyRedeemRequest;
import com.bloodbank.donorservice.enums.LoyaltyTierEnum;
import com.bloodbank.donorservice.service.DonorLoyaltyService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = DonorLoyaltyController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class DonorLoyaltyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DonorLoyaltyService donorLoyaltyService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UUID donorId;
    private UUID branchId;
    private DonorLoyaltyResponse sampleLoyaltyResponse;

    @BeforeEach
    void setUp() {
        donorId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleLoyaltyResponse = new DonorLoyaltyResponse(
                UUID.randomUUID(), donorId, 500, 100, 400,
                LoyaltyTierEnum.SILVER, LocalDateTime.now(),
                branchId, LocalDateTime.now()
        );
    }

    // ==================== GET LOYALTY ====================

    @Nested
    @DisplayName("GET /api/v1/donors/{donorId}/loyalty")
    class GetLoyalty {

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should get loyalty as DONOR")
        void getLoyalty_asDonor_returns200() throws Exception {
            when(donorLoyaltyService.getOrCreateLoyalty(donorId))
                    .thenReturn(sampleLoyaltyResponse);

            mockMvc.perform(get("/api/v1/donors/{donorId}/loyalty", donorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.pointsBalance").value(400))
                    .andExpect(jsonPath("$.data.tier").value("SILVER"));
        }

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should get loyalty as RECEPTIONIST")
        void getLoyalty_asReceptionist_returns200() throws Exception {
            when(donorLoyaltyService.getOrCreateLoyalty(donorId))
                    .thenReturn(sampleLoyaltyResponse);

            mockMvc.perform(get("/api/v1/donors/{donorId}/loyalty", donorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = {"LAB_TECHNICIAN"})
        @DisplayName("Should deny LAB_TECHNICIAN from getting loyalty")
        void getLoyalty_asLabTechnician_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/donors/{donorId}/loyalty", donorId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void getLoyalty_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/donors/{donorId}/loyalty", donorId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== AWARD POINTS ====================

    @Nested
    @DisplayName("POST /api/v1/donors/{donorId}/loyalty/award")
    class AwardPoints {

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should award points as RECEPTIONIST")
        void awardPoints_asReceptionist_returns200() throws Exception {
            when(donorLoyaltyService.awardPoints(eq(donorId), eq(100)))
                    .thenReturn(sampleLoyaltyResponse);

            mockMvc.perform(post("/api/v1/donors/{donorId}/loyalty/award", donorId)
                            .param("points", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Points awarded successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should award points as BRANCH_MANAGER")
        void awardPoints_asBranchManager_returns200() throws Exception {
            when(donorLoyaltyService.awardPoints(eq(donorId), eq(100)))
                    .thenReturn(sampleLoyaltyResponse);

            mockMvc.perform(post("/api/v1/donors/{donorId}/loyalty/award", donorId)
                            .param("points", "100"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from awarding points")
        void awardPoints_asDonor_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/donors/{donorId}/loyalty/award", donorId)
                            .param("points", "100"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== REDEEM POINTS ====================

    @Nested
    @DisplayName("POST /api/v1/donors/{donorId}/loyalty/redeem")
    class RedeemPoints {

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should redeem points as DONOR")
        void redeemPoints_asDonor_returns200() throws Exception {
            LoyaltyRedeemRequest redeemRequest = new LoyaltyRedeemRequest(50);
            when(donorLoyaltyService.redeemPoints(eq(donorId), eq(50)))
                    .thenReturn(sampleLoyaltyResponse);

            mockMvc.perform(post("/api/v1/donors/{donorId}/loyalty/redeem", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(redeemRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Points redeemed successfully"));
        }

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should redeem points as RECEPTIONIST")
        void redeemPoints_asReceptionist_returns200() throws Exception {
            LoyaltyRedeemRequest redeemRequest = new LoyaltyRedeemRequest(50);
            when(donorLoyaltyService.redeemPoints(eq(donorId), eq(50)))
                    .thenReturn(sampleLoyaltyResponse);

            mockMvc.perform(post("/api/v1/donors/{donorId}/loyalty/redeem", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(redeemRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"LAB_TECHNICIAN"})
        @DisplayName("Should deny LAB_TECHNICIAN from redeeming points")
        void redeemPoints_asLabTechnician_returns403() throws Exception {
            LoyaltyRedeemRequest redeemRequest = new LoyaltyRedeemRequest(50);

            mockMvc.perform(post("/api/v1/donors/{donorId}/loyalty/redeem", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(redeemRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void redeemPoints_unauthenticated_returns401() throws Exception {
            LoyaltyRedeemRequest redeemRequest = new LoyaltyRedeemRequest(50);

            mockMvc.perform(post("/api/v1/donors/{donorId}/loyalty/redeem", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(redeemRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
