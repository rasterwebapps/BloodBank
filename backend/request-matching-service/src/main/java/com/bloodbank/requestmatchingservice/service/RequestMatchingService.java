package com.bloodbank.requestmatchingservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.BloodRequestMatchedEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestResponse;
import com.bloodbank.requestmatchingservice.entity.EmergencyRequest;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;
import com.bloodbank.requestmatchingservice.event.RequestMatchingEventPublisher;
import com.bloodbank.requestmatchingservice.mapper.EmergencyRequestMapper;
import com.bloodbank.requestmatchingservice.repository.EmergencyRequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for matching blood requests to available inventory
 * using ABO/Rh compatibility and FEFO (First Expiry, First Out) selection.
 */
@Service
@Transactional(readOnly = true)
public class RequestMatchingService {

    private static final Logger log = LoggerFactory.getLogger(RequestMatchingService.class);

    private final EmergencyRequestRepository emergencyRequestRepository;
    private final EmergencyRequestMapper emergencyRequestMapper;
    private final RequestMatchingEventPublisher eventPublisher;

    public RequestMatchingService(EmergencyRequestRepository emergencyRequestRepository,
                                  EmergencyRequestMapper emergencyRequestMapper,
                                  RequestMatchingEventPublisher eventPublisher) {
        this.emergencyRequestRepository = emergencyRequestRepository;
        this.emergencyRequestMapper = emergencyRequestMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Attempts to match a blood request to available inventory.
     * Uses ABO/Rh compatibility rules and FEFO selection strategy.
     *
     * @param requestId the emergency request to match
     * @return the updated emergency request response
     */
    @Transactional
    public EmergencyRequestResponse matchRequest(UUID requestId) {
        log.info("Attempting to match emergency request: {}", requestId);

        EmergencyRequest request = emergencyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyRequest", "id", requestId));

        if (request.getStatus() != EmergencyStatusEnum.OPEN
                && request.getStatus() != EmergencyStatusEnum.PARTIALLY_FULFILLED) {
            log.warn("Request {} is not in a matchable status: {}", requestId, request.getStatus());
            return emergencyRequestMapper.toResponse(request);
        }

        List<UUID> matchedUnitIds = findCompatibleUnits(
                request.getBloodGroupId(),
                request.getComponentTypeId(),
                request.getUnitsNeeded() - request.getUnitsFulfilled(),
                request.getBranchId()
        );

        if (!matchedUnitIds.isEmpty()) {
            int newFulfilled = request.getUnitsFulfilled() + matchedUnitIds.size();
            request.setUnitsFulfilled(newFulfilled);

            if (newFulfilled >= request.getUnitsNeeded()) {
                request.setStatus(EmergencyStatusEnum.FULFILLED);
                log.info("Request {} fully fulfilled with {} units", requestId, newFulfilled);
            } else {
                request.setStatus(EmergencyStatusEnum.PARTIALLY_FULFILLED);
                log.info("Request {} partially fulfilled: {}/{} units",
                        requestId, newFulfilled, request.getUnitsNeeded());
            }

            request = emergencyRequestRepository.save(request);

            eventPublisher.publishBloodRequestMatched(new BloodRequestMatchedEvent(
                    request.getId(),
                    request.getBranchId(),
                    matchedUnitIds,
                    Instant.now()
            ));
        } else {
            log.warn("No compatible units found for request: {}", requestId);
        }

        return emergencyRequestMapper.toResponse(request);
    }

    /**
     * Finds compatible blood units using ABO/Rh compatibility and FEFO strategy.
     * This is a placeholder that returns an empty list; full implementation requires
     * direct inventory queries or cross-service communication.
     */
    private List<UUID> findCompatibleUnits(UUID bloodGroupId, UUID componentTypeId,
                                            int unitsRequired, UUID branchId) {
        log.debug("Searching for {} compatible units: bloodGroup={}, componentType={}, branch={}",
                unitsRequired, bloodGroupId, componentTypeId, branchId);
        // In production, this would query inventory-service or shared DB tables
        // using ABO/Rh compatibility matrix and FEFO (oldest-expiring first) selection
        return List.of();
    }

    /**
     * Gets all open emergency requests that still need matching.
     */
    public List<EmergencyRequestResponse> getOpenRequests() {
        log.debug("Fetching open emergency requests for matching");
        List<EmergencyRequest> openRequests = emergencyRequestRepository
                .findByStatus(EmergencyStatusEnum.OPEN, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        return emergencyRequestMapper.toResponseList(openRequests);
    }
}
