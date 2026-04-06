package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.events.TestResultAvailableEvent;
import com.bloodbank.common.events.UnitReleasedEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.inventoryservice.dto.BloodUnitCreateRequest;
import com.bloodbank.inventoryservice.dto.BloodUnitResponse;
import com.bloodbank.inventoryservice.entity.BloodUnit;
import com.bloodbank.inventoryservice.enums.TtiStatusEnum;
import com.bloodbank.inventoryservice.mapper.BloodUnitMapper;
import com.bloodbank.inventoryservice.repository.BloodUnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BloodUnitService {

    private static final Logger log = LoggerFactory.getLogger(BloodUnitService.class);

    private final BloodUnitRepository bloodUnitRepository;
    private final BloodUnitMapper bloodUnitMapper;

    public BloodUnitService(BloodUnitRepository bloodUnitRepository,
                            BloodUnitMapper bloodUnitMapper) {
        this.bloodUnitRepository = bloodUnitRepository;
        this.bloodUnitMapper = bloodUnitMapper;
    }

    @Transactional
    public BloodUnitResponse createBloodUnit(BloodUnitCreateRequest request) {
        log.info("Creating blood unit for collection: {}", request.collectionId());
        BloodUnit unit = bloodUnitMapper.toEntity(request);
        unit.setUnitNumber(generateUnitNumber());
        unit.setStatus(BloodUnitStatusEnum.QUARANTINED);
        unit.setTtiStatus(TtiStatusEnum.PENDING);
        unit.setBranchId(request.branchId());
        unit = bloodUnitRepository.save(unit);
        return bloodUnitMapper.toResponse(unit);
    }

    public BloodUnitResponse getById(UUID id) {
        BloodUnit unit = bloodUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodUnit", "id", id));
        return bloodUnitMapper.toResponse(unit);
    }

    public BloodUnitResponse getByUnitNumber(String unitNumber) {
        BloodUnit unit = bloodUnitRepository.findByUnitNumber(unitNumber)
                .orElseThrow(() -> new ResourceNotFoundException("BloodUnit", "unitNumber", unitNumber));
        return bloodUnitMapper.toResponse(unit);
    }

    public List<BloodUnitResponse> getByDonor(UUID donorId) {
        return bloodUnitMapper.toResponseList(bloodUnitRepository.findByDonorId(donorId));
    }

    public List<BloodUnitResponse> getByStatus(BloodUnitStatusEnum status) {
        return bloodUnitMapper.toResponseList(bloodUnitRepository.findByStatus(status));
    }

    @Transactional
    public BloodUnitResponse updateStatus(UUID id, BloodUnitStatusEnum status) {
        log.info("Updating blood unit {} status to {}", id, status);
        BloodUnit unit = bloodUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodUnit", "id", id));
        unit.setStatus(status);
        unit = bloodUnitRepository.save(unit);
        return bloodUnitMapper.toResponse(unit);
    }

    @Transactional
    public void createFromDonation(DonationCompletedEvent event) {
        log.info("Creating blood unit from donation: {}", event.donationId());
        BloodUnit unit = new BloodUnit(
                event.donationId(),
                event.donorId(),
                generateUnitNumber(),
                null,
                null,
                450,
                event.occurredAt(),
                event.occurredAt().plus(42, ChronoUnit.DAYS)
        );
        unit.setBranchId(event.branchId());
        bloodUnitRepository.save(unit);
    }

    @Transactional
    public void updateTtiStatus(TestResultAvailableEvent event) {
        log.info("Updating TTI status for blood unit: {}", event.bloodUnitId());
        bloodUnitRepository.findById(event.bloodUnitId()).ifPresent(unit -> {
            // Mark as negative (safe) by default from event; actual logic would check test results
            unit.setTtiStatus(TtiStatusEnum.NEGATIVE);
            bloodUnitRepository.save(unit);
        });
    }

    @Transactional
    public void markAsAvailable(UnitReleasedEvent event) {
        log.info("Marking blood unit as available: {}", event.bloodUnitId());
        bloodUnitRepository.findById(event.bloodUnitId()).ifPresent(unit -> {
            unit.setStatus(BloodUnitStatusEnum.AVAILABLE);
            unit.setTtiStatus(TtiStatusEnum.NEGATIVE);
            bloodUnitRepository.save(unit);
        });
    }

    private String generateUnitNumber() {
        return "BU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
