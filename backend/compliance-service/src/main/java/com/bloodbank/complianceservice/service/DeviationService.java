package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.CorrectiveActionRequest;
import com.bloodbank.complianceservice.dto.DeviationCreateRequest;
import com.bloodbank.complianceservice.dto.DeviationResponse;
import com.bloodbank.complianceservice.entity.Deviation;
import com.bloodbank.complianceservice.enums.DeviationSeverityEnum;
import com.bloodbank.complianceservice.enums.DeviationStatusEnum;
import com.bloodbank.complianceservice.mapper.DeviationMapper;
import com.bloodbank.complianceservice.repository.DeviationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DeviationService {

    private static final Logger log = LoggerFactory.getLogger(DeviationService.class);

    private final DeviationRepository deviationRepository;
    private final DeviationMapper deviationMapper;

    public DeviationService(DeviationRepository deviationRepository, DeviationMapper deviationMapper) {
        this.deviationRepository = deviationRepository;
        this.deviationMapper = deviationMapper;
    }

    @Transactional
    public DeviationResponse create(DeviationCreateRequest request) {
        log.info("Creating deviation: type={}, severity={}", request.deviationType(), request.severity());
        Deviation deviation = deviationMapper.toEntity(request);
        deviation.setBranchId(request.branchId());
        deviation.setDeviationNumber("DEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        deviation.setDetectedDate(Instant.now());
        deviation.setStatus(DeviationStatusEnum.OPEN);
        deviation = deviationRepository.save(deviation);
        return deviationMapper.toResponse(deviation);
    }

    public DeviationResponse getById(UUID id) {
        Deviation deviation = deviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deviation", "id", id));
        return deviationMapper.toResponse(deviation);
    }

    public DeviationResponse getByDeviationNumber(String deviationNumber) {
        Deviation deviation = deviationRepository.findByDeviationNumber(deviationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Deviation", "deviationNumber", deviationNumber));
        return deviationMapper.toResponse(deviation);
    }

    public List<DeviationResponse> getByStatus(DeviationStatusEnum status) {
        return deviationMapper.toResponseList(deviationRepository.findByStatus(status));
    }

    public List<DeviationResponse> getBySeverity(DeviationSeverityEnum severity) {
        return deviationMapper.toResponseList(deviationRepository.findBySeverity(severity));
    }

    @Transactional
    public DeviationResponse investigate(UUID id) {
        Deviation deviation = deviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deviation", "id", id));

        if (deviation.getStatus() != DeviationStatusEnum.OPEN && deviation.getStatus() != DeviationStatusEnum.REOPENED) {
            throw new BusinessException("Deviation must be OPEN or REOPENED to investigate", "DEVIATION_INVALID_STATE");
        }

        deviation.setStatus(DeviationStatusEnum.UNDER_INVESTIGATION);
        deviation = deviationRepository.save(deviation);
        log.info("Deviation under investigation: id={}", id);
        return deviationMapper.toResponse(deviation);
    }

    @Transactional
    public DeviationResponse addCorrectiveAction(UUID id, CorrectiveActionRequest request) {
        Deviation deviation = deviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deviation", "id", id));

        if (deviation.getStatus() != DeviationStatusEnum.UNDER_INVESTIGATION) {
            throw new BusinessException("Deviation must be UNDER_INVESTIGATION to add corrective action", "DEVIATION_INVALID_STATE");
        }

        deviation.setRootCause(request.rootCause());
        deviation.setCorrectiveAction(request.correctiveAction());
        deviation.setPreventiveAction(request.preventiveAction());
        deviation.setStatus(DeviationStatusEnum.CORRECTIVE_ACTION);
        deviation = deviationRepository.save(deviation);
        log.info("Added corrective action to deviation: id={}", id);
        return deviationMapper.toResponse(deviation);
    }

    @Transactional
    public DeviationResponse close(UUID id, String closedBy) {
        Deviation deviation = deviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deviation", "id", id));

        if (deviation.getStatus() != DeviationStatusEnum.CORRECTIVE_ACTION) {
            throw new BusinessException("Deviation must be in CORRECTIVE_ACTION status to close", "DEVIATION_INVALID_STATE");
        }

        deviation.setStatus(DeviationStatusEnum.CLOSED);
        deviation.setClosureDate(Instant.now());
        deviation.setClosedBy(closedBy);
        deviation = deviationRepository.save(deviation);
        log.info("Closed deviation: id={}, closedBy={}", id, closedBy);
        return deviationMapper.toResponse(deviation);
    }

    @Transactional
    public DeviationResponse reopen(UUID id) {
        Deviation deviation = deviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deviation", "id", id));

        if (deviation.getStatus() != DeviationStatusEnum.CLOSED) {
            throw new BusinessException("Deviation must be CLOSED to reopen", "DEVIATION_INVALID_STATE");
        }

        deviation.setStatus(DeviationStatusEnum.REOPENED);
        deviation.setClosureDate(null);
        deviation.setClosedBy(null);
        deviation = deviationRepository.save(deviation);
        log.info("Reopened deviation: id={}", id);
        return deviationMapper.toResponse(deviation);
    }
}
