package com.bloodbank.requestmatchingservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.requestmatchingservice.dto.DisasterEventCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DisasterEventResponse;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationResponse;
import com.bloodbank.requestmatchingservice.entity.DisasterEvent;
import com.bloodbank.requestmatchingservice.entity.DonorMobilization;
import com.bloodbank.requestmatchingservice.enums.DisasterSeverityEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterStatusEnum;
import com.bloodbank.requestmatchingservice.mapper.DisasterEventMapper;
import com.bloodbank.requestmatchingservice.mapper.DonorMobilizationMapper;
import com.bloodbank.requestmatchingservice.repository.DisasterEventRepository;
import com.bloodbank.requestmatchingservice.repository.DonorMobilizationRepository;

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
 * Service for mass casualty protocol, donor mobilization,
 * and emergency stock rebalancing across branches.
 */
@Service
@Transactional(readOnly = true)
public class DisasterResponseService {

    private static final Logger log = LoggerFactory.getLogger(DisasterResponseService.class);

    private final DisasterEventRepository disasterEventRepository;
    private final DonorMobilizationRepository donorMobilizationRepository;
    private final DisasterEventMapper disasterEventMapper;
    private final DonorMobilizationMapper donorMobilizationMapper;

    public DisasterResponseService(DisasterEventRepository disasterEventRepository,
                                   DonorMobilizationRepository donorMobilizationRepository,
                                   DisasterEventMapper disasterEventMapper,
                                   DonorMobilizationMapper donorMobilizationMapper) {
        this.disasterEventRepository = disasterEventRepository;
        this.donorMobilizationRepository = donorMobilizationRepository;
        this.disasterEventMapper = disasterEventMapper;
        this.donorMobilizationMapper = donorMobilizationMapper;
    }

    @Transactional
    public DisasterEventResponse createDisasterEvent(DisasterEventCreateRequest request) {
        log.info("Creating disaster event: {} (type: {})", request.eventName(), request.eventType());

        DisasterEvent entity = disasterEventMapper.toEntity(request);
        entity.setEventCode("DE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setStatus(DisasterStatusEnum.ACTIVE);
        entity.setBranchId(request.branchId());
        if (entity.getStartDate() == null) {
            entity.setStartDate(Instant.now());
        }

        entity = disasterEventRepository.save(entity);
        log.info("Disaster event created: {} (code: {})", entity.getEventName(), entity.getEventCode());
        return disasterEventMapper.toResponse(entity);
    }

    public DisasterEventResponse getDisasterEventById(UUID id) {
        log.debug("Fetching disaster event by id: {}", id);
        DisasterEvent entity = disasterEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DisasterEvent", "id", id));
        return disasterEventMapper.toResponse(entity);
    }

    public DisasterEventResponse getByEventCode(String eventCode) {
        log.debug("Fetching disaster event by code: {}", eventCode);
        DisasterEvent entity = disasterEventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("DisasterEvent", "eventCode", eventCode));
        return disasterEventMapper.toResponse(entity);
    }

    public PagedResponse<DisasterEventResponse> getByStatus(DisasterStatusEnum status, Pageable pageable) {
        log.debug("Fetching disaster events by status: {}", status);
        Page<DisasterEvent> page = disasterEventRepository.findByStatus(status, pageable);
        return toDisasterPagedResponse(page);
    }

    public List<DisasterEventResponse> getActiveEvents() {
        log.debug("Fetching active disaster events");
        List<DisasterEvent> events = disasterEventRepository
                .findByStatusIn(List.of(DisasterStatusEnum.ACTIVE, DisasterStatusEnum.ESCALATED));
        return disasterEventMapper.toResponseList(events);
    }

    /**
     * Escalates a disaster event to a higher severity level.
     */
    @Transactional
    public DisasterEventResponse escalateEvent(UUID id) {
        log.info("Escalating disaster event: {}", id);
        DisasterEvent entity = disasterEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DisasterEvent", "id", id));

        if (entity.getStatus() == DisasterStatusEnum.CLOSED) {
            throw new BusinessException("Cannot escalate a closed disaster event", "DISASTER_CLOSED");
        }

        DisasterSeverityEnum currentSeverity = entity.getSeverity();
        if (currentSeverity == DisasterSeverityEnum.LOW) {
            entity.setSeverity(DisasterSeverityEnum.MEDIUM);
        } else if (currentSeverity == DisasterSeverityEnum.MEDIUM) {
            entity.setSeverity(DisasterSeverityEnum.HIGH);
        } else if (currentSeverity == DisasterSeverityEnum.HIGH) {
            entity.setSeverity(DisasterSeverityEnum.CRITICAL);
        }

        entity.setStatus(DisasterStatusEnum.ESCALATED);
        entity = disasterEventRepository.save(entity);
        log.info("Disaster event {} escalated to severity: {}", id, entity.getSeverity());
        return disasterEventMapper.toResponse(entity);
    }

    /**
     * Closes a disaster event.
     */
    @Transactional
    public DisasterEventResponse closeEvent(UUID id) {
        log.info("Closing disaster event: {}", id);
        DisasterEvent entity = disasterEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DisasterEvent", "id", id));

        entity.setStatus(DisasterStatusEnum.CLOSED);
        entity.setEndDate(Instant.now());
        entity = disasterEventRepository.save(entity);
        log.info("Disaster event {} closed", id);
        return disasterEventMapper.toResponse(entity);
    }

    // --- Donor Mobilization ---

    @Transactional
    public DonorMobilizationResponse mobilizeDonor(DonorMobilizationCreateRequest request) {
        log.info("Mobilizing donor: {} for disaster/emergency", request.donorId());

        if (request.disasterEventId() != null) {
            disasterEventRepository.findById(request.disasterEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("DisasterEvent", "id", request.disasterEventId()));

            if (donorMobilizationRepository.existsByDisasterEventIdAndDonorId(
                    request.disasterEventId(), request.donorId())) {
                throw new BusinessException("Donor already mobilized for this disaster event", "DONOR_ALREADY_MOBILIZED");
            }
        }

        DonorMobilization entity = donorMobilizationMapper.toEntity(request);
        entity.setContactedAt(Instant.now());
        entity.setDonationCompleted(false);
        entity.setBranchId(request.branchId());

        entity = donorMobilizationRepository.save(entity);
        log.info("Donor {} mobilized successfully", request.donorId());
        return donorMobilizationMapper.toResponse(entity);
    }

    public DonorMobilizationResponse getMobilizationById(UUID id) {
        log.debug("Fetching donor mobilization by id: {}", id);
        DonorMobilization entity = donorMobilizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DonorMobilization", "id", id));
        return donorMobilizationMapper.toResponse(entity);
    }

    public List<DonorMobilizationResponse> getMobilizationsByDisaster(UUID disasterEventId) {
        log.debug("Fetching mobilizations for disaster event: {}", disasterEventId);
        List<DonorMobilization> mobilizations = donorMobilizationRepository
                .findByDisasterEventId(disasterEventId);
        return donorMobilizationMapper.toResponseList(mobilizations);
    }

    public PagedResponse<DonorMobilizationResponse> getMobilizationsByDisasterPaged(
            UUID disasterEventId, Pageable pageable) {
        log.debug("Fetching paged mobilizations for disaster event: {}", disasterEventId);
        Page<DonorMobilization> page = donorMobilizationRepository
                .findByDisasterEventId(disasterEventId, pageable);
        return toMobilizationPagedResponse(page);
    }

    /**
     * Records a donor's response to a mobilization request.
     */
    @Transactional
    public DonorMobilizationResponse recordResponse(UUID mobilizationId,
                                                     com.bloodbank.requestmatchingservice.enums.MobilizationStatusEnum response) {
        log.info("Recording response {} for mobilization: {}", response, mobilizationId);
        DonorMobilization entity = donorMobilizationRepository.findById(mobilizationId)
                .orElseThrow(() -> new ResourceNotFoundException("DonorMobilization", "id", mobilizationId));

        entity.setResponse(response);
        entity.setResponseAt(Instant.now());
        entity = donorMobilizationRepository.save(entity);
        log.info("Mobilization {} response recorded: {}", mobilizationId, response);
        return donorMobilizationMapper.toResponse(entity);
    }

    /**
     * Marks a mobilized donation as completed.
     */
    @Transactional
    public DonorMobilizationResponse markDonationCompleted(UUID mobilizationId, UUID collectionId) {
        log.info("Marking donation completed for mobilization: {}", mobilizationId);
        DonorMobilization entity = donorMobilizationRepository.findById(mobilizationId)
                .orElseThrow(() -> new ResourceNotFoundException("DonorMobilization", "id", mobilizationId));

        entity.setDonationCompleted(true);
        entity.setCollectionId(collectionId);
        entity = donorMobilizationRepository.save(entity);
        log.info("Mobilization {} donation completed with collection: {}", mobilizationId, collectionId);
        return donorMobilizationMapper.toResponse(entity);
    }

    private PagedResponse<DisasterEventResponse> toDisasterPagedResponse(Page<DisasterEvent> page) {
        List<DisasterEventResponse> content = disasterEventMapper.toResponseList(page.getContent());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private PagedResponse<DonorMobilizationResponse> toMobilizationPagedResponse(Page<DonorMobilization> page) {
        List<DonorMobilizationResponse> content = donorMobilizationMapper.toResponseList(page.getContent());
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
