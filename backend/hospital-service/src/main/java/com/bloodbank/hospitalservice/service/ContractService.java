package com.bloodbank.hospitalservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.hospitalservice.dto.HospitalContractCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalContractResponse;
import com.bloodbank.hospitalservice.entity.HospitalContract;
import com.bloodbank.hospitalservice.enums.ContractStatusEnum;
import com.bloodbank.hospitalservice.mapper.HospitalContractMapper;
import com.bloodbank.hospitalservice.repository.HospitalContractRepository;
import com.bloodbank.hospitalservice.repository.HospitalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    private final HospitalContractRepository contractRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalContractMapper contractMapper;

    public ContractService(HospitalContractRepository contractRepository,
                           HospitalRepository hospitalRepository,
                           HospitalContractMapper contractMapper) {
        this.contractRepository = contractRepository;
        this.hospitalRepository = hospitalRepository;
        this.contractMapper = contractMapper;
    }

    @Transactional
    public HospitalContractResponse createContract(HospitalContractCreateRequest request) {
        log.info("Creating contract for hospital: {}", request.hospitalId());

        hospitalRepository.findById(request.hospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", request.hospitalId()));

        if (contractRepository.existsByContractNumber(request.contractNumber())) {
            throw new ConflictException("Contract with number '" + request.contractNumber() + "' already exists");
        }

        HospitalContract contract = contractMapper.toEntity(request);
        contract.setStatus(ContractStatusEnum.ACTIVE);
        contract.setBranchId(request.branchId());

        contract = contractRepository.save(contract);
        log.info("Contract created: {}", contract.getContractNumber());
        return contractMapper.toResponse(contract);
    }

    public HospitalContractResponse getContractById(UUID id) {
        log.debug("Fetching contract by id: {}", id);
        HospitalContract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalContract", "id", id));
        return contractMapper.toResponse(contract);
    }

    public PagedResponse<HospitalContractResponse> getContractsByHospitalId(UUID hospitalId, Pageable pageable) {
        log.debug("Fetching contracts for hospital: {}", hospitalId);
        Page<HospitalContract> page = contractRepository.findByHospitalId(hospitalId, pageable);
        return toPagedResponse(page);
    }

    public List<HospitalContractResponse> getActiveContracts(UUID hospitalId) {
        log.debug("Fetching active contracts for hospital: {}", hospitalId);
        List<HospitalContract> contracts = contractRepository
                .findByHospitalIdAndStatus(hospitalId, ContractStatusEnum.ACTIVE);
        return contractMapper.toResponseList(contracts);
    }

    @Transactional
    public HospitalContractResponse updateContractStatus(UUID id, ContractStatusEnum status) {
        log.info("Updating contract {} status to: {}", id, status);
        HospitalContract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalContract", "id", id));
        contract.setStatus(status);
        contract = contractRepository.save(contract);
        return contractMapper.toResponse(contract);
    }

    private PagedResponse<HospitalContractResponse> toPagedResponse(Page<HospitalContract> page) {
        List<HospitalContractResponse> content = contractMapper.toResponseList(page.getContent());
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
