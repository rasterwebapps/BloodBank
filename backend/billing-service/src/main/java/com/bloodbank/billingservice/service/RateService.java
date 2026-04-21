package com.bloodbank.billingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.billingservice.dto.RateCreateRequest;
import com.bloodbank.billingservice.dto.RateResponse;
import com.bloodbank.billingservice.entity.RateMaster;
import com.bloodbank.billingservice.mapper.RateMapper;
import com.bloodbank.billingservice.repository.RateRepository;

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
public class RateService {

    private static final Logger log = LoggerFactory.getLogger(RateService.class);

    private final RateRepository rateRepository;
    private final RateMapper rateMapper;

    public RateService(RateRepository rateRepository, RateMapper rateMapper) {
        this.rateRepository = rateRepository;
        this.rateMapper = rateMapper;
    }

    @Transactional
    @CacheEvict(value = "rateMaster", allEntries = true)
    public RateResponse createRate(RateCreateRequest request) {
        log.info("Creating rate: serviceCode={}, serviceName={}", request.serviceCode(), request.serviceName());
        RateMaster rate = rateMapper.toEntity(request);
        rate.setBranchId(request.branchId());
        if (request.currency() != null) {
            rate.setCurrency(request.currency());
        }
        rate = rateRepository.save(rate);
        return rateMapper.toResponse(rate);
    }

    @Cacheable(value = "rateMaster", key = "#id")
    public RateResponse getRateById(UUID id) {
        RateMaster rate = rateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate", "id", id));
        return rateMapper.toResponse(rate);
    }

    @Cacheable(value = "rateMaster", key = "#branchId")
    public List<RateResponse> getActiveRatesByBranch(UUID branchId) {
        return rateMapper.toResponseList(rateRepository.findByBranchIdAndActiveTrue(branchId));
    }

    @Transactional
    @CacheEvict(value = "rateMaster", key = "#id")
    public RateResponse updateRate(UUID id, RateCreateRequest request) {
        RateMaster rate = rateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate", "id", id));
        rate.setServiceCode(request.serviceCode());
        rate.setServiceName(request.serviceName());
        rate.setRateAmount(request.rateAmount());
        rate.setComponentTypeId(request.componentTypeId());
        rate.setTaxPercentage(request.taxPercentage());
        rate.setEffectiveFrom(request.effectiveFrom());
        rate.setEffectiveTo(request.effectiveTo());
        if (request.currency() != null) {
            rate.setCurrency(request.currency());
        }
        rate = rateRepository.save(rate);
        log.info("Updated rate: id={}", id);
        return rateMapper.toResponse(rate);
    }

    @Transactional
    @CacheEvict(value = "rateMaster", key = "#id")
    public void deactivateRate(UUID id) {
        RateMaster rate = rateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate", "id", id));
        rate.setActive(false);
        rateRepository.save(rate);
        log.info("Deactivated rate: id={}", id);
    }
}
