package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.inventoryservice.dto.StockTransferCreateRequest;
import com.bloodbank.inventoryservice.dto.StockTransferResponse;
import com.bloodbank.inventoryservice.entity.StockTransfer;
import com.bloodbank.inventoryservice.enums.TransferStatusEnum;
import com.bloodbank.inventoryservice.mapper.StockTransferMapper;
import com.bloodbank.inventoryservice.repository.StockTransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StockTransferService {

    private static final Logger log = LoggerFactory.getLogger(StockTransferService.class);

    private final StockTransferRepository stockTransferRepository;
    private final StockTransferMapper stockTransferMapper;

    public StockTransferService(StockTransferRepository stockTransferRepository,
                                StockTransferMapper stockTransferMapper) {
        this.stockTransferRepository = stockTransferRepository;
        this.stockTransferMapper = stockTransferMapper;
    }

    @Transactional
    public StockTransferResponse createTransfer(StockTransferCreateRequest request) {
        log.info("Creating stock transfer from {} to {}",
                request.sourceBranchId(), request.destinationBranchId());
        StockTransfer transfer = stockTransferMapper.toEntity(request);
        transfer.setTransferNumber(generateTransferNumber());
        transfer.setRequestDate(Instant.now());
        transfer.setStatus(TransferStatusEnum.REQUESTED);
        transfer.setBranchId(request.branchId());
        transfer = stockTransferRepository.save(transfer);
        return stockTransferMapper.toResponse(transfer);
    }

    public StockTransferResponse getById(UUID id) {
        StockTransfer transfer = stockTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        return stockTransferMapper.toResponse(transfer);
    }

    public List<StockTransferResponse> getByBranch(UUID branchId) {
        List<StockTransfer> transfers = stockTransferRepository.findBySourceBranchId(branchId);
        transfers.addAll(stockTransferRepository.findByDestinationBranchId(branchId));
        return stockTransferMapper.toResponseList(transfers);
    }

    public List<StockTransferResponse> getByStatus(TransferStatusEnum status) {
        return stockTransferMapper.toResponseList(stockTransferRepository.findByStatus(status));
    }

    @Transactional
    public StockTransferResponse approveTransfer(UUID id, String approvedBy) {
        log.info("Approving transfer: {}", id);
        StockTransfer transfer = getTransferWithStatus(id, TransferStatusEnum.REQUESTED);
        transfer.setStatus(TransferStatusEnum.APPROVED);
        transfer.setApprovedBy(approvedBy);
        transfer = stockTransferRepository.save(transfer);
        return stockTransferMapper.toResponse(transfer);
    }

    @Transactional
    public StockTransferResponse shipTransfer(UUID id) {
        log.info("Shipping transfer: {}", id);
        StockTransfer transfer = getTransferWithStatus(id, TransferStatusEnum.APPROVED);
        transfer.setStatus(TransferStatusEnum.SHIPPED);
        transfer.setShippedDate(Instant.now());
        transfer = stockTransferRepository.save(transfer);
        return stockTransferMapper.toResponse(transfer);
    }

    @Transactional
    public StockTransferResponse receiveTransfer(UUID id) {
        log.info("Receiving transfer: {}", id);
        StockTransfer transfer = stockTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        if (transfer.getStatus() != TransferStatusEnum.SHIPPED
                && transfer.getStatus() != TransferStatusEnum.IN_TRANSIT) {
            throw new BusinessException("Transfer must be shipped or in transit to receive",
                    "INVALID_TRANSFER_STATUS");
        }
        transfer.setStatus(TransferStatusEnum.RECEIVED);
        transfer.setReceivedDate(Instant.now());
        transfer = stockTransferRepository.save(transfer);
        return stockTransferMapper.toResponse(transfer);
    }

    @Transactional
    public StockTransferResponse cancelTransfer(UUID id) {
        log.info("Cancelling transfer: {}", id);
        StockTransfer transfer = stockTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        if (transfer.getStatus() == TransferStatusEnum.RECEIVED) {
            throw new BusinessException("Cannot cancel a received transfer", "TRANSFER_ALREADY_RECEIVED");
        }
        transfer.setStatus(TransferStatusEnum.CANCELLED);
        transfer = stockTransferRepository.save(transfer);
        return stockTransferMapper.toResponse(transfer);
    }

    private StockTransfer getTransferWithStatus(UUID id, TransferStatusEnum expectedStatus) {
        StockTransfer transfer = stockTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        if (transfer.getStatus() != expectedStatus) {
            throw new BusinessException(
                    String.format("Transfer must be in %s status", expectedStatus),
                    "INVALID_TRANSFER_STATUS");
        }
        return transfer;
    }

    private String generateTransferNumber() {
        return "TR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
