package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.entity.BloodComponent;
import com.bloodbank.inventoryservice.entity.BloodUnit;
import com.bloodbank.inventoryservice.entity.UnitDisposal;
import com.bloodbank.inventoryservice.entity.UnitReservation;
import com.bloodbank.inventoryservice.event.InventoryEventPublisher;
import com.bloodbank.inventoryservice.mapper.BloodComponentMapper;
import com.bloodbank.inventoryservice.mapper.UnitDisposalMapper;
import com.bloodbank.inventoryservice.mapper.UnitReservationMapper;
import com.bloodbank.inventoryservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final BloodUnitRepository bloodUnitRepository;
    private final BloodComponentRepository bloodComponentRepository;
    private final UnitDisposalRepository unitDisposalRepository;
    private final UnitReservationRepository unitReservationRepository;
    private final BloodComponentMapper bloodComponentMapper;
    private final UnitDisposalMapper unitDisposalMapper;
    private final UnitReservationMapper unitReservationMapper;
    private final InventoryEventPublisher eventPublisher;

    @Value("${inventory.expiry.warning-days:7}")
    private int expiryWarningDays;

    public StockService(BloodUnitRepository bloodUnitRepository,
                        BloodComponentRepository bloodComponentRepository,
                        UnitDisposalRepository unitDisposalRepository,
                        UnitReservationRepository unitReservationRepository,
                        BloodComponentMapper bloodComponentMapper,
                        UnitDisposalMapper unitDisposalMapper,
                        UnitReservationMapper unitReservationMapper,
                        InventoryEventPublisher eventPublisher) {
        this.bloodUnitRepository = bloodUnitRepository;
        this.bloodComponentRepository = bloodComponentRepository;
        this.unitDisposalRepository = unitDisposalRepository;
        this.unitReservationRepository = unitReservationRepository;
        this.bloodComponentMapper = bloodComponentMapper;
        this.unitDisposalMapper = unitDisposalMapper;
        this.unitReservationMapper = unitReservationMapper;
        this.eventPublisher = eventPublisher;
    }

    public List<StockLevelResponse> getStockLevels(UUID branchId, UUID bloodGroupId, UUID componentTypeId) {
        log.info("Getting stock levels for branch: {}", branchId);
        List<StockLevelResponse> levels = new ArrayList<>();

        if (componentTypeId != null && bloodGroupId != null) {
            long count = bloodComponentRepository.countByComponentTypeIdAndBloodGroupIdAndStatus(
                    componentTypeId, bloodGroupId, ComponentStatusEnum.AVAILABLE);
            levels.add(new StockLevelResponse(bloodGroupId, componentTypeId, branchId, count));
        } else if (bloodGroupId != null) {
            long count = bloodUnitRepository.countByBloodGroupIdAndStatus(
                    bloodGroupId, BloodUnitStatusEnum.AVAILABLE);
            levels.add(new StockLevelResponse(bloodGroupId, null, branchId, count));
        }

        return levels;
    }

    @Transactional
    public BloodComponentResponse dispatchComponent(UUID componentTypeId, UUID bloodGroupId) {
        log.info("Dispatching component, type: {}, blood group: {}", componentTypeId, bloodGroupId);
        // FEFO: First Expiry, First Out
        List<BloodComponent> available = bloodComponentRepository
                .findByComponentTypeIdAndStatusOrderByExpiryDateAsc(componentTypeId, ComponentStatusEnum.AVAILABLE);

        BloodComponent component = available.stream()
                .filter(c -> bloodGroupId == null || bloodGroupId.equals(c.getBloodGroupId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "No available components for the requested type and blood group",
                        "STOCK_UNAVAILABLE"));

        component.setStatus(ComponentStatusEnum.ISSUED);
        component = bloodComponentRepository.save(component);
        return bloodComponentMapper.toResponse(component);
    }

    public List<BloodUnitResponse> getExpiringUnits(int days) {
        Instant threshold = Instant.now().plus(days > 0 ? days : expiryWarningDays, ChronoUnit.DAYS);
        List<BloodUnit> expiring = bloodUnitRepository.findByExpiryDateBeforeAndStatusIn(
                threshold, List.of(BloodUnitStatusEnum.AVAILABLE, BloodUnitStatusEnum.RESERVED));
        return expiring.stream()
                .map(unit -> new BloodUnitResponse(
                        unit.getId(), unit.getCollectionId(), unit.getDonorId(),
                        unit.getUnitNumber(), unit.getBloodGroupId(), unit.getRhFactor(),
                        unit.getVolumeMl(), unit.getCollectionDate(), unit.getExpiryDate(),
                        unit.getStatus(), unit.getTtiStatus(), unit.getStorageLocationId(),
                        unit.getBranchId(), unit.getCreatedAt(), unit.getUpdatedAt()))
                .toList();
    }

    @Transactional
    public UnitDisposalResponse disposeUnit(UnitDisposalCreateRequest request) {
        log.info("Disposing unit, reason: {}", request.disposalReason());

        if (request.bloodUnitId() != null) {
            BloodUnit unit = bloodUnitRepository.findById(request.bloodUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("BloodUnit", "id", request.bloodUnitId()));
            unit.setStatus(BloodUnitStatusEnum.DISCARDED);
            bloodUnitRepository.save(unit);
        }

        if (request.componentId() != null) {
            BloodComponent component = bloodComponentRepository.findById(request.componentId())
                    .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", "id", request.componentId()));
            component.setStatus(ComponentStatusEnum.DISCARDED);
            bloodComponentRepository.save(component);
        }

        UnitDisposal disposal = unitDisposalMapper.toEntity(request);
        disposal.setDisposalDate(Instant.now());
        disposal.setBranchId(request.branchId());
        disposal = unitDisposalRepository.save(disposal);
        return unitDisposalMapper.toResponse(disposal);
    }

    @Transactional
    public UnitReservationResponse reserveComponent(UnitReservationCreateRequest request) {
        log.info("Reserving component: {} for: {}", request.componentId(), request.reservedFor());
        BloodComponent component = bloodComponentRepository.findById(request.componentId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", "id", request.componentId()));

        if (component.getStatus() != ComponentStatusEnum.AVAILABLE) {
            throw new BusinessException("Component is not available for reservation", "COMPONENT_NOT_AVAILABLE");
        }

        component.setStatus(ComponentStatusEnum.RESERVED);
        bloodComponentRepository.save(component);

        UnitReservation reservation = unitReservationMapper.toEntity(request);
        reservation.setReservationDate(Instant.now());
        reservation.setBranchId(request.branchId());
        reservation = unitReservationRepository.save(reservation);
        return unitReservationMapper.toResponse(reservation);
    }
}
