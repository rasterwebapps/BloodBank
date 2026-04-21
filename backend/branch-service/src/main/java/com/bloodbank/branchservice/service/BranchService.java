package com.bloodbank.branchservice.service;

import com.bloodbank.branchservice.dto.*;
import com.bloodbank.branchservice.entity.*;
import com.bloodbank.branchservice.mapper.BranchMapper;
import com.bloodbank.branchservice.repository.*;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BranchService {

    private static final Logger log = LoggerFactory.getLogger(BranchService.class);

    private final BranchRepository branchRepository;
    private final BranchOperatingHoursRepository operatingHoursRepository;
    private final BranchEquipmentRepository equipmentRepository;
    private final BranchRegionRepository branchRegionRepository;
    private final CityRepository cityRepository;
    private final RegionRepository regionRepository;
    private final BranchMapper branchMapper;

    public BranchService(BranchRepository branchRepository,
                         BranchOperatingHoursRepository operatingHoursRepository,
                         BranchEquipmentRepository equipmentRepository,
                         BranchRegionRepository branchRegionRepository,
                         CityRepository cityRepository,
                         RegionRepository regionRepository,
                         BranchMapper branchMapper) {
        this.branchRepository = branchRepository;
        this.operatingHoursRepository = operatingHoursRepository;
        this.equipmentRepository = equipmentRepository;
        this.branchRegionRepository = branchRegionRepository;
        this.cityRepository = cityRepository;
        this.regionRepository = regionRepository;
        this.branchMapper = branchMapper;
    }

    @Transactional
    public BranchResponse createBranch(BranchCreateRequest request) {
        log.info("Creating branch with code: {}", request.branchCode());

        if (branchRepository.existsByBranchCode(request.branchCode())) {
            throw new ConflictException("Branch with code '" + request.branchCode() + "' already exists");
        }

        if (request.email() != null && branchRepository.existsByEmail(request.email())) {
            throw new ConflictException("Branch with email '" + request.email() + "' already exists");
        }

        Branch branch = branchMapper.toEntity(request);

        if (request.cityId() != null) {
            City city = cityRepository.findById(request.cityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.cityId()));
            branch.setCity(city);
        }

        if (request.parentBranchId() != null) {
            Branch parentBranch = branchRepository.findById(request.parentBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.parentBranchId()));
            branch.setParentBranch(parentBranch);
        }

        branch = branchRepository.save(branch);
        log.info("Branch created successfully with id: {}", branch.getId());
        return branchMapper.toResponse(branch);
    }

    @Transactional
    @CacheEvict(value = "branches", key = "#id")
    public BranchResponse updateBranch(UUID id, BranchUpdateRequest request) {
        log.info("Updating branch with id: {}", id);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        if (request.branchName() != null) {
            branch.setBranchName(request.branchName());
        }
        if (request.branchType() != null) {
            branch.setBranchType(request.branchType());
        }
        if (request.addressLine1() != null) {
            branch.setAddressLine1(request.addressLine1());
        }
        branch.setAddressLine2(request.addressLine2());

        if (request.cityId() != null) {
            City city = cityRepository.findById(request.cityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.cityId()));
            branch.setCity(city);
        }

        branch.setPostalCode(request.postalCode());
        branch.setPhone(request.phone());

        if (request.email() != null) {
            branch.setEmail(request.email());
        }

        branch.setLicenseNumber(request.licenseNumber());
        branch.setLicenseExpiry(request.licenseExpiry());
        branch.setLatitude(request.latitude());
        branch.setLongitude(request.longitude());

        if (request.parentBranchId() != null) {
            Branch parentBranch = branchRepository.findById(request.parentBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.parentBranchId()));
            branch.setParentBranch(parentBranch);
        } else {
            branch.setParentBranch(null);
        }

        branch = branchRepository.save(branch);
        log.info("Branch updated successfully with id: {}", id);
        return branchMapper.toResponse(branch);
    }

    @Cacheable(value = "branches", key = "#id")
    public BranchResponse getBranchById(UUID id) {
        log.debug("Fetching branch with id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return branchMapper.toResponse(branch);
    }

    @Cacheable(value = "branches", key = "#code")
    public BranchResponse getBranchByCode(String code) {
        log.debug("Fetching branch with code: {}", code);
        Branch branch = branchRepository.findByBranchCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "code", code));
        return branchMapper.toResponse(branch);
    }

    public PagedResponse<BranchResponse> getAllBranches(Pageable pageable) {
        log.debug("Fetching all branches, page: {}", pageable.getPageNumber());
        Page<Branch> page = branchRepository.findAll(pageable);
        List<BranchResponse> content = branchMapper.toResponseList(page.getContent());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public PagedResponse<BranchResponse> searchBranches(String name, Pageable pageable) {
        log.debug("Searching branches with name: {}", name);
        Page<Branch> page = branchRepository.findByBranchNameContainingIgnoreCase(name, pageable);
        List<BranchResponse> content = branchMapper.toResponseList(page.getContent());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public List<BranchResponse> getBranchesByStatus(String status) {
        log.debug("Fetching branches with status: {}", status);
        return branchMapper.toResponseList(branchRepository.findByStatus(status));
    }

    public List<BranchResponse> getBranchesByType(String branchType) {
        log.debug("Fetching branches with type: {}", branchType);
        return branchMapper.toResponseList(branchRepository.findByBranchType(branchType));
    }

    @Transactional
    @CacheEvict(value = "branches", key = "#id")
    public BranchResponse activateBranch(UUID id) {
        log.info("Activating branch with id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        branch.setStatus("ACTIVE");
        branch = branchRepository.save(branch);
        return branchMapper.toResponse(branch);
    }

    @Transactional
    @CacheEvict(value = "branches", key = "#id")
    public BranchResponse deactivateBranch(UUID id) {
        log.info("Deactivating branch with id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        branch.setStatus("INACTIVE");
        branch = branchRepository.save(branch);
        return branchMapper.toResponse(branch);
    }

    // Operating Hours
    @Transactional
    public BranchOperatingHoursResponse addOperatingHours(UUID branchId, BranchOperatingHoursRequest request) {
        log.info("Adding operating hours for branch: {}, day: {}", branchId, request.dayOfWeek());
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        BranchOperatingHours hours = branchMapper.toEntity(request);
        hours.setBranch(branch);
        hours = operatingHoursRepository.save(hours);
        return branchMapper.toResponse(hours);
    }

    public List<BranchOperatingHoursResponse> getOperatingHours(UUID branchId) {
        log.debug("Fetching operating hours for branch: {}", branchId);
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch", "id", branchId);
        }
        return branchMapper.toOperatingHoursResponseList(operatingHoursRepository.findByBranchId(branchId));
    }

    // Equipment
    @Transactional
    public BranchEquipmentResponse addEquipment(UUID branchId, BranchEquipmentRequest request) {
        log.info("Adding equipment for branch: {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        BranchEquipment equipment = branchMapper.toEntity(request);
        equipment.setBranch(branch);
        if (equipment.getStatus() == null) {
            equipment.setStatus("OPERATIONAL");
        }
        equipment = equipmentRepository.save(equipment);
        return branchMapper.toResponse(equipment);
    }

    public List<BranchEquipmentResponse> getEquipment(UUID branchId) {
        log.debug("Fetching equipment for branch: {}", branchId);
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch", "id", branchId);
        }
        return branchMapper.toEquipmentResponseList(equipmentRepository.findByBranchId(branchId));
    }

    // Branch Regions
    @Transactional
    public BranchRegionResponse addBranchRegion(UUID branchId, BranchRegionRequest request) {
        log.info("Adding region {} to branch: {}", request.regionId(), branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        if (branchRegionRepository.existsByBranchIdAndRegionId(branchId, request.regionId())) {
            throw new ConflictException("Branch already has region '" + request.regionId() + "' assigned");
        }

        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region", "id", request.regionId()));

        BranchRegion branchRegion = new BranchRegion(branch, region);
        branchRegion.setPrimary(request.isPrimary());
        branchRegion = branchRegionRepository.save(branchRegion);
        return branchMapper.toResponse(branchRegion);
    }

    public List<BranchRegionResponse> getBranchRegions(UUID branchId) {
        log.debug("Fetching regions for branch: {}", branchId);
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch", "id", branchId);
        }
        return branchMapper.toBranchRegionResponseList(branchRegionRepository.findByBranchId(branchId));
    }

    @Transactional
    public void removeBranchRegion(UUID branchId, UUID regionId) {
        log.info("Removing region {} from branch: {}", regionId, branchId);
        BranchRegion branchRegion = branchRegionRepository.findByBranchIdAndRegionId(branchId, regionId)
                .orElseThrow(() -> new ResourceNotFoundException("BranchRegion", "branchId/regionId", branchId + "/" + regionId));
        branchRegionRepository.delete(branchRegion);
    }
}
