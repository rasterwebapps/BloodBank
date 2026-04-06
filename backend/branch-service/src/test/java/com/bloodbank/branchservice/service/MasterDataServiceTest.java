package com.bloodbank.branchservice.service;

import com.bloodbank.branchservice.dto.*;
import com.bloodbank.branchservice.entity.*;
import com.bloodbank.branchservice.mapper.MasterDataMapper;
import com.bloodbank.branchservice.repository.*;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterDataServiceTest {

    @Mock
    private BloodGroupRepository bloodGroupRepository;

    @Mock
    private ComponentTypeRepository componentTypeRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private DeferralReasonRepository deferralReasonRepository;

    @Mock
    private ReactionTypeRepository reactionTypeRepository;

    @Mock
    private IcdCodeRepository icdCodeRepository;

    @Mock
    private MasterDataMapper masterDataMapper;

    @InjectMocks
    private MasterDataService masterDataService;

    @Nested
    @DisplayName("Blood Groups")
    class BloodGroupTests {

        private BloodGroup bloodGroup;
        private BloodGroupResponse bloodGroupResponse;
        private UUID bloodGroupId;

        @BeforeEach
        void setUp() {
            bloodGroupId = UUID.randomUUID();
            bloodGroup = new BloodGroup("O+", "O Positive");
            bloodGroup.setId(bloodGroupId);

            bloodGroupResponse = new BloodGroupResponse(bloodGroupId, "O+", "O Positive", true);
        }

        @Test
        @DisplayName("should return all active blood groups")
        void shouldReturnAllActiveBloodGroups() {
            when(bloodGroupRepository.findByIsActiveTrue()).thenReturn(List.of(bloodGroup));
            when(masterDataMapper.toBloodGroupResponseList(List.of(bloodGroup)))
                    .thenReturn(List.of(bloodGroupResponse));

            List<BloodGroupResponse> result = masterDataService.getAllBloodGroups();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).groupName()).isEqualTo("O+");
            verify(bloodGroupRepository).findByIsActiveTrue();
        }

        @Test
        @DisplayName("should return blood group by id")
        void shouldReturnBloodGroupById() {
            when(bloodGroupRepository.findById(bloodGroupId)).thenReturn(Optional.of(bloodGroup));
            when(masterDataMapper.toResponse(bloodGroup)).thenReturn(bloodGroupResponse);

            BloodGroupResponse result = masterDataService.getBloodGroupById(bloodGroupId);

            assertThat(result).isNotNull();
            assertThat(result.groupName()).isEqualTo("O+");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when blood group not found")
        void shouldThrowResourceNotFoundExceptionWhenBloodGroupNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(bloodGroupRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getBloodGroupById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("BloodGroup");
        }
    }

    @Nested
    @DisplayName("Component Types")
    class ComponentTypeTests {

        private ComponentType componentType;
        private ComponentTypeResponse componentTypeResponse;
        private UUID componentTypeId;

        @BeforeEach
        void setUp() {
            componentTypeId = UUID.randomUUID();
            componentType = new ComponentType("WB", "Whole Blood", 35);
            componentType.setId(componentTypeId);

            componentTypeResponse = new ComponentTypeResponse(
                    componentTypeId, "WB", "Whole Blood", "Whole blood unit",
                    35, new BigDecimal("2.0"), new BigDecimal("6.0"), true
            );
        }

        @Test
        @DisplayName("should return all active component types")
        void shouldReturnAllActiveComponentTypes() {
            when(componentTypeRepository.findByIsActiveTrue()).thenReturn(List.of(componentType));
            when(masterDataMapper.toComponentTypeResponseList(List.of(componentType)))
                    .thenReturn(List.of(componentTypeResponse));

            List<ComponentTypeResponse> result = masterDataService.getAllComponentTypes();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).typeCode()).isEqualTo("WB");
        }

        @Test
        @DisplayName("should return component type by id")
        void shouldReturnComponentTypeById() {
            when(componentTypeRepository.findById(componentTypeId)).thenReturn(Optional.of(componentType));
            when(masterDataMapper.toResponse(componentType)).thenReturn(componentTypeResponse);

            ComponentTypeResponse result = masterDataService.getComponentTypeById(componentTypeId);

            assertThat(result).isNotNull();
            assertThat(result.typeName()).isEqualTo("Whole Blood");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when component type not found by id")
        void shouldThrowResourceNotFoundExceptionWhenComponentTypeNotFoundById() {
            UUID unknownId = UUID.randomUUID();
            when(componentTypeRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getComponentTypeById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ComponentType");
        }

        @Test
        @DisplayName("should return component type by code")
        void shouldReturnComponentTypeByCode() {
            when(componentTypeRepository.findByTypeCode("WB")).thenReturn(Optional.of(componentType));
            when(masterDataMapper.toResponse(componentType)).thenReturn(componentTypeResponse);

            ComponentTypeResponse result = masterDataService.getComponentTypeByCode("WB");

            assertThat(result).isNotNull();
            assertThat(result.typeCode()).isEqualTo("WB");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when component type not found by code")
        void shouldThrowResourceNotFoundExceptionWhenComponentTypeNotFoundByCode() {
            when(componentTypeRepository.findByTypeCode("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getComponentTypeByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ComponentType");
        }
    }

    @Nested
    @DisplayName("Countries")
    class CountryTests {

        private Country country;
        private CountryResponse countryResponse;
        private UUID countryId;

        @BeforeEach
        void setUp() {
            countryId = UUID.randomUUID();
            country = new Country("US", "United States");
            country.setId(countryId);

            countryResponse = new CountryResponse(countryId, "US", "United States", "+1", true);
        }

        @Test
        @DisplayName("should return all active countries")
        void shouldReturnAllActiveCountries() {
            when(countryRepository.findByIsActiveTrue()).thenReturn(List.of(country));
            when(masterDataMapper.toCountryResponseList(List.of(country)))
                    .thenReturn(List.of(countryResponse));

            List<CountryResponse> result = masterDataService.getAllCountries();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).countryName()).isEqualTo("United States");
        }

        @Test
        @DisplayName("should return country by id")
        void shouldReturnCountryById() {
            when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));
            when(masterDataMapper.toResponse(country)).thenReturn(countryResponse);

            CountryResponse result = masterDataService.getCountryById(countryId);

            assertThat(result).isNotNull();
            assertThat(result.countryCode()).isEqualTo("US");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when country not found")
        void shouldThrowResourceNotFoundExceptionWhenCountryNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(countryRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getCountryById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Country");
        }
    }

    @Nested
    @DisplayName("Regions")
    class RegionTests {

        private Country country;
        private Region region;
        private RegionResponse regionResponse;
        private UUID countryId;
        private UUID regionId;

        @BeforeEach
        void setUp() {
            countryId = UUID.randomUUID();
            regionId = UUID.randomUUID();

            country = new Country("US", "United States");
            country.setId(countryId);

            region = new Region(country, "CA", "California");
            region.setId(regionId);

            regionResponse = new RegionResponse(regionId, countryId, "United States", "CA", "California", true);
        }

        @Test
        @DisplayName("should return regions by country")
        void shouldReturnRegionsByCountry() {
            when(regionRepository.findByCountryId(countryId)).thenReturn(List.of(region));
            when(masterDataMapper.toRegionResponseList(List.of(region)))
                    .thenReturn(List.of(regionResponse));

            List<RegionResponse> result = masterDataService.getRegionsByCountry(countryId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).regionName()).isEqualTo("California");
        }

        @Test
        @DisplayName("should return all active regions")
        void shouldReturnAllActiveRegions() {
            when(regionRepository.findByIsActiveTrue()).thenReturn(List.of(region));
            when(masterDataMapper.toRegionResponseList(List.of(region)))
                    .thenReturn(List.of(regionResponse));

            List<RegionResponse> result = masterDataService.getAllRegions();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return region by id")
        void shouldReturnRegionById() {
            when(regionRepository.findById(regionId)).thenReturn(Optional.of(region));
            when(masterDataMapper.toResponse(region)).thenReturn(regionResponse);

            RegionResponse result = masterDataService.getRegionById(regionId);

            assertThat(result).isNotNull();
            assertThat(result.regionCode()).isEqualTo("CA");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when region not found")
        void shouldThrowResourceNotFoundExceptionWhenRegionNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(regionRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getRegionById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Region");
        }
    }

    @Nested
    @DisplayName("Cities")
    class CityTests {

        private Country country;
        private Region region;
        private City city;
        private CityResponse cityResponse;
        private UUID regionId;
        private UUID cityId;

        @BeforeEach
        void setUp() {
            regionId = UUID.randomUUID();
            cityId = UUID.randomUUID();

            country = new Country("US", "United States");
            country.setId(UUID.randomUUID());

            region = new Region(country, "CA", "California");
            region.setId(regionId);

            city = new City(region, "Los Angeles");
            city.setId(cityId);

            cityResponse = new CityResponse(cityId, regionId, "California", "Los Angeles", "90001", true);
        }

        @Test
        @DisplayName("should return cities by region")
        void shouldReturnCitiesByRegion() {
            when(cityRepository.findByRegionId(regionId)).thenReturn(List.of(city));
            when(masterDataMapper.toCityResponseList(List.of(city)))
                    .thenReturn(List.of(cityResponse));

            List<CityResponse> result = masterDataService.getCitiesByRegion(regionId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).cityName()).isEqualTo("Los Angeles");
        }

        @Test
        @DisplayName("should return all active cities")
        void shouldReturnAllActiveCities() {
            when(cityRepository.findByIsActiveTrue()).thenReturn(List.of(city));
            when(masterDataMapper.toCityResponseList(List.of(city)))
                    .thenReturn(List.of(cityResponse));

            List<CityResponse> result = masterDataService.getAllCities();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return city by id")
        void shouldReturnCityById() {
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
            when(masterDataMapper.toResponse(city)).thenReturn(cityResponse);

            CityResponse result = masterDataService.getCityById(cityId);

            assertThat(result).isNotNull();
            assertThat(result.cityName()).isEqualTo("Los Angeles");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when city not found")
        void shouldThrowResourceNotFoundExceptionWhenCityNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(cityRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getCityById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("City");
        }
    }

    @Nested
    @DisplayName("Deferral Reasons")
    class DeferralReasonTests {

        private DeferralReason deferralReason;
        private DeferralReasonResponse deferralReasonResponse;
        private UUID deferralReasonId;

        @BeforeEach
        void setUp() {
            deferralReasonId = UUID.randomUUID();
            deferralReason = new DeferralReason("DR001", "Low hemoglobin", "TEMPORARY");
            deferralReason.setId(deferralReasonId);

            deferralReasonResponse = new DeferralReasonResponse(
                    deferralReasonId, "DR001", "Low hemoglobin", "TEMPORARY", 30, true
            );
        }

        @Test
        @DisplayName("should return all active deferral reasons")
        void shouldReturnAllActiveDeferralReasons() {
            when(deferralReasonRepository.findByIsActiveTrue()).thenReturn(List.of(deferralReason));
            when(masterDataMapper.toDeferralReasonResponseList(List.of(deferralReason)))
                    .thenReturn(List.of(deferralReasonResponse));

            List<DeferralReasonResponse> result = masterDataService.getAllDeferralReasons();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).reasonCode()).isEqualTo("DR001");
        }

        @Test
        @DisplayName("should return deferral reason by id")
        void shouldReturnDeferralReasonById() {
            when(deferralReasonRepository.findById(deferralReasonId)).thenReturn(Optional.of(deferralReason));
            when(masterDataMapper.toResponse(deferralReason)).thenReturn(deferralReasonResponse);

            DeferralReasonResponse result = masterDataService.getDeferralReasonById(deferralReasonId);

            assertThat(result).isNotNull();
            assertThat(result.deferralType()).isEqualTo("TEMPORARY");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when deferral reason not found")
        void shouldThrowResourceNotFoundExceptionWhenDeferralReasonNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(deferralReasonRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getDeferralReasonById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DeferralReason");
        }

        @Test
        @DisplayName("should return deferral reasons by type")
        void shouldReturnDeferralReasonsByType() {
            when(deferralReasonRepository.findByDeferralType("TEMPORARY")).thenReturn(List.of(deferralReason));
            when(masterDataMapper.toDeferralReasonResponseList(List.of(deferralReason)))
                    .thenReturn(List.of(deferralReasonResponse));

            List<DeferralReasonResponse> result = masterDataService.getDeferralReasonsByType("TEMPORARY");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).deferralType()).isEqualTo("TEMPORARY");
        }
    }

    @Nested
    @DisplayName("Reaction Types")
    class ReactionTypeTests {

        private ReactionType reactionType;
        private ReactionTypeResponse reactionTypeResponse;
        private UUID reactionTypeId;

        @BeforeEach
        void setUp() {
            reactionTypeId = UUID.randomUUID();
            reactionType = new ReactionType("RT001", "Febrile", "MILD");
            reactionType.setId(reactionTypeId);

            reactionTypeResponse = new ReactionTypeResponse(
                    reactionTypeId, "RT001", "Febrile", "MILD",
                    "Febrile non-hemolytic reaction", true
            );
        }

        @Test
        @DisplayName("should return all active reaction types")
        void shouldReturnAllActiveReactionTypes() {
            when(reactionTypeRepository.findByIsActiveTrue()).thenReturn(List.of(reactionType));
            when(masterDataMapper.toReactionTypeResponseList(List.of(reactionType)))
                    .thenReturn(List.of(reactionTypeResponse));

            List<ReactionTypeResponse> result = masterDataService.getAllReactionTypes();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).reactionCode()).isEqualTo("RT001");
        }

        @Test
        @DisplayName("should return reaction type by id")
        void shouldReturnReactionTypeById() {
            when(reactionTypeRepository.findById(reactionTypeId)).thenReturn(Optional.of(reactionType));
            when(masterDataMapper.toResponse(reactionType)).thenReturn(reactionTypeResponse);

            ReactionTypeResponse result = masterDataService.getReactionTypeById(reactionTypeId);

            assertThat(result).isNotNull();
            assertThat(result.severity()).isEqualTo("MILD");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when reaction type not found")
        void shouldThrowResourceNotFoundExceptionWhenReactionTypeNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(reactionTypeRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getReactionTypeById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ReactionType");
        }

        @Test
        @DisplayName("should return reaction types by severity")
        void shouldReturnReactionTypesBySeverity() {
            when(reactionTypeRepository.findBySeverity("MILD")).thenReturn(List.of(reactionType));
            when(masterDataMapper.toReactionTypeResponseList(List.of(reactionType)))
                    .thenReturn(List.of(reactionTypeResponse));

            List<ReactionTypeResponse> result = masterDataService.getReactionTypesBySeverity("MILD");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).severity()).isEqualTo("MILD");
        }
    }

    @Nested
    @DisplayName("ICD Codes")
    class IcdCodeTests {

        private IcdCode icdCode;
        private IcdCodeResponse icdCodeResponse;
        private UUID icdCodeId;

        @BeforeEach
        void setUp() {
            icdCodeId = UUID.randomUUID();
            icdCode = new IcdCode("D50.0", "Iron deficiency anemia secondary to blood loss");
            icdCode.setId(icdCodeId);

            icdCodeResponse = new IcdCodeResponse(
                    icdCodeId, "D50.0", "Iron deficiency anemia secondary to blood loss",
                    "HEMATOLOGY", true
            );
        }

        @Test
        @DisplayName("should return all active ICD codes")
        void shouldReturnAllActiveIcdCodes() {
            when(icdCodeRepository.findByIsActiveTrue()).thenReturn(List.of(icdCode));
            when(masterDataMapper.toIcdCodeResponseList(List.of(icdCode)))
                    .thenReturn(List.of(icdCodeResponse));

            List<IcdCodeResponse> result = masterDataService.getAllIcdCodes();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).icdCode()).isEqualTo("D50.0");
        }

        @Test
        @DisplayName("should return ICD code by id")
        void shouldReturnIcdCodeById() {
            when(icdCodeRepository.findById(icdCodeId)).thenReturn(Optional.of(icdCode));
            when(masterDataMapper.toResponse(icdCode)).thenReturn(icdCodeResponse);

            IcdCodeResponse result = masterDataService.getIcdCodeById(icdCodeId);

            assertThat(result).isNotNull();
            assertThat(result.icdCode()).isEqualTo("D50.0");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when ICD code not found")
        void shouldThrowResourceNotFoundExceptionWhenIcdCodeNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(icdCodeRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> masterDataService.getIcdCodeById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("IcdCode");
        }

        @Test
        @DisplayName("should return ICD codes by category")
        void shouldReturnIcdCodesByCategory() {
            when(icdCodeRepository.findByCategory("HEMATOLOGY")).thenReturn(List.of(icdCode));
            when(masterDataMapper.toIcdCodeResponseList(List.of(icdCode)))
                    .thenReturn(List.of(icdCodeResponse));

            List<IcdCodeResponse> result = masterDataService.getIcdCodesByCategory("HEMATOLOGY");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).category()).isEqualTo("HEMATOLOGY");
        }
    }
}
