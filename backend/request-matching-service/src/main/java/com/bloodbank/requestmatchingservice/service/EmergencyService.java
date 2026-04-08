package com.bloodbank.requestmatchingservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.EmergencyRequestEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestCreateRequest;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestResponse;
import com.bloodbank.requestmatchingservice.entity.EmergencyRequest;
import com.bloodbank.requestmatchingservice.enums.EmergencyPriorityEnum;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;
import com.bloodbank.requestmatchingservice.event.RequestMatchingEventPublisher;
import com.bloodbank.requestmatchingservice.mapper.EmergencyRequestMapper;
import com.bloodbank.requestmatchingservice.repository.EmergencyRequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for emergency blood request workflow, O-negative emergency protocol,
 * and priority escalation.
 */
@Service
@Transactional(readOnly = true)
public class EmergencyService {

    private static final Logger log = LoggerFactory.getLogger(EmergencyService.class);

    private final EmergencyRequestRepository emergencyRequestRepository;
    private final EmergencyRequestMapper emergencyRequestMapper;
    private final RequestMatchingEventPublisher eventPublisher;

    public EmergencyService(EmergencyRequestRepository emergencyRequestRepository,
                            EmergencyRequestMapper emergencyRequestMapper,
                            RequestMatchingEventPublisher eventPublisher) {
        this.emergencyRequestRepository = emergencyRequestRepository;
        this.emergencyRequestMapper = emergencyRequestMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public EmergencyRequestResponse createEmergencyRequest(EmergencyRequestCreateRequest request) {
        log.info("Creating emergency request with priority: {}", request.priority());

        EmergencyRequest entity = emergencyRequestMapper.toEntity(request);
        entity.setRequestNumber("ER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setStatus(EmergencyStatusEnum.OPEN);
        entity.setUnitsFulfilled(0);
        entity.setBroadcastSent(false);
        entity.setBranchId(request.branchId());

        entity = emergencyRequestRepository.save(entity);
        log.info("Emergency request created: {}", entity.getRequestNumber());

        eventPublisher.publishEmergencyRequest(new EmergencyRequestEvent(
                entity.getId(),
                entity.getBranchId(),
                entity.getBloodGroupId().toString(),
                entity.getPriority().name(),
                Instant.now()
        ));

        return emergencyRequestMapper.toResponse(entity);
    }

    public EmergencyRequestResponse getById(UUID id) {
        log.debug("Fetching emergency request by id: {}", id);
        EmergencyRequest entity = emergencyRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyRequest", "id", id));
        return emergencyRequestMapper.toResponse(entity);
    }

    public EmergencyRequestResponse getByRequestNumber(String requestNumber) {
        log.debug("Fetching emergency request by number: {}", requestNumber);
        EmergencyRequest entity = emergencyRequestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyRequest", "requestNumber", requestNumber));
        return emergencyRequestMapper.toResponse(entity);
    }

    public PagedResponse<EmergencyRequestResponse> getByStatus(EmergencyStatusEnum status, Pageable pageable) {
        log.debug("Fetching emergency requests by status: {}", status);
        Page<EmergencyRequest> page = emergencyRequestRepository.findByStatus(status, pageable);
        return toPagedResponse(page);
    }

    /**
     * Escalates an emergency request to a higher priority level.
     */
    @Transactional
    public EmergencyRequestResponse escalate(UUID id) {
        log.info("Escalating emergency request: {}", id);
        EmergencyRequest entity = emergencyRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyRequest", "id", id));

        EmergencyPriorityEnum currentPriority = entity.getPriority();
        if (currentPriority == EmergencyPriorityEnum.EMERGENCY) {
            entity.setPriority(EmergencyPriorityEnum.CRITICAL);
        } else if (currentPriority == EmergencyPriorityEnum.CRITICAL) {
            entity.setPriority(EmergencyPriorityEnum.MASS_CASUALTY);
        }

        entity = emergencyRequestRepository.save(entity);
        log.info("Emergency request {} escalated to {}", id, entity.getPriority());

        eventPublisher.publishEmergencyRequest(new EmergencyRequestEvent(
                entity.getId(),
                entity.getBranchId(),
                entity.getBloodGroupId().toString(),
                entity.getPriority().name(),
                Instant.now()
        ));

        return emergencyRequestMapper.toResponse(entity);
    }

    /**
     * Cancels an emergency request.
     */
    @Transactional
    public EmergencyRequestResponse cancel(UUID id) {
        log.info("Cancelling emergency request: {}", id);
        EmergencyRequest entity = emergencyRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyRequest", "id", id));

        entity.setStatus(EmergencyStatusEnum.CANCELLED);
        entity = emergencyRequestRepository.save(entity);
        log.info("Emergency request {} cancelled", id);
        return emergencyRequestMapper.toResponse(entity);
    }

    /**
     * Marks broadcast as sent for an emergency request.
     */
    @Transactional
    public EmergencyRequestResponse markBroadcastSent(UUID id) {
        log.info("Marking broadcast sent for emergency request: {}", id);
        EmergencyRequest entity = emergencyRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyRequest", "id", id));

        entity.setBroadcastSent(true);
        entity = emergencyRequestRepository.save(entity);
        return emergencyRequestMapper.toResponse(entity);
    }

    public List<EmergencyRequestResponse> getByHospital(UUID hospitalId) {
        log.debug("Fetching emergency requests for hospital: {}", hospitalId);
        List<EmergencyRequest> requests = emergencyRequestRepository.findByHospitalId(hospitalId);
        return emergencyRequestMapper.toResponseList(requests);
    }

    private PagedResponse<EmergencyRequestResponse> toPagedResponse(Page<EmergencyRequest> page) {
        List<EmergencyRequestResponse> content = emergencyRequestMapper.toResponseList(page.getContent());
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
