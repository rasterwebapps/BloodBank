package com.bloodbank.reportingservice.service;

import com.bloodbank.reportingservice.dto.ChainOfCustodyCreateRequest;
import com.bloodbank.reportingservice.dto.ChainOfCustodyResponse;
import com.bloodbank.reportingservice.entity.ChainOfCustody;
import com.bloodbank.reportingservice.mapper.ChainOfCustodyMapper;
import com.bloodbank.reportingservice.repository.ChainOfCustodyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ChainOfCustodyService {

    private static final Logger log = LoggerFactory.getLogger(ChainOfCustodyService.class);

    private final ChainOfCustodyRepository chainOfCustodyRepository;
    private final ChainOfCustodyMapper chainOfCustodyMapper;

    public ChainOfCustodyService(ChainOfCustodyRepository chainOfCustodyRepository,
                                 ChainOfCustodyMapper chainOfCustodyMapper) {
        this.chainOfCustodyRepository = chainOfCustodyRepository;
        this.chainOfCustodyMapper = chainOfCustodyMapper;
    }

    @Transactional
    public ChainOfCustodyResponse addEvent(ChainOfCustodyCreateRequest request) {
        log.info("Adding custody event {} for entity {}", request.custodyEvent(), request.entityType());
        ChainOfCustody custody = chainOfCustodyMapper.toEntity(request);
        custody = chainOfCustodyRepository.save(custody);
        return chainOfCustodyMapper.toResponse(custody);
    }

    public List<ChainOfCustodyResponse> getByEntityId(String entityType, UUID entityId) {
        log.debug("Fetching custody chain for {} with id {}", entityType, entityId);
        List<ChainOfCustody> chain = chainOfCustodyRepository
                .findByEntityTypeAndEntityIdOrderByEventTimeAsc(entityType, entityId);
        return chainOfCustodyMapper.toResponseList(chain);
    }

    public List<ChainOfCustodyResponse> getFullChain(UUID entityId) {
        log.debug("Fetching full custody chain for entity {}", entityId);
        List<ChainOfCustody> chain = chainOfCustodyRepository
                .findByEntityTypeAndEntityIdOrderByEventTimeAsc("BloodUnit", entityId);
        return chainOfCustodyMapper.toResponseList(chain);
    }
}
