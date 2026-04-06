package com.bloodbank.donorservice.controller;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.model.enums.CollectionStatusEnum;
import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.donorservice.dto.*;
import com.bloodbank.donorservice.enums.*;
import com.bloodbank.donorservice.service.CollectionService;
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

@WebMvcTest(value = CollectionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollectionService collectionService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/collections";
    private UUID collectionId;
    private UUID donorId;
    private UUID branchId;
    private CollectionResponse sampleCollectionResponse;
    private CollectionCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        collectionId = UUID.randomUUID();
        donorId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleCollectionResponse = new CollectionResponse(
                collectionId, donorId, UUID.randomUUID(), "COL-000001",
                LocalDateTime.now(), CollectionTypeEnum.WHOLE_BLOOD,
                DonationTypeEnum.VOLUNTARY, 450, "SINGLE", "LOT-001",
                "PHLEB-001", LocalDateTime.now(), null,
                CollectionStatusEnum.IN_PROGRESS, "Normal collection", null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new CollectionCreateRequest(
                donorId, UUID.randomUUID(), CollectionTypeEnum.WHOLE_BLOOD,
                DonationTypeEnum.VOLUNTARY, "SINGLE", "LOT-001",
                "PHLEB-001", "Normal collection", branchId
        );
    }

    // ==================== START COLLECTION ====================

    @Nested
    @DisplayName("POST /api/v1/collections")
    class StartCollection {

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should start collection as PHLEBOTOMIST")
        void startCollection_asPhlebotomist_returns201() throws Exception {
            when(collectionService.startCollection(any(CollectionCreateRequest.class)))
                    .thenReturn(sampleCollectionResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(collectionId.toString()))
                    .andExpect(jsonPath("$.message").value("Collection started successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from starting a collection")
        void startCollection_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void startCollection_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== COMPLETE COLLECTION ====================

    @Nested
    @DisplayName("PUT /api/v1/collections/{id}/complete")
    class CompleteCollection {

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should complete collection as PHLEBOTOMIST")
        void completeCollection_asPhlebotomist_returns200() throws Exception {
            CollectionCompleteRequest completeRequest = new CollectionCompleteRequest(
                    450, LocalDateTime.now(), "Completed without issues"
            );
            when(collectionService.completeCollection(eq(collectionId), any(CollectionCompleteRequest.class)))
                    .thenReturn(sampleCollectionResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/complete", collectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(completeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Collection completed successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from completing a collection")
        void completeCollection_asDonor_returns403() throws Exception {
            CollectionCompleteRequest completeRequest = new CollectionCompleteRequest(
                    450, LocalDateTime.now(), "Completed"
            );
            mockMvc.perform(put(BASE_URL + "/{id}/complete", collectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(completeRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET COLLECTION BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/collections/{id}")
    class GetCollectionById {

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should get collection by ID as PHLEBOTOMIST")
        void getCollectionById_asPhlebotomist_returns200() throws Exception {
            when(collectionService.getCollectionById(collectionId))
                    .thenReturn(sampleCollectionResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", collectionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.collectionNumber").value("COL-000001"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting collection")
        void getCollectionById_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", collectionId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET COLLECTIONS BY DONOR ====================

    @Nested
    @DisplayName("GET /api/v1/collections/donor/{donorId}")
    class GetCollectionsByDonor {

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should get collections by donor as PHLEBOTOMIST")
        void getCollectionsByDonor_asPhlebotomist_returns200() throws Exception {
            when(collectionService.getCollectionsByDonor(donorId))
                    .thenReturn(List.of(sampleCollectionResponse));

            mockMvc.perform(get(BASE_URL + "/donor/{donorId}", donorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting collections by donor")
        void getCollectionsByDonor_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/donor/{donorId}", donorId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET COLLECTIONS BY STATUS ====================

    @Nested
    @DisplayName("GET /api/v1/collections/status/{status}")
    class GetCollectionsByStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get collections by status as BRANCH_MANAGER")
        void getCollectionsByStatus_asBranchManager_returns200() throws Exception {
            PagedResponse<CollectionResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleCollectionResponse), 0, 20, 1, 1, true
            );
            when(collectionService.getCollectionsByStatus(eq(CollectionStatusEnum.IN_PROGRESS), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/status/{status}", "IN_PROGRESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting collections by status")
        void getCollectionsByStatus_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/status/{status}", "IN_PROGRESS"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== ADVERSE REACTIONS ====================

    @Nested
    @DisplayName("Adverse Reaction endpoints")
    class AdverseReactionEndpoints {

        private AdverseReactionCreateRequest reactionRequest;
        private AdverseReactionResponse reactionResponse;

        @BeforeEach
        void setUp() {
            reactionRequest = new AdverseReactionCreateRequest(
                    collectionId, "VASOVAGAL", SeverityEnum.MILD,
                    LocalDateTime.now(), "Felt dizzy", "Rest and fluids",
                    ReactionOutcomeEnum.RESOLVED, "PHLEB-001", branchId
            );
            reactionResponse = new AdverseReactionResponse(
                    UUID.randomUUID(), collectionId, "VASOVAGAL", SeverityEnum.MILD,
                    LocalDateTime.now(), "Felt dizzy", "Rest and fluids",
                    ReactionOutcomeEnum.RESOLVED, "PHLEB-001", branchId, LocalDateTime.now()
            );
        }

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should record adverse reaction as PHLEBOTOMIST")
        void recordAdverseReaction_asPhlebotomist_returns201() throws Exception {
            when(collectionService.recordAdverseReaction(any(AdverseReactionCreateRequest.class)))
                    .thenReturn(reactionResponse);

            mockMvc.perform(post(BASE_URL + "/{collectionId}/adverse-reactions", collectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reactionRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Adverse reaction recorded successfully"));
        }

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get adverse reactions as DOCTOR")
        void getAdverseReactions_asDoctor_returns200() throws Exception {
            when(collectionService.getAdverseReactions(collectionId))
                    .thenReturn(List.of(reactionResponse));

            mockMvc.perform(get(BASE_URL + "/{collectionId}/adverse-reactions", collectionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from recording adverse reaction")
        void recordAdverseReaction_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{collectionId}/adverse-reactions", collectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reactionRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== SAMPLES ====================

    @Nested
    @DisplayName("Sample endpoints")
    class SampleEndpoints {

        private CollectionSampleCreateRequest sampleRequest;
        private CollectionSampleResponse sampleResponse;

        @BeforeEach
        void setUp() {
            sampleRequest = new CollectionSampleCreateRequest(
                    collectionId, SampleTypeEnum.EDTA, "Standard sample", branchId
            );
            sampleResponse = new CollectionSampleResponse(
                    UUID.randomUUID(), collectionId, "SMP-000001", SampleTypeEnum.EDTA,
                    LocalDateTime.now(), SampleStatusEnum.COLLECTED, "Standard sample",
                    branchId, LocalDateTime.now()
            );
        }

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should add sample as PHLEBOTOMIST")
        void addSample_asPhlebotomist_returns201() throws Exception {
            when(collectionService.addSample(any(CollectionSampleCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL + "/{collectionId}/samples", collectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Sample added successfully"));
        }

        @Test
        @WithMockUser(roles = {"LAB_TECHNICIAN"})
        @DisplayName("Should get samples as LAB_TECHNICIAN")
        void getSamples_asLabTechnician_returns200() throws Exception {
            when(collectionService.getSamples(collectionId))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/{collectionId}/samples", collectionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from adding sample")
        void addSample_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{collectionId}/samples", collectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated sample request")
        void getSamples_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{collectionId}/samples", collectionId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
