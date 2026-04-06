package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.entity.BloodComponent;
import com.bloodbank.inventoryservice.entity.ComponentLabel;
import com.bloodbank.inventoryservice.entity.ComponentProcessing;
import com.bloodbank.inventoryservice.entity.PooledComponent;
import com.bloodbank.inventoryservice.enums.ProcessResultEnum;
import com.bloodbank.inventoryservice.mapper.BloodComponentMapper;
import com.bloodbank.inventoryservice.mapper.ComponentLabelMapper;
import com.bloodbank.inventoryservice.mapper.ComponentProcessingMapper;
import com.bloodbank.inventoryservice.mapper.PooledComponentMapper;
import com.bloodbank.inventoryservice.repository.BloodComponentRepository;
import com.bloodbank.inventoryservice.repository.ComponentLabelRepository;
import com.bloodbank.inventoryservice.repository.ComponentProcessingRepository;
import com.bloodbank.inventoryservice.repository.PooledComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BloodComponentService {

    private static final Logger log = LoggerFactory.getLogger(BloodComponentService.class);

    private final BloodComponentRepository bloodComponentRepository;
    private final ComponentProcessingRepository componentProcessingRepository;
    private final ComponentLabelRepository componentLabelRepository;
    private final PooledComponentRepository pooledComponentRepository;
    private final BloodComponentMapper bloodComponentMapper;
    private final ComponentProcessingMapper componentProcessingMapper;
    private final ComponentLabelMapper componentLabelMapper;
    private final PooledComponentMapper pooledComponentMapper;

    public BloodComponentService(BloodComponentRepository bloodComponentRepository,
                                 ComponentProcessingRepository componentProcessingRepository,
                                 ComponentLabelRepository componentLabelRepository,
                                 PooledComponentRepository pooledComponentRepository,
                                 BloodComponentMapper bloodComponentMapper,
                                 ComponentProcessingMapper componentProcessingMapper,
                                 ComponentLabelMapper componentLabelMapper,
                                 PooledComponentMapper pooledComponentMapper) {
        this.bloodComponentRepository = bloodComponentRepository;
        this.componentProcessingRepository = componentProcessingRepository;
        this.componentLabelRepository = componentLabelRepository;
        this.pooledComponentRepository = pooledComponentRepository;
        this.bloodComponentMapper = bloodComponentMapper;
        this.componentProcessingMapper = componentProcessingMapper;
        this.componentLabelMapper = componentLabelMapper;
        this.pooledComponentMapper = pooledComponentMapper;
    }

    @Transactional
    public BloodComponentResponse createComponent(BloodComponentCreateRequest request) {
        log.info("Creating blood component for blood unit: {}", request.bloodUnitId());
        BloodComponent component = bloodComponentMapper.toEntity(request);
        component.setComponentNumber(generateComponentNumber());
        component.setPreparationDate(Instant.now());
        component.setStatus(ComponentStatusEnum.QUARANTINED);
        component.setBranchId(request.branchId());
        component = bloodComponentRepository.save(component);
        return bloodComponentMapper.toResponse(component);
    }

    public BloodComponentResponse getById(UUID id) {
        BloodComponent component = bloodComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", "id", id));
        return bloodComponentMapper.toResponse(component);
    }

    public List<BloodComponentResponse> getByBloodUnit(UUID bloodUnitId) {
        return bloodComponentMapper.toResponseList(
                bloodComponentRepository.findByBloodUnitId(bloodUnitId));
    }

    @Transactional
    public BloodComponentResponse updateStatus(UUID id, ComponentStatusEnum status) {
        log.info("Updating component {} status to {}", id, status);
        BloodComponent component = bloodComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", "id", id));
        component.setStatus(status);
        component = bloodComponentRepository.save(component);
        return bloodComponentMapper.toResponse(component);
    }

    @Transactional
    public ComponentProcessingResponse processComponent(UUID componentId,
                                                         ComponentProcessingCreateRequest request) {
        log.info("Processing component: {}, type: {}", componentId, request.processType());
        bloodComponentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", "id", componentId));

        ComponentProcessing processing = componentProcessingMapper.toEntity(request);
        processing.setComponentId(componentId);
        processing.setProcessDate(Instant.now());
        processing.setResult(ProcessResultEnum.SUCCESS);
        processing.setBranchId(request.branchId());
        processing = componentProcessingRepository.save(processing);
        return componentProcessingMapper.toResponse(processing);
    }

    @Transactional
    public ComponentLabelResponse createLabel(UUID componentId, ComponentLabelCreateRequest request) {
        log.info("Creating label for component: {}", componentId);
        bloodComponentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", "id", componentId));

        ComponentLabel label = componentLabelMapper.toEntity(request);
        label.setComponentId(componentId);
        label.setPrintedAt(Instant.now());
        label.setBranchId(request.branchId());
        label = componentLabelRepository.save(label);
        return componentLabelMapper.toResponse(label);
    }

    @Transactional
    public PooledComponentResponse createPooledComponent(PooledComponentCreateRequest request) {
        log.info("Creating pooled component, type: {}", request.componentTypeId());
        PooledComponent pooled = pooledComponentMapper.toEntity(request);
        pooled.setPoolNumber(generatePoolNumber());
        pooled.setPreparationDate(Instant.now());
        pooled.setStatus(ComponentStatusEnum.AVAILABLE);
        pooled.setBranchId(request.branchId());
        pooled = pooledComponentRepository.save(pooled);
        return pooledComponentMapper.toResponse(pooled);
    }

    private String generateComponentNumber() {
        return "BC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generatePoolNumber() {
        return "PL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
