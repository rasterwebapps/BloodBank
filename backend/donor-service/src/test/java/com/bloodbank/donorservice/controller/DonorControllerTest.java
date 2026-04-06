package com.bloodbank.donorservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.model.enums.DonorStatusEnum;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.donorservice.dto.*;
import com.bloodbank.donorservice.enums.*;
import com.bloodbank.donorservice.service.DonorService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = DonorController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class DonorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DonorService donorService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/donors";
    private UUID donorId;
    private UUID branchId;
    private UUID bloodGroupId;
    private UUID cityId;
    private DonorResponse sampleDonorResponse;
    private DonorCreateRequest sampleCreateRequest;
    private DonorUpdateRequest sampleUpdateRequest;

    @BeforeEach
    void setUp() {
        donorId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        cityId = UUID.randomUUID();

        sampleDonorResponse = new DonorResponse(
                donorId, "DN-000001", "John", "Doe",
                LocalDate.of(1990, 5, 15), GenderEnum.MALE,
                bloodGroupId, "POSITIVE", "john@example.com", "+1234567890",
                "123 Main St", null, cityId, "62701",
                "NAT-001", "US", "Engineer", DonorTypeEnum.VOLUNTARY,
                DonorStatusEnum.ACTIVE, LocalDate.of(2024, 1, 15), 5,
                LocalDate.of(2023, 1, 1), null, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new DonorCreateRequest(
                "John", "Doe", LocalDate.of(1990, 5, 15), GenderEnum.MALE,
                bloodGroupId, "POSITIVE", "john@example.com", "+1234567890",
                "123 Main St", null, cityId, "62701",
                "NAT-001", "US", "Engineer", DonorTypeEnum.VOLUNTARY, branchId
        );

        sampleUpdateRequest = new DonorUpdateRequest(
                "Jane", "Doe", LocalDate.of(1990, 5, 15), GenderEnum.FEMALE,
                bloodGroupId, "NEGATIVE", "jane@example.com", "+0987654321",
                "456 Oak Ave", null, cityId, "62702",
                "NAT-002", "US", "Doctor"
        );
    }

    // ==================== REGISTER DONOR ====================

    @Nested
    @DisplayName("POST /api/v1/donors")
    class RegisterDonor {

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should register donor as RECEPTIONIST")
        void registerDonor_asReceptionist_returns201() throws Exception {
            when(donorService.registerDonor(any(DonorCreateRequest.class)))
                    .thenReturn(sampleDonorResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(donorId.toString()))
                    .andExpect(jsonPath("$.data.firstName").value("John"))
                    .andExpect(jsonPath("$.data.lastName").value("Doe"))
                    .andExpect(jsonPath("$.message").value("Donor registered successfully"));
        }

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should register donor as PHLEBOTOMIST")
        void registerDonor_asPhlebotomist_returns201() throws Exception {
            when(donorService.registerDonor(any(DonorCreateRequest.class)))
                    .thenReturn(sampleDonorResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from registering a donor")
        void registerDonor_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void registerDonor_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== UPDATE DONOR ====================

    @Nested
    @DisplayName("PUT /api/v1/donors/{id}")
    class UpdateDonor {

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should update donor as RECEPTIONIST")
        void updateDonor_asReceptionist_returns200() throws Exception {
            when(donorService.updateDonor(eq(donorId), any(DonorUpdateRequest.class)))
                    .thenReturn(sampleDonorResponse);

            mockMvc.perform(put(BASE_URL + "/{id}", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(donorId.toString()))
                    .andExpect(jsonPath("$.message").value("Donor updated successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from updating a donor")
        void updateDonor_asDonor_returns403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET DONOR BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/donors/{id}")
    class GetDonorById {

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should get donor by ID as RECEPTIONIST")
        void getDonorById_asReceptionist_returns200() throws Exception {
            when(donorService.getDonorById(donorId)).thenReturn(sampleDonorResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", donorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.donorNumber").value("DN-000001"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting donor by ID")
        void getDonorById_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", donorId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET DONOR BY NUMBER ====================

    @Nested
    @DisplayName("GET /api/v1/donors/number/{donorNumber}")
    class GetDonorByNumber {

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should get donor by number as PHLEBOTOMIST")
        void getDonorByNumber_asPhlebotomist_returns200() throws Exception {
            when(donorService.getDonorByDonorNumber("DN-000001")).thenReturn(sampleDonorResponse);

            mockMvc.perform(get(BASE_URL + "/number/{donorNumber}", "DN-000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.donorNumber").value("DN-000001"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting donor by number")
        void getDonorByNumber_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/number/{donorNumber}", "DN-000001"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== SEARCH DONORS ====================

    @Nested
    @DisplayName("GET /api/v1/donors/search")
    class SearchDonors {

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should search donors as BRANCH_MANAGER")
        void searchDonors_asBranchManager_returns200() throws Exception {
            PagedResponse<DonorResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleDonorResponse), 0, 20, 1, 1, true
            );
            when(donorService.searchDonors(eq("John"), any())).thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/search").param("query", "John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].firstName").value("John"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from searching donors")
        void searchDonors_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/search").param("query", "John"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET DONORS BY STATUS ====================

    @Nested
    @DisplayName("GET /api/v1/donors/status/{status}")
    class GetDonorsByStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get donors by status as BRANCH_ADMIN")
        void getDonorsByStatus_asBranchAdmin_returns200() throws Exception {
            PagedResponse<DonorResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleDonorResponse), 0, 20, 1, 1, true
            );
            when(donorService.getDonorsByStatus(eq(DonorStatusEnum.ACTIVE), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get(BASE_URL + "/status/{status}", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting donors by status")
        void getDonorsByStatus_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/status/{status}", "ACTIVE"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== CHECK ELIGIBILITY ====================

    @Nested
    @DisplayName("GET /api/v1/donors/{donorId}/eligibility")
    class CheckEligibility {

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should check eligibility as PHLEBOTOMIST")
        void checkEligibility_asPhlebotomist_returns200() throws Exception {
            DonorHealthRecordResponse healthRecord = new DonorHealthRecordResponse(
                    UUID.randomUUID(), donorId, LocalDateTime.now(),
                    new BigDecimal("70.0"), new BigDecimal("175.0"),
                    120, 80, 72, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), true, "Healthy", "Dr. Smith",
                    branchId, LocalDateTime.now()
            );
            when(donorService.checkEligibility(donorId)).thenReturn(healthRecord);

            mockMvc.perform(get(BASE_URL + "/{donorId}/eligibility", donorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isEligible").value(true));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from checking eligibility")
        void checkEligibility_asDonor_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{donorId}/eligibility", donorId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== DEFER DONOR ====================

    @Nested
    @DisplayName("POST /api/v1/donors/{donorId}/defer")
    class DeferDonor {

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should defer donor as DOCTOR")
        void deferDonor_asDoctor_returns201() throws Exception {
            UUID deferralReasonId = UUID.randomUUID();
            DonorDeferralCreateRequest deferralRequest = new DonorDeferralCreateRequest(
                    donorId, deferralReasonId, DeferralTypeEnum.TEMPORARY,
                    LocalDate.now().plusMonths(3), "Low hemoglobin", "Dr. Smith", branchId
            );
            DonorDeferralResponse deferralResponse = new DonorDeferralResponse(
                    UUID.randomUUID(), donorId, deferralReasonId, DeferralTypeEnum.TEMPORARY,
                    LocalDate.now(), LocalDate.now().plusMonths(3), "Low hemoglobin",
                    "Dr. Smith", DeferralStatusEnum.ACTIVE, branchId, LocalDateTime.now()
            );
            when(donorService.deferDonor(any(DonorDeferralCreateRequest.class)))
                    .thenReturn(deferralResponse);

            mockMvc.perform(post(BASE_URL + "/{donorId}/defer", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deferralRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Donor deferred successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from deferring a donor")
        void deferDonor_asDonor_returns403() throws Exception {
            DonorDeferralCreateRequest deferralRequest = new DonorDeferralCreateRequest(
                    donorId, UUID.randomUUID(), DeferralTypeEnum.TEMPORARY,
                    LocalDate.now().plusMonths(3), "Low hemoglobin", "Dr. Smith", branchId
            );

            mockMvc.perform(post(BASE_URL + "/{donorId}/defer", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deferralRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== HEALTH RECORDS ====================

    @Nested
    @DisplayName("Health Record endpoints")
    class HealthRecordEndpoints {

        private DonorHealthRecordCreateRequest healthRecordRequest;
        private DonorHealthRecordResponse healthRecordResponse;

        @BeforeEach
        void setUp() {
            healthRecordRequest = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("70.0"), new BigDecimal("175.0"),
                    120, 80, 72, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), "Healthy", "Nurse Jane", branchId
            );
            healthRecordResponse = new DonorHealthRecordResponse(
                    UUID.randomUUID(), donorId, LocalDateTime.now(),
                    new BigDecimal("70.0"), new BigDecimal("175.0"),
                    120, 80, 72, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), true, "Healthy", "Nurse Jane",
                    branchId, LocalDateTime.now()
            );
        }

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should create health record as PHLEBOTOMIST")
        void createHealthRecord_asPhlebotomist_returns201() throws Exception {
            when(donorService.createHealthRecord(any(DonorHealthRecordCreateRequest.class)))
                    .thenReturn(healthRecordResponse);

            mockMvc.perform(post(BASE_URL + "/{donorId}/health-records", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(healthRecordRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Health record created successfully"));
        }

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get health records as DOCTOR")
        void getHealthRecords_asDoctor_returns200() throws Exception {
            when(donorService.getHealthRecords(donorId))
                    .thenReturn(List.of(healthRecordResponse));

            mockMvc.perform(get(BASE_URL + "/{donorId}/health-records", donorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from creating health record")
        void createHealthRecord_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{donorId}/health-records", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(healthRecordRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== CONSENTS ====================

    @Nested
    @DisplayName("Consent endpoints")
    class ConsentEndpoints {

        private DonorConsentCreateRequest consentRequest;
        private DonorConsentResponse consentResponse;

        @BeforeEach
        void setUp() {
            consentRequest = new DonorConsentCreateRequest(
                    donorId, "BLOOD_DONATION", true,
                    "I consent to blood donation", "SIG-001", "192.168.1.1", branchId
            );
            consentResponse = new DonorConsentResponse(
                    UUID.randomUUID(), donorId, "BLOOD_DONATION", true,
                    LocalDateTime.now(), LocalDateTime.now().plusYears(1),
                    "I consent to blood donation", "SIG-001", "192.168.1.1",
                    null, branchId, LocalDateTime.now()
            );
        }

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should create consent as RECEPTIONIST")
        void createConsent_asReceptionist_returns201() throws Exception {
            when(donorService.createConsent(any(DonorConsentCreateRequest.class)))
                    .thenReturn(consentResponse);

            mockMvc.perform(post(BASE_URL + "/{donorId}/consents", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(consentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Consent recorded successfully"));
        }

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should get consents as RECEPTIONIST")
        void getConsents_asReceptionist_returns200() throws Exception {
            when(donorService.getConsents(donorId))
                    .thenReturn(List.of(consentResponse));

            mockMvc.perform(get(BASE_URL + "/{donorId}/consents", donorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from creating consent")
        void createConsent_asDonor_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{donorId}/consents", donorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(consentRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated consent request")
        void getConsents_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{donorId}/consents", donorId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
