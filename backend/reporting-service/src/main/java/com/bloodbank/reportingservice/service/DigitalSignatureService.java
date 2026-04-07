package com.bloodbank.reportingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.reportingservice.dto.DigitalSignatureCreateRequest;
import com.bloodbank.reportingservice.dto.DigitalSignatureResponse;
import com.bloodbank.reportingservice.entity.DigitalSignature;
import com.bloodbank.reportingservice.mapper.DigitalSignatureMapper;
import com.bloodbank.reportingservice.repository.DigitalSignatureRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DigitalSignatureService {

    private static final Logger log = LoggerFactory.getLogger(DigitalSignatureService.class);

    private final DigitalSignatureRepository digitalSignatureRepository;
    private final DigitalSignatureMapper digitalSignatureMapper;

    public DigitalSignatureService(DigitalSignatureRepository digitalSignatureRepository,
                                   DigitalSignatureMapper digitalSignatureMapper) {
        this.digitalSignatureRepository = digitalSignatureRepository;
        this.digitalSignatureMapper = digitalSignatureMapper;
    }

    @Transactional
    public DigitalSignatureResponse create(DigitalSignatureCreateRequest request) {
        log.info("Creating digital signature for entity {} by signer {}", request.entityType(), request.signerId());
        DigitalSignature signature = digitalSignatureMapper.toEntity(request);
        signature = digitalSignatureRepository.save(signature);
        return digitalSignatureMapper.toResponse(signature);
    }

    public DigitalSignatureResponse verify(UUID signatureId) {
        log.info("Verifying digital signature {}", signatureId);
        DigitalSignature signature = digitalSignatureRepository.findById(signatureId)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalSignature", "id", signatureId.toString()));
        return digitalSignatureMapper.toResponse(signature);
    }

    public List<DigitalSignatureResponse> getByEntityId(String entityType, UUID entityId) {
        log.debug("Fetching digital signatures for {} with id {}", entityType, entityId);
        List<DigitalSignature> signatures = digitalSignatureRepository
                .findByEntityTypeAndEntityIdOrderBySignedAtDesc(entityType, entityId);
        return digitalSignatureMapper.toResponseList(signatures);
    }
}
