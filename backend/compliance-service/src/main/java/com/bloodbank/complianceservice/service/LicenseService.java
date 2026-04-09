package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.LicenseCreateRequest;
import com.bloodbank.complianceservice.dto.LicenseResponse;
import com.bloodbank.complianceservice.entity.License;
import com.bloodbank.complianceservice.enums.LicenseStatusEnum;
import com.bloodbank.complianceservice.enums.LicenseTypeEnum;
import com.bloodbank.complianceservice.mapper.LicenseMapper;
import com.bloodbank.complianceservice.repository.LicenseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LicenseService {

    private static final Logger log = LoggerFactory.getLogger(LicenseService.class);

    private final LicenseRepository licenseRepository;
    private final LicenseMapper licenseMapper;

    public LicenseService(LicenseRepository licenseRepository, LicenseMapper licenseMapper) {
        this.licenseRepository = licenseRepository;
        this.licenseMapper = licenseMapper;
    }

    @Transactional
    public LicenseResponse create(LicenseCreateRequest request) {
        log.info("Creating license: type={}, number={}", request.licenseType(), request.licenseNumber());
        License license = licenseMapper.toEntity(request);
        license.setBranchId(request.branchId());
        license.setStatus(LicenseStatusEnum.ACTIVE);
        license = licenseRepository.save(license);
        return licenseMapper.toResponse(license);
    }

    public LicenseResponse getById(UUID id) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License", "id", id));
        return licenseMapper.toResponse(license);
    }

    public LicenseResponse getByLicenseNumber(String licenseNumber) {
        License license = licenseRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new ResourceNotFoundException("License", "licenseNumber", licenseNumber));
        return licenseMapper.toResponse(license);
    }

    public List<LicenseResponse> getByStatus(LicenseStatusEnum status) {
        return licenseMapper.toResponseList(licenseRepository.findByStatus(status));
    }

    public List<LicenseResponse> getByType(LicenseTypeEnum type) {
        return licenseMapper.toResponseList(licenseRepository.findByLicenseType(type));
    }

    public List<LicenseResponse> getExpiringSoon(LocalDate beforeDate) {
        return licenseMapper.toResponseList(licenseRepository.findByExpiryDateBefore(beforeDate));
    }

    @Transactional
    public LicenseResponse updateStatus(UUID id, LicenseStatusEnum status) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License", "id", id));

        if (license.getStatus() == LicenseStatusEnum.REVOKED) {
            throw new BusinessException("Cannot update status of a revoked license", "LICENSE_REVOKED");
        }

        license.setStatus(status);
        license = licenseRepository.save(license);
        log.info("Updated license status: id={}, newStatus={}", id, status);
        return licenseMapper.toResponse(license);
    }

    @Transactional
    public LicenseResponse renew(UUID id, LocalDate newExpiryDate) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License", "id", id));

        if (license.getStatus() == LicenseStatusEnum.REVOKED) {
            throw new BusinessException("Cannot renew a revoked license", "LICENSE_REVOKED");
        }

        license.setRenewalDate(LocalDate.now());
        license.setExpiryDate(newExpiryDate);
        license.setStatus(LicenseStatusEnum.ACTIVE);
        license = licenseRepository.save(license);
        log.info("Renewed license: id={}, newExpiryDate={}", id, newExpiryDate);
        return licenseMapper.toResponse(license);
    }
}
