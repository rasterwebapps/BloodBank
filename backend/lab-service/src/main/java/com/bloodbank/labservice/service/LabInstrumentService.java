package com.bloodbank.labservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.labservice.dto.LabInstrumentCreateRequest;
import com.bloodbank.labservice.dto.LabInstrumentResponse;
import com.bloodbank.labservice.entity.LabInstrument;
import com.bloodbank.labservice.enums.InstrumentStatusEnum;
import com.bloodbank.labservice.mapper.LabInstrumentMapper;
import com.bloodbank.labservice.repository.LabInstrumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LabInstrumentService {

    private static final Logger log = LoggerFactory.getLogger(LabInstrumentService.class);

    private final LabInstrumentRepository labInstrumentRepository;
    private final LabInstrumentMapper labInstrumentMapper;

    public LabInstrumentService(LabInstrumentRepository labInstrumentRepository,
                                LabInstrumentMapper labInstrumentMapper) {
        this.labInstrumentRepository = labInstrumentRepository;
        this.labInstrumentMapper = labInstrumentMapper;
    }

    @Transactional
    public LabInstrumentResponse createInstrument(LabInstrumentCreateRequest request) {
        log.info("Creating lab instrument: name={}, type={}", request.instrumentName(), request.instrumentType());
        LabInstrument instrument = labInstrumentMapper.toEntity(request);
        instrument.setInstrumentCode("INS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        instrument.setStatus(InstrumentStatusEnum.ACTIVE);
        instrument.setBranchId(request.branchId());
        instrument = labInstrumentRepository.save(instrument);
        return labInstrumentMapper.toResponse(instrument);
    }

    public List<LabInstrumentResponse> getInstrumentsByBranch(UUID branchId) {
        return labInstrumentMapper.toResponseList(labInstrumentRepository.findByBranchId(branchId));
    }

    public LabInstrumentResponse getInstrumentById(UUID id) {
        LabInstrument instrument = labInstrumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LabInstrument", "id", id));
        return labInstrumentMapper.toResponse(instrument);
    }

    @Transactional
    public LabInstrumentResponse updateInstrumentStatus(UUID id, InstrumentStatusEnum status) {
        LabInstrument instrument = labInstrumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LabInstrument", "id", id));
        instrument.setStatus(status);
        instrument = labInstrumentRepository.save(instrument);
        log.info("Updated instrument {} status to {}", id, status);
        return labInstrumentMapper.toResponse(instrument);
    }
}
