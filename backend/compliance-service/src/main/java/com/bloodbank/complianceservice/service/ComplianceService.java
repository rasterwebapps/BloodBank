package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkCreateRequest;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkResponse;
import com.bloodbank.complianceservice.entity.RegulatoryFramework;
import com.bloodbank.complianceservice.mapper.RegulatoryFrameworkMapper;
import com.bloodbank.complianceservice.repository.RegulatoryFrameworkRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplianceService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);

    private final RegulatoryFrameworkRepository frameworkRepository;
    private final RegulatoryFrameworkMapper frameworkMapper;

    public ComplianceService(RegulatoryFrameworkRepository frameworkRepository,
                             RegulatoryFrameworkMapper frameworkMapper) {
        this.frameworkRepository = frameworkRepository;
        this.frameworkMapper = frameworkMapper;
    }

    @Transactional
    @CacheEvict(value = "regulatoryFrameworks", allEntries = true)
    public RegulatoryFrameworkResponse create(RegulatoryFrameworkCreateRequest request) {
        log.info("Creating regulatory framework: code={}, name={}", request.frameworkCode(), request.frameworkName());
        RegulatoryFramework framework = frameworkMapper.toEntity(request);
        framework = frameworkRepository.save(framework);
        return frameworkMapper.toResponse(framework);
    }

    @Cacheable(value = "regulatoryFrameworks", key = "#id")
    public RegulatoryFrameworkResponse getById(UUID id) {
        RegulatoryFramework framework = frameworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegulatoryFramework", "id", id));
        return frameworkMapper.toResponse(framework);
    }

    @Cacheable(value = "regulatoryFrameworks", key = "'all'")
    public List<RegulatoryFrameworkResponse> getAll() {
        return frameworkMapper.toResponseList(frameworkRepository.findAll());
    }

    public RegulatoryFrameworkResponse getByFrameworkCode(String code) {
        RegulatoryFramework framework = frameworkRepository.findByFrameworkCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("RegulatoryFramework", "frameworkCode", code));
        return frameworkMapper.toResponse(framework);
    }

    @Cacheable(value = "regulatoryFrameworks", key = "'active'")
    public List<RegulatoryFrameworkResponse> getActiveFrameworks() {
        return frameworkMapper.toResponseList(frameworkRepository.findByIsActive(true));
    }

    @Transactional
    @CacheEvict(value = "regulatoryFrameworks", allEntries = true)
    public RegulatoryFrameworkResponse update(UUID id, RegulatoryFrameworkCreateRequest request) {
        RegulatoryFramework framework = frameworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegulatoryFramework", "id", id));

        framework.setFrameworkCode(request.frameworkCode());
        framework.setFrameworkName(request.frameworkName());
        framework.setAuthorityName(request.authorityName());
        framework.setCountryId(request.countryId());
        framework.setDescription(request.description());
        framework.setEffectiveDate(request.effectiveDate());
        framework.setVersionNumber(request.versionNumber());
        framework.setDocumentUrl(request.documentUrl());

        framework = frameworkRepository.save(framework);
        log.info("Updated regulatory framework: id={}", id);
        return frameworkMapper.toResponse(framework);
    }

    @Transactional
    @CacheEvict(value = "regulatoryFrameworks", allEntries = true)
    public RegulatoryFrameworkResponse deactivate(UUID id) {
        RegulatoryFramework framework = frameworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegulatoryFramework", "id", id));

        framework.setActive(false);
        framework = frameworkRepository.save(framework);
        log.info("Deactivated regulatory framework: id={}", id);
        return frameworkMapper.toResponse(framework);
    }
}
