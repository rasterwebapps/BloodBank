package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.SopCreateRequest;
import com.bloodbank.complianceservice.dto.SopResponse;
import com.bloodbank.complianceservice.entity.SopDocument;
import com.bloodbank.complianceservice.enums.SopCategoryEnum;
import com.bloodbank.complianceservice.enums.SopStatusEnum;
import com.bloodbank.complianceservice.mapper.SopDocumentMapper;
import com.bloodbank.complianceservice.repository.SopDocumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SopService {

    private static final Logger log = LoggerFactory.getLogger(SopService.class);

    private final SopDocumentRepository sopRepository;
    private final SopDocumentMapper sopMapper;

    public SopService(SopDocumentRepository sopRepository, SopDocumentMapper sopMapper) {
        this.sopRepository = sopRepository;
        this.sopMapper = sopMapper;
    }

    @Transactional
    public SopResponse create(SopCreateRequest request) {
        log.info("Creating SOP document: code={}, title={}", request.sopCode(), request.sopTitle());
        SopDocument sop = sopMapper.toEntity(request);
        sop.setBranchId(request.branchId());
        sop.setStatus(SopStatusEnum.DRAFT);
        if (request.versionNumber() != null) {
            sop.setVersionNumber(request.versionNumber());
        }
        sop = sopRepository.save(sop);
        return sopMapper.toResponse(sop);
    }

    public SopResponse getById(UUID id) {
        SopDocument sop = sopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SopDocument", "id", id));
        return sopMapper.toResponse(sop);
    }

    public List<SopResponse> getByFrameworkId(UUID frameworkId) {
        return sopMapper.toResponseList(sopRepository.findByFrameworkId(frameworkId));
    }

    public List<SopResponse> getByStatus(SopStatusEnum status) {
        return sopMapper.toResponseList(sopRepository.findByStatus(status));
    }

    public List<SopResponse> getByCategory(SopCategoryEnum category) {
        return sopMapper.toResponseList(sopRepository.findByCategory(category));
    }

    @Transactional
    public SopResponse updateStatus(UUID id, SopStatusEnum newStatus) {
        SopDocument sop = sopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SopDocument", "id", id));

        validateStatusTransition(sop.getStatus(), newStatus);
        sop.setStatus(newStatus);
        sop = sopRepository.save(sop);
        log.info("Updated SOP status: id={}, newStatus={}", id, newStatus);
        return sopMapper.toResponse(sop);
    }

    @Transactional
    public SopResponse approve(UUID id, String approvedBy) {
        SopDocument sop = sopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SopDocument", "id", id));

        if (sop.getStatus() != SopStatusEnum.REVIEW) {
            throw new BusinessException("SOP must be in REVIEW status to approve", "SOP_INVALID_STATE");
        }

        sop.setStatus(SopStatusEnum.APPROVED);
        sop.setApprovedBy(approvedBy);
        sop.setApprovedAt(Instant.now());
        sop = sopRepository.save(sop);
        log.info("Approved SOP: id={}, approvedBy={}", id, approvedBy);
        return sopMapper.toResponse(sop);
    }

    @Transactional
    public SopResponse retire(UUID id) {
        SopDocument sop = sopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SopDocument", "id", id));

        if (sop.getStatus() != SopStatusEnum.APPROVED && sop.getStatus() != SopStatusEnum.SUPERSEDED) {
            throw new BusinessException("SOP must be APPROVED or SUPERSEDED to retire", "SOP_INVALID_STATE");
        }

        sop.setStatus(SopStatusEnum.RETIRED);
        sop = sopRepository.save(sop);
        log.info("Retired SOP: id={}", id);
        return sopMapper.toResponse(sop);
    }

    private void validateStatusTransition(SopStatusEnum current, SopStatusEnum target) {
        boolean valid = switch (current) {
            case DRAFT -> target == SopStatusEnum.REVIEW;
            case REVIEW -> target == SopStatusEnum.APPROVED || target == SopStatusEnum.DRAFT;
            case APPROVED -> target == SopStatusEnum.SUPERSEDED || target == SopStatusEnum.RETIRED;
            case SUPERSEDED -> target == SopStatusEnum.RETIRED;
            case RETIRED -> false;
        };

        if (!valid) {
            throw new BusinessException(
                    String.format("Invalid SOP status transition from %s to %s", current, target),
                    "SOP_INVALID_TRANSITION"
            );
        }
    }
}
