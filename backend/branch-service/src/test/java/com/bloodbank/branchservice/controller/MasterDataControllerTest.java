package com.bloodbank.branchservice.controller;

import com.bloodbank.branchservice.dto.*;
import com.bloodbank.branchservice.service.MasterDataService;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = MasterDataController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class MasterDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MasterDataService masterDataService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/master-data";

    // ==================== BLOOD GROUPS ====================

    @Nested
    @DisplayName("Blood Groups endpoints")
    class BloodGroupEndpoints {

        private UUID bloodGroupId;
        private BloodGroupResponse sampleBloodGroup;
        private List<BloodGroupResponse> bloodGroupList;

        @BeforeEach
        void setUp() {
            bloodGroupId = UUID.randomUUID();
            sampleBloodGroup = new BloodGroupResponse(
                    bloodGroupId, "O+", "O Positive blood group", true
            );
            bloodGroupList = List.of(
                    sampleBloodGroup,
                    new BloodGroupResponse(UUID.randomUUID(), "A+", "A Positive blood group", true),
                    new BloodGroupResponse(UUID.randomUUID(), "B+", "B Positive blood group", true),
                    new BloodGroupResponse(UUID.randomUUID(), "AB+", "AB Positive blood group", true)
            );
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get all blood groups as authenticated user")
        void getAllBloodGroups_authenticated_returns200() throws Exception {
            when(masterDataService.getAllBloodGroups()).thenReturn(bloodGroupList);

            mockMvc.perform(get(BASE_URL + "/blood-groups"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(4))
                    .andExpect(jsonPath("$.data[0].groupName").value("O+"))
                    .andExpect(jsonPath("$.data[0].description").value("O Positive blood group"))
                    .andExpect(jsonPath("$.data[0].isActive").value(true));

            verify(masterDataService).getAllBloodGroups();
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should allow DONOR to get all blood groups")
        void getAllBloodGroups_asDonor_returns200() throws Exception {
            when(masterDataService.getAllBloodGroups()).thenReturn(bloodGroupList);

            mockMvc.perform(get(BASE_URL + "/blood-groups"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated user")
        void getAllBloodGroups_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/blood-groups"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get blood group by ID")
        void getBloodGroupById_authenticated_returns200() throws Exception {
            when(masterDataService.getBloodGroupById(bloodGroupId)).thenReturn(sampleBloodGroup);

            mockMvc.perform(get(BASE_URL + "/blood-groups/{id}", bloodGroupId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(bloodGroupId.toString()))
                    .andExpect(jsonPath("$.data.groupName").value("O+"));

            verify(masterDataService).getBloodGroupById(bloodGroupId);
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated blood group by ID request")
        void getBloodGroupById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/blood-groups/{id}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== COMPONENT TYPES ====================

    @Nested
    @DisplayName("Component Types endpoints")
    class ComponentTypeEndpoints {

        private UUID componentTypeId;
        private ComponentTypeResponse sampleComponentType;

        @BeforeEach
        void setUp() {
            componentTypeId = UUID.randomUUID();
            sampleComponentType = new ComponentTypeResponse(
                    componentTypeId, "WB", "Whole Blood",
                    "Complete blood unit", 35,
                    new BigDecimal("2.0"), new BigDecimal("6.0"), true
            );
        }

        @Test
        @WithMockUser(roles = {"LAB_TECHNICIAN"})
        @DisplayName("Should get all component types as authenticated user")
        void getAllComponentTypes_authenticated_returns200() throws Exception {
            List<ComponentTypeResponse> list = List.of(
                    sampleComponentType,
                    new ComponentTypeResponse(UUID.randomUUID(), "RBC", "Red Blood Cells",
                            "Packed RBCs", 42, new BigDecimal("2.0"), new BigDecimal("6.0"), true)
            );
            when(masterDataService.getAllComponentTypes()).thenReturn(list);

            mockMvc.perform(get(BASE_URL + "/component-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].typeCode").value("WB"))
                    .andExpect(jsonPath("$.data[0].typeName").value("Whole Blood"))
                    .andExpect(jsonPath("$.data[0].shelfLifeDays").value(35));

            verify(masterDataService).getAllComponentTypes();
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get component type by ID")
        void getComponentTypeById_authenticated_returns200() throws Exception {
            when(masterDataService.getComponentTypeById(componentTypeId)).thenReturn(sampleComponentType);

            mockMvc.perform(get(BASE_URL + "/component-types/{id}", componentTypeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(componentTypeId.toString()))
                    .andExpect(jsonPath("$.data.typeCode").value("WB"));
        }

        @Test
        @WithMockUser(roles = {"INVENTORY_MANAGER"})
        @DisplayName("Should get component type by code")
        void getComponentTypeByCode_authenticated_returns200() throws Exception {
            when(masterDataService.getComponentTypeByCode("WB")).thenReturn(sampleComponentType);

            mockMvc.perform(get(BASE_URL + "/component-types/code/{code}", "WB"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.typeCode").value("WB"))
                    .andExpect(jsonPath("$.data.typeName").value("Whole Blood"));

            verify(masterDataService).getComponentTypeByCode("WB");
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated component type request")
        void getAllComponentTypes_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/component-types"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== COUNTRIES ====================

    @Nested
    @DisplayName("Countries endpoints")
    class CountryEndpoints {

        private UUID countryId;
        private CountryResponse sampleCountry;

        @BeforeEach
        void setUp() {
            countryId = UUID.randomUUID();
            sampleCountry = new CountryResponse(
                    countryId, "US", "United States", "+1", true
            );
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get all countries")
        void getAllCountries_authenticated_returns200() throws Exception {
            List<CountryResponse> countries = List.of(
                    sampleCountry,
                    new CountryResponse(UUID.randomUUID(), "GB", "United Kingdom", "+44", true)
            );
            when(masterDataService.getAllCountries()).thenReturn(countries);

            mockMvc.perform(get(BASE_URL + "/countries"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].countryCode").value("US"))
                    .andExpect(jsonPath("$.data[0].countryName").value("United States"))
                    .andExpect(jsonPath("$.data[0].phoneCode").value("+1"));

            verify(masterDataService).getAllCountries();
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should get country by ID as DONOR")
        void getCountryById_asDonor_returns200() throws Exception {
            when(masterDataService.getCountryById(countryId)).thenReturn(sampleCountry);

            mockMvc.perform(get(BASE_URL + "/countries/{id}", countryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(countryId.toString()))
                    .andExpect(jsonPath("$.data.countryCode").value("US"));
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated country request")
        void getAllCountries_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/countries"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== REGIONS ====================

    @Nested
    @DisplayName("Regions endpoints")
    class RegionEndpoints {

        private UUID regionId;
        private UUID countryId;
        private RegionResponse sampleRegion;

        @BeforeEach
        void setUp() {
            regionId = UUID.randomUUID();
            countryId = UUID.randomUUID();
            sampleRegion = new RegionResponse(
                    regionId, countryId, "United States", "IL", "Illinois", true
            );
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get all regions")
        void getAllRegions_authenticated_returns200() throws Exception {
            when(masterDataService.getAllRegions()).thenReturn(List.of(sampleRegion));

            mockMvc.perform(get(BASE_URL + "/regions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].regionCode").value("IL"))
                    .andExpect(jsonPath("$.data[0].regionName").value("Illinois"));

            verify(masterDataService).getAllRegions();
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should get region by ID")
        void getRegionById_authenticated_returns200() throws Exception {
            when(masterDataService.getRegionById(regionId)).thenReturn(sampleRegion);

            mockMvc.perform(get(BASE_URL + "/regions/{id}", regionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(regionId.toString()))
                    .andExpect(jsonPath("$.data.regionName").value("Illinois"));
        }

        @Test
        @WithMockUser(roles = {"RECEPTIONIST"})
        @DisplayName("Should get regions by country")
        void getRegionsByCountry_authenticated_returns200() throws Exception {
            when(masterDataService.getRegionsByCountry(countryId)).thenReturn(List.of(sampleRegion));

            mockMvc.perform(get(BASE_URL + "/countries/{countryId}/regions", countryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].countryId").value(countryId.toString()));

            verify(masterDataService).getRegionsByCountry(countryId);
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated region request")
        void getAllRegions_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/regions"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== CITIES ====================

    @Nested
    @DisplayName("Cities endpoints")
    class CityEndpoints {

        private UUID cityId;
        private UUID regionId;
        private CityResponse sampleCity;

        @BeforeEach
        void setUp() {
            cityId = UUID.randomUUID();
            regionId = UUID.randomUUID();
            sampleCity = new CityResponse(
                    cityId, regionId, "Illinois", "Springfield", "62701", true
            );
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get all cities")
        void getAllCities_authenticated_returns200() throws Exception {
            when(masterDataService.getAllCities()).thenReturn(List.of(sampleCity));

            mockMvc.perform(get(BASE_URL + "/cities"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].cityName").value("Springfield"))
                    .andExpect(jsonPath("$.data[0].postalCode").value("62701"));

            verify(masterDataService).getAllCities();
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should get city by ID as DONOR")
        void getCityById_asDonor_returns200() throws Exception {
            when(masterDataService.getCityById(cityId)).thenReturn(sampleCity);

            mockMvc.perform(get(BASE_URL + "/cities/{id}", cityId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(cityId.toString()))
                    .andExpect(jsonPath("$.data.cityName").value("Springfield"));
        }

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should get cities by region")
        void getCitiesByRegion_authenticated_returns200() throws Exception {
            when(masterDataService.getCitiesByRegion(regionId)).thenReturn(List.of(sampleCity));

            mockMvc.perform(get(BASE_URL + "/regions/{regionId}/cities", regionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].regionId").value(regionId.toString()));

            verify(masterDataService).getCitiesByRegion(regionId);
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated city request")
        void getAllCities_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/cities"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== DEFERRAL REASONS ====================

    @Nested
    @DisplayName("Deferral Reasons endpoints")
    class DeferralReasonEndpoints {

        private UUID deferralReasonId;
        private DeferralReasonResponse sampleDeferralReason;

        @BeforeEach
        void setUp() {
            deferralReasonId = UUID.randomUUID();
            sampleDeferralReason = new DeferralReasonResponse(
                    deferralReasonId, "DR-001", "Low hemoglobin",
                    "TEMPORARY", 90, true
            );
        }

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get all deferral reasons as authenticated user")
        void getAllDeferralReasons_authenticated_returns200() throws Exception {
            List<DeferralReasonResponse> reasons = List.of(
                    sampleDeferralReason,
                    new DeferralReasonResponse(UUID.randomUUID(), "DR-002",
                            "Recent travel to malaria zone", "TEMPORARY", 365, true)
            );
            when(masterDataService.getAllDeferralReasons()).thenReturn(reasons);

            mockMvc.perform(get(BASE_URL + "/deferral-reasons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].reasonCode").value("DR-001"))
                    .andExpect(jsonPath("$.data[0].reasonDescription").value("Low hemoglobin"))
                    .andExpect(jsonPath("$.data[0].deferralType").value("TEMPORARY"))
                    .andExpect(jsonPath("$.data[0].defaultDays").value(90));

            verify(masterDataService).getAllDeferralReasons();
        }

        @Test
        @WithMockUser(roles = {"PHLEBOTOMIST"})
        @DisplayName("Should get deferral reason by ID")
        void getDeferralReasonById_authenticated_returns200() throws Exception {
            when(masterDataService.getDeferralReasonById(deferralReasonId))
                    .thenReturn(sampleDeferralReason);

            mockMvc.perform(get(BASE_URL + "/deferral-reasons/{id}", deferralReasonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(deferralReasonId.toString()))
                    .andExpect(jsonPath("$.data.reasonCode").value("DR-001"));
        }

        @Test
        @WithMockUser(roles = {"NURSE"})
        @DisplayName("Should get deferral reasons by type")
        void getDeferralReasonsByType_authenticated_returns200() throws Exception {
            when(masterDataService.getDeferralReasonsByType("TEMPORARY"))
                    .thenReturn(List.of(sampleDeferralReason));

            mockMvc.perform(get(BASE_URL + "/deferral-reasons/type/{type}", "TEMPORARY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].deferralType").value("TEMPORARY"));

            verify(masterDataService).getDeferralReasonsByType("TEMPORARY");
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated deferral reasons request")
        void getAllDeferralReasons_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/deferral-reasons"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== REACTION TYPES ====================

    @Nested
    @DisplayName("Reaction Types endpoints")
    class ReactionTypeEndpoints {

        private UUID reactionTypeId;
        private ReactionTypeResponse sampleReactionType;

        @BeforeEach
        void setUp() {
            reactionTypeId = UUID.randomUUID();
            sampleReactionType = new ReactionTypeResponse(
                    reactionTypeId, "RT-001", "Febrile Non-Hemolytic",
                    "MILD", "Fever and chills during or after transfusion", true
            );
        }

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get all reaction types as authenticated user")
        void getAllReactionTypes_authenticated_returns200() throws Exception {
            List<ReactionTypeResponse> types = List.of(
                    sampleReactionType,
                    new ReactionTypeResponse(UUID.randomUUID(), "RT-002",
                            "Acute Hemolytic", "SEVERE",
                            "Destruction of transfused red blood cells", true)
            );
            when(masterDataService.getAllReactionTypes()).thenReturn(types);

            mockMvc.perform(get(BASE_URL + "/reaction-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].reactionCode").value("RT-001"))
                    .andExpect(jsonPath("$.data[0].reactionName").value("Febrile Non-Hemolytic"))
                    .andExpect(jsonPath("$.data[0].severity").value("MILD"));

            verify(masterDataService).getAllReactionTypes();
        }

        @Test
        @WithMockUser(roles = {"NURSE"})
        @DisplayName("Should get reaction type by ID")
        void getReactionTypeById_authenticated_returns200() throws Exception {
            when(masterDataService.getReactionTypeById(reactionTypeId))
                    .thenReturn(sampleReactionType);

            mockMvc.perform(get(BASE_URL + "/reaction-types/{id}", reactionTypeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(reactionTypeId.toString()))
                    .andExpect(jsonPath("$.data.reactionCode").value("RT-001"));
        }

        @Test
        @WithMockUser(roles = {"LAB_TECHNICIAN"})
        @DisplayName("Should get reaction types by severity")
        void getReactionTypesBySeverity_authenticated_returns200() throws Exception {
            when(masterDataService.getReactionTypesBySeverity("MILD"))
                    .thenReturn(List.of(sampleReactionType));

            mockMvc.perform(get(BASE_URL + "/reaction-types/severity/{severity}", "MILD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].severity").value("MILD"));

            verify(masterDataService).getReactionTypesBySeverity("MILD");
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated reaction types request")
        void getAllReactionTypes_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/reaction-types"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== ICD CODES ====================

    @Nested
    @DisplayName("ICD Codes endpoints")
    class IcdCodeEndpoints {

        private UUID icdCodeId;
        private IcdCodeResponse sampleIcdCode;

        @BeforeEach
        void setUp() {
            icdCodeId = UUID.randomUUID();
            sampleIcdCode = new IcdCodeResponse(
                    icdCodeId, "D50.0", "Iron deficiency anemia secondary to blood loss",
                    "D50", true
            );
        }

        @Test
        @WithMockUser(roles = {"DOCTOR"})
        @DisplayName("Should get all ICD codes as authenticated user")
        void getAllIcdCodes_authenticated_returns200() throws Exception {
            List<IcdCodeResponse> codes = List.of(
                    sampleIcdCode,
                    new IcdCodeResponse(UUID.randomUUID(), "D59.1",
                            "Autoimmune hemolytic anemia", "D59", true)
            );
            when(masterDataService.getAllIcdCodes()).thenReturn(codes);

            mockMvc.perform(get(BASE_URL + "/icd-codes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].icdCode").value("D50.0"))
                    .andExpect(jsonPath("$.data[0].description").value("Iron deficiency anemia secondary to blood loss"))
                    .andExpect(jsonPath("$.data[0].category").value("D50"));

            verify(masterDataService).getAllIcdCodes();
        }

        @Test
        @WithMockUser(roles = {"SUPER_ADMIN"})
        @DisplayName("Should get ICD code by ID")
        void getIcdCodeById_authenticated_returns200() throws Exception {
            when(masterDataService.getIcdCodeById(icdCodeId)).thenReturn(sampleIcdCode);

            mockMvc.perform(get(BASE_URL + "/icd-codes/{id}", icdCodeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(icdCodeId.toString()))
                    .andExpect(jsonPath("$.data.icdCode").value("D50.0"));

            verify(masterDataService).getIcdCodeById(icdCodeId);
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should get ICD codes by category")
        void getIcdCodesByCategory_authenticated_returns200() throws Exception {
            when(masterDataService.getIcdCodesByCategory("D50"))
                    .thenReturn(List.of(sampleIcdCode));

            mockMvc.perform(get(BASE_URL + "/icd-codes/category/{category}", "D50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].category").value("D50"));

            verify(masterDataService).getIcdCodesByCategory("D50");
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated ICD codes request")
        void getAllIcdCodes_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/icd-codes"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated ICD code by ID request")
        void getIcdCodeById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/icd-codes/{id}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated ICD codes by category request")
        void getIcdCodesByCategory_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/icd-codes/category/{category}", "D50"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== CROSS-CUTTING: ANY ROLE CAN ACCESS ====================

    @Nested
    @DisplayName("Any authenticated role can access master data")
    class AnyRoleAccess {

        @Test
        @WithMockUser(roles = {"BILLING_CLERK"})
        @DisplayName("BILLING_CLERK can access blood groups")
        void billingClerk_canAccessBloodGroups() throws Exception {
            when(masterDataService.getAllBloodGroups()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/blood-groups"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"CAMP_COORDINATOR"})
        @DisplayName("CAMP_COORDINATOR can access component types")
        void campCoordinator_canAccessComponentTypes() throws Exception {
            when(masterDataService.getAllComponentTypes()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/component-types"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("HOSPITAL_USER can access countries")
        void hospitalUser_canAccessCountries() throws Exception {
            when(masterDataService.getAllCountries()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/countries"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"INVENTORY_MANAGER"})
        @DisplayName("INVENTORY_MANAGER can access regions")
        void inventoryManager_canAccessRegions() throws Exception {
            when(masterDataService.getAllRegions()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/regions"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"AUDITOR"})
        @DisplayName("AUDITOR can access deferral reasons")
        void auditor_canAccessDeferralReasons() throws Exception {
            when(masterDataService.getAllDeferralReasons()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/deferral-reasons"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"SYSTEM_ADMIN"})
        @DisplayName("SYSTEM_ADMIN can access reaction types")
        void systemAdmin_canAccessReactionTypes() throws Exception {
            when(masterDataService.getAllReactionTypes()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/reaction-types"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"REGIONAL_ADMIN"})
        @DisplayName("REGIONAL_ADMIN can access ICD codes")
        void regionalAdmin_canAccessIcdCodes() throws Exception {
            when(masterDataService.getAllIcdCodes()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/icd-codes"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("DONOR can access cities")
        void donor_canAccessCities() throws Exception {
            when(masterDataService.getAllCities()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/cities"))
                    .andExpect(status().isOk());
        }
    }
}
