package com.bloodbank.hospitalservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.hospitalservice.dto.HospitalCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalResponse;
import com.bloodbank.hospitalservice.entity.Hospital;
import com.bloodbank.hospitalservice.enums.HospitalStatusEnum;
import com.bloodbank.hospitalservice.mapper.HospitalMapper;
import com.bloodbank.hospitalservice.repository.HospitalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HospitalService {

    private static final Logger log = LoggerFactory.getLogger(HospitalService.class);

    private final HospitalRepository hospitalRepository;
    private final HospitalMapper hospitalMapper;

    public HospitalService(HospitalRepository hospitalRepository,
                           HospitalMapper hospitalMapper) {
        this.hospitalRepository = hospitalRepository;
        this.hospitalMapper = hospitalMapper;
    }

    @Transactional
    public HospitalResponse createHospital(HospitalCreateRequest request) {
        log.info("Creating hospital: {}", request.hospitalName());

        Hospital hospital = hospitalMapper.toEntity(request);
        hospital.setHospitalCode("HSP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        hospital.setStatus(HospitalStatusEnum.ACTIVE);
        hospital.setBranchId(request.branchId());

        hospital = hospitalRepository.save(hospital);
        log.info("Hospital created with code: {}", hospital.getHospitalCode());
        return hospitalMapper.toResponse(hospital);
    }

    @Transactional
    @CacheEvict(value = "hospitals", allEntries = true)
    public HospitalResponse updateHospital(UUID id, HospitalCreateRequest request) {
        log.info("Updating hospital: {}", id);

        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", id));

        hospital.setHospitalName(request.hospitalName());
        hospital.setHospitalType(request.hospitalType());
        hospital.setAddressLine1(request.addressLine1());
        hospital.setAddressLine2(request.addressLine2());
        hospital.setCityId(request.cityId());
        hospital.setPostalCode(request.postalCode());
        hospital.setPhone(request.phone());
        hospital.setEmail(request.email());
        hospital.setContactPerson(request.contactPerson());
        hospital.setLicenseNumber(request.licenseNumber());
        hospital.setBedCount(request.bedCount());
        hospital.setHasBloodBank(request.hasBloodBank());

        hospital = hospitalRepository.save(hospital);
        return hospitalMapper.toResponse(hospital);
    }

    @Cacheable(value = "hospitals", key = "#id")
    public HospitalResponse getHospitalById(UUID id) {
        log.debug("Fetching hospital by id: {}", id);
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", id));
        return hospitalMapper.toResponse(hospital);
    }

    @Cacheable(value = "hospitals", key = "#hospitalCode")
    public HospitalResponse getHospitalByCode(String hospitalCode) {
        log.debug("Fetching hospital by code: {}", hospitalCode);
        Hospital hospital = hospitalRepository.findByHospitalCode(hospitalCode)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "hospitalCode", hospitalCode));
        return hospitalMapper.toResponse(hospital);
    }

    public PagedResponse<HospitalResponse> getAllHospitals(Pageable pageable) {
        log.debug("Fetching all hospitals");
        Page<Hospital> page = hospitalRepository.findAll(pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<HospitalResponse> searchHospitals(String name, Pageable pageable) {
        log.debug("Searching hospitals with name: {}", name);
        Page<Hospital> page = hospitalRepository.findByHospitalNameContainingIgnoreCase(name, pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<HospitalResponse> getHospitalsByStatus(HospitalStatusEnum status, Pageable pageable) {
        log.debug("Fetching hospitals by status: {}", status);
        Page<Hospital> page = hospitalRepository.findByStatus(status, pageable);
        return toPagedResponse(page);
    }

    @Transactional
    @CacheEvict(value = "hospitals", allEntries = true)
    public HospitalResponse updateHospitalStatus(UUID id, HospitalStatusEnum status) {
        log.info("Updating hospital {} status to: {}", id, status);
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", id));
        hospital.setStatus(status);
        hospital = hospitalRepository.save(hospital);
        return hospitalMapper.toResponse(hospital);
    }

    private PagedResponse<HospitalResponse> toPagedResponse(Page<Hospital> page) {
        List<HospitalResponse> content = hospitalMapper.toResponseList(page.getContent());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
