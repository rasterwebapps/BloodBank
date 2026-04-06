package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.entity.ColdChainLog;
import com.bloodbank.inventoryservice.entity.DeliveryConfirmation;
import com.bloodbank.inventoryservice.entity.TransportBox;
import com.bloodbank.inventoryservice.entity.TransportRequest;
import com.bloodbank.inventoryservice.enums.TransportBoxStatusEnum;
import com.bloodbank.inventoryservice.enums.TransportStatusEnum;
import com.bloodbank.inventoryservice.mapper.ColdChainLogMapper;
import com.bloodbank.inventoryservice.mapper.DeliveryConfirmationMapper;
import com.bloodbank.inventoryservice.mapper.TransportBoxMapper;
import com.bloodbank.inventoryservice.mapper.TransportRequestMapper;
import com.bloodbank.inventoryservice.repository.ColdChainLogRepository;
import com.bloodbank.inventoryservice.repository.DeliveryConfirmationRepository;
import com.bloodbank.inventoryservice.repository.TransportBoxRepository;
import com.bloodbank.inventoryservice.repository.TransportRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LogisticsService {

    private static final Logger log = LoggerFactory.getLogger(LogisticsService.class);

    private final TransportRequestRepository transportRequestRepository;
    private final ColdChainLogRepository coldChainLogRepository;
    private final TransportBoxRepository transportBoxRepository;
    private final DeliveryConfirmationRepository deliveryConfirmationRepository;
    private final TransportRequestMapper transportRequestMapper;
    private final ColdChainLogMapper coldChainLogMapper;
    private final TransportBoxMapper transportBoxMapper;
    private final DeliveryConfirmationMapper deliveryConfirmationMapper;

    public LogisticsService(TransportRequestRepository transportRequestRepository,
                            ColdChainLogRepository coldChainLogRepository,
                            TransportBoxRepository transportBoxRepository,
                            DeliveryConfirmationRepository deliveryConfirmationRepository,
                            TransportRequestMapper transportRequestMapper,
                            ColdChainLogMapper coldChainLogMapper,
                            TransportBoxMapper transportBoxMapper,
                            DeliveryConfirmationMapper deliveryConfirmationMapper) {
        this.transportRequestRepository = transportRequestRepository;
        this.coldChainLogRepository = coldChainLogRepository;
        this.transportBoxRepository = transportBoxRepository;
        this.deliveryConfirmationRepository = deliveryConfirmationRepository;
        this.transportRequestMapper = transportRequestMapper;
        this.coldChainLogMapper = coldChainLogMapper;
        this.transportBoxMapper = transportBoxMapper;
        this.deliveryConfirmationMapper = deliveryConfirmationMapper;
    }

    @Transactional
    public TransportRequestResponse createTransportRequest(TransportRequestCreateRequest request) {
        log.info("Creating transport request from branch: {}", request.sourceBranchId());
        TransportRequest transportRequest = transportRequestMapper.toEntity(request);
        transportRequest.setRequestNumber(generateRequestNumber());
        transportRequest.setStatus(TransportStatusEnum.REQUESTED);
        transportRequest.setPickupTime(Instant.now());
        transportRequest.setBranchId(request.branchId());
        transportRequest = transportRequestRepository.save(transportRequest);
        return transportRequestMapper.toResponse(transportRequest);
    }

    public TransportRequestResponse getTransportRequest(UUID id) {
        TransportRequest request = transportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransportRequest", "id", id));
        return transportRequestMapper.toResponse(request);
    }

    public List<TransportRequestResponse> getTransportRequests() {
        return transportRequestMapper.toResponseList(transportRequestRepository.findAll());
    }

    @Transactional
    public TransportRequestResponse updateTransportStatus(UUID id, TransportStatusEnum status) {
        log.info("Updating transport request {} status to {}", id, status);
        TransportRequest request = transportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransportRequest", "id", id));

        if (status == TransportStatusEnum.DELIVERED) {
            request.setActualDeliveryTime(Instant.now());
        }
        request.setStatus(status);
        request = transportRequestRepository.save(request);
        return transportRequestMapper.toResponse(request);
    }

    @Transactional
    public ColdChainLogResponse logColdChain(ColdChainLogCreateRequest request) {
        log.info("Logging cold chain data, temperature: {}", request.temperature());
        ColdChainLog logEntry = coldChainLogMapper.toEntity(request);
        logEntry.setRecordedAt(Instant.now());
        logEntry.setBranchId(request.branchId());

        // Determine if temperature is within acceptable range (2-6°C for blood)
        boolean withinRange = request.temperature().doubleValue() >= 2.0
                && request.temperature().doubleValue() <= 6.0;
        logEntry.setWithinRange(withinRange);
        logEntry.setAlertTriggered(!withinRange);

        logEntry = coldChainLogRepository.save(logEntry);
        return coldChainLogMapper.toResponse(logEntry);
    }

    public List<ColdChainLogResponse> getColdChainLogs(UUID transportRequestId) {
        return coldChainLogMapper.toResponseList(
                coldChainLogRepository.findByTransportRequestId(transportRequestId));
    }

    @Transactional
    public TransportBoxResponse createTransportBox(TransportBoxCreateRequest request) {
        log.info("Creating transport box: {}", request.boxCode());
        TransportBox box = transportBoxMapper.toEntity(request);
        box.setStatus(TransportBoxStatusEnum.AVAILABLE);
        box.setBranchId(request.branchId());
        box = transportBoxRepository.save(box);
        return transportBoxMapper.toResponse(box);
    }

    @Transactional
    public TransportBoxResponse updateTransportBoxStatus(UUID id, TransportBoxStatusEnum status) {
        log.info("Updating transport box {} status to {}", id, status);
        TransportBox box = transportBoxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransportBox", "id", id));
        box.setStatus(status);
        box = transportBoxRepository.save(box);
        return transportBoxMapper.toResponse(box);
    }

    @Transactional
    public DeliveryConfirmationResponse confirmDelivery(DeliveryConfirmationCreateRequest request) {
        log.info("Confirming delivery for transport request: {}", request.transportRequestId());

        TransportRequest transportRequest = transportRequestRepository.findById(request.transportRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TransportRequest", "id", request.transportRequestId()));

        if (transportRequest.getStatus() != TransportStatusEnum.DISPATCHED
                && transportRequest.getStatus() != TransportStatusEnum.IN_TRANSIT) {
            throw new BusinessException("Transport request must be dispatched or in transit to confirm delivery",
                    "INVALID_TRANSPORT_STATUS");
        }

        transportRequest.setStatus(TransportStatusEnum.DELIVERED);
        transportRequest.setActualDeliveryTime(Instant.now());
        transportRequestRepository.save(transportRequest);

        DeliveryConfirmation confirmation = deliveryConfirmationMapper.toEntity(request);
        confirmation.setReceivedAt(Instant.now());
        confirmation.setBranchId(request.branchId());
        confirmation = deliveryConfirmationRepository.save(confirmation);
        return deliveryConfirmationMapper.toResponse(confirmation);
    }

    private String generateRequestNumber() {
        return "TRQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
