package com.bloodbank.branchservice.service;

import com.bloodbank.branchservice.dto.*;
import com.bloodbank.branchservice.mapper.MasterDataMapper;
import com.bloodbank.branchservice.repository.*;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MasterDataService {

    private static final Logger log = LoggerFactory.getLogger(MasterDataService.class);

    private final BloodGroupRepository bloodGroupRepository;
    private final ComponentTypeRepository componentTypeRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final DeferralReasonRepository deferralReasonRepository;
    private final ReactionTypeRepository reactionTypeRepository;
    private final IcdCodeRepository icdCodeRepository;
    private final MasterDataMapper masterDataMapper;

    public MasterDataService(BloodGroupRepository bloodGroupRepository,
                             ComponentTypeRepository componentTypeRepository,
                             CountryRepository countryRepository,
                             RegionRepository regionRepository,
                             CityRepository cityRepository,
                             DeferralReasonRepository deferralReasonRepository,
                             ReactionTypeRepository reactionTypeRepository,
                             IcdCodeRepository icdCodeRepository,
                             MasterDataMapper masterDataMapper) {
        this.bloodGroupRepository = bloodGroupRepository;
        this.componentTypeRepository = componentTypeRepository;
        this.countryRepository = countryRepository;
        this.regionRepository = regionRepository;
        this.cityRepository = cityRepository;
        this.deferralReasonRepository = deferralReasonRepository;
        this.reactionTypeRepository = reactionTypeRepository;
        this.icdCodeRepository = icdCodeRepository;
        this.masterDataMapper = masterDataMapper;
    }

    // Blood Groups
    @Cacheable(value = "bloodGroups", key = "'all'")
    public List<BloodGroupResponse> getAllBloodGroups() {
        log.debug("Fetching all blood groups");
        return masterDataMapper.toBloodGroupResponseList(bloodGroupRepository.findByIsActiveTrue());
    }

    public BloodGroupResponse getBloodGroupById(UUID id) {
        log.debug("Fetching blood group with id: {}", id);
        return masterDataMapper.toResponse(
                bloodGroupRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("BloodGroup", "id", id)));
    }

    // Component Types
    @Cacheable(value = "componentTypes", key = "'all'")
    public List<ComponentTypeResponse> getAllComponentTypes() {
        log.debug("Fetching all component types");
        return masterDataMapper.toComponentTypeResponseList(componentTypeRepository.findByIsActiveTrue());
    }

    public ComponentTypeResponse getComponentTypeById(UUID id) {
        log.debug("Fetching component type with id: {}", id);
        return masterDataMapper.toResponse(
                componentTypeRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("ComponentType", "id", id)));
    }

    public ComponentTypeResponse getComponentTypeByCode(String code) {
        log.debug("Fetching component type with code: {}", code);
        return masterDataMapper.toResponse(
                componentTypeRepository.findByTypeCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("ComponentType", "code", code)));
    }

    // Countries
    @Cacheable(value = "countries", key = "'all'")
    public List<CountryResponse> getAllCountries() {
        log.debug("Fetching all countries");
        return masterDataMapper.toCountryResponseList(countryRepository.findByIsActiveTrue());
    }

    public CountryResponse getCountryById(UUID id) {
        log.debug("Fetching country with id: {}", id);
        return masterDataMapper.toResponse(
                countryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Country", "id", id)));
    }

    // Regions
    @Cacheable(value = "regions", key = "#countryId")
    public List<RegionResponse> getRegionsByCountry(UUID countryId) {
        log.debug("Fetching regions for country: {}", countryId);
        return masterDataMapper.toRegionResponseList(regionRepository.findByCountryId(countryId));
    }

    @Cacheable(value = "regions", key = "'all'")
    public List<RegionResponse> getAllRegions() {
        log.debug("Fetching all regions");
        return masterDataMapper.toRegionResponseList(regionRepository.findByIsActiveTrue());
    }

    public RegionResponse getRegionById(UUID id) {
        log.debug("Fetching region with id: {}", id);
        return masterDataMapper.toResponse(
                regionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Region", "id", id)));
    }

    // Cities
    @Cacheable(value = "cities", key = "#regionId")
    public List<CityResponse> getCitiesByRegion(UUID regionId) {
        log.debug("Fetching cities for region: {}", regionId);
        return masterDataMapper.toCityResponseList(cityRepository.findByRegionId(regionId));
    }

    @Cacheable(value = "cities", key = "'all'")
    public List<CityResponse> getAllCities() {
        log.debug("Fetching all cities");
        return masterDataMapper.toCityResponseList(cityRepository.findByIsActiveTrue());
    }

    public CityResponse getCityById(UUID id) {
        log.debug("Fetching city with id: {}", id);
        return masterDataMapper.toResponse(
                cityRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("City", "id", id)));
    }

    // Deferral Reasons
    @Cacheable(value = "deferralReasons", key = "'all'")
    public List<DeferralReasonResponse> getAllDeferralReasons() {
        log.debug("Fetching all deferral reasons");
        return masterDataMapper.toDeferralReasonResponseList(deferralReasonRepository.findByIsActiveTrue());
    }

    public DeferralReasonResponse getDeferralReasonById(UUID id) {
        log.debug("Fetching deferral reason with id: {}", id);
        return masterDataMapper.toResponse(
                deferralReasonRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("DeferralReason", "id", id)));
    }

    public List<DeferralReasonResponse> getDeferralReasonsByType(String type) {
        log.debug("Fetching deferral reasons by type: {}", type);
        return masterDataMapper.toDeferralReasonResponseList(deferralReasonRepository.findByDeferralType(type));
    }

    // Reaction Types
    @Cacheable(value = "reactionTypes", key = "'all'")
    public List<ReactionTypeResponse> getAllReactionTypes() {
        log.debug("Fetching all reaction types");
        return masterDataMapper.toReactionTypeResponseList(reactionTypeRepository.findByIsActiveTrue());
    }

    public ReactionTypeResponse getReactionTypeById(UUID id) {
        log.debug("Fetching reaction type with id: {}", id);
        return masterDataMapper.toResponse(
                reactionTypeRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("ReactionType", "id", id)));
    }

    public List<ReactionTypeResponse> getReactionTypesBySeverity(String severity) {
        log.debug("Fetching reaction types by severity: {}", severity);
        return masterDataMapper.toReactionTypeResponseList(reactionTypeRepository.findBySeverity(severity));
    }

    // ICD Codes
    @Cacheable(value = "icdCodes", key = "'all'")
    public List<IcdCodeResponse> getAllIcdCodes() {
        log.debug("Fetching all ICD codes");
        return masterDataMapper.toIcdCodeResponseList(icdCodeRepository.findByIsActiveTrue());
    }

    public IcdCodeResponse getIcdCodeById(UUID id) {
        log.debug("Fetching ICD code with id: {}", id);
        return masterDataMapper.toResponse(
                icdCodeRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("IcdCode", "id", id)));
    }

    public List<IcdCodeResponse> getIcdCodesByCategory(String category) {
        log.debug("Fetching ICD codes by category: {}", category);
        return masterDataMapper.toIcdCodeResponseList(icdCodeRepository.findByCategory(category));
    }
}
