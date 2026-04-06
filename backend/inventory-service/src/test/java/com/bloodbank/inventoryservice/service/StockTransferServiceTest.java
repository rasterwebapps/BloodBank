package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.inventoryservice.dto.StockTransferCreateRequest;
import com.bloodbank.inventoryservice.dto.StockTransferResponse;
import com.bloodbank.inventoryservice.entity.StockTransfer;
import com.bloodbank.inventoryservice.enums.TransferStatusEnum;
import com.bloodbank.inventoryservice.mapper.StockTransferMapper;
import com.bloodbank.inventoryservice.repository.StockTransferRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockTransferServiceTest {

    @Mock private StockTransferRepository stockTransferRepository;
    @Mock private StockTransferMapper stockTransferMapper;

    @InjectMocks
    private StockTransferService stockTransferService;

    private UUID transferId;
    private UUID branchId;
    private UUID sourceBranchId;
    private UUID destBranchId;
    private StockTransfer transfer;
    private StockTransferResponse transferResponse;

    @BeforeEach
    void setUp() {
        transferId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        sourceBranchId = UUID.randomUUID();
        destBranchId = UUID.randomUUID();

        transfer = new StockTransfer(sourceBranchId, destBranchId, "TR-ABCD1234", "tech1");
        transfer.setId(transferId);
        transfer.setBranchId(branchId);

        transferResponse = new StockTransferResponse(
                transferId, "TR-ABCD1234", sourceBranchId, destBranchId,
                UUID.randomUUID(), null, Instant.now(), null, null,
                TransferStatusEnum.REQUESTED, "tech1", null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("createTransfer")
    class CreateTransfer {

        @Test
        @DisplayName("should create transfer successfully")
        void shouldCreate() {
            StockTransferCreateRequest request = new StockTransferCreateRequest(
                    sourceBranchId, destBranchId, UUID.randomUUID(), null, "tech1", null, branchId);

            when(stockTransferMapper.toEntity(request)).thenReturn(transfer);
            when(stockTransferRepository.save(any(StockTransfer.class))).thenReturn(transfer);
            when(stockTransferMapper.toResponse(transfer)).thenReturn(transferResponse);

            StockTransferResponse result = stockTransferService.createTransfer(request);

            assertThat(result).isNotNull();
            verify(stockTransferRepository).save(any(StockTransfer.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return transfer when found")
        void shouldReturn() {
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
            when(stockTransferMapper.toResponse(transfer)).thenReturn(transferResponse);

            StockTransferResponse result = stockTransferService.getById(transferId);

            assertThat(result.id()).isEqualTo(transferId);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockTransferService.getById(transferId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("approveTransfer")
    class ApproveTransfer {

        @Test
        @DisplayName("should approve REQUESTED transfer")
        void shouldApprove() {
            transfer.setStatus(TransferStatusEnum.REQUESTED);
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
            when(stockTransferRepository.save(any(StockTransfer.class))).thenReturn(transfer);
            when(stockTransferMapper.toResponse(transfer)).thenReturn(transferResponse);

            stockTransferService.approveTransfer(transferId, "mgr1");

            assertThat(transfer.getStatus()).isEqualTo(TransferStatusEnum.APPROVED);
            assertThat(transfer.getApprovedBy()).isEqualTo("mgr1");
        }

        @Test
        @DisplayName("should throw when not in REQUESTED status")
        void shouldThrowWrongStatus() {
            transfer.setStatus(TransferStatusEnum.SHIPPED);
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));

            assertThatThrownBy(() -> stockTransferService.approveTransfer(transferId, "mgr1"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("shipTransfer")
    class ShipTransfer {

        @Test
        @DisplayName("should ship APPROVED transfer")
        void shouldShip() {
            transfer.setStatus(TransferStatusEnum.APPROVED);
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
            when(stockTransferRepository.save(any(StockTransfer.class))).thenReturn(transfer);
            when(stockTransferMapper.toResponse(transfer)).thenReturn(transferResponse);

            stockTransferService.shipTransfer(transferId);

            assertThat(transfer.getStatus()).isEqualTo(TransferStatusEnum.SHIPPED);
            assertThat(transfer.getShippedDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("receiveTransfer")
    class ReceiveTransfer {

        @Test
        @DisplayName("should receive SHIPPED transfer")
        void shouldReceive() {
            transfer.setStatus(TransferStatusEnum.SHIPPED);
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
            when(stockTransferRepository.save(any(StockTransfer.class))).thenReturn(transfer);
            when(stockTransferMapper.toResponse(transfer)).thenReturn(transferResponse);

            stockTransferService.receiveTransfer(transferId);

            assertThat(transfer.getStatus()).isEqualTo(TransferStatusEnum.RECEIVED);
            assertThat(transfer.getReceivedDate()).isNotNull();
        }

        @Test
        @DisplayName("should throw when not shipped/in-transit")
        void shouldThrowWrongStatus() {
            transfer.setStatus(TransferStatusEnum.REQUESTED);
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));

            assertThatThrownBy(() -> stockTransferService.receiveTransfer(transferId))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("cancelTransfer")
    class CancelTransfer {

        @Test
        @DisplayName("should cancel non-received transfer")
        void shouldCancel() {
            transfer.setStatus(TransferStatusEnum.REQUESTED);
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
            when(stockTransferRepository.save(any(StockTransfer.class))).thenReturn(transfer);
            when(stockTransferMapper.toResponse(transfer)).thenReturn(transferResponse);

            stockTransferService.cancelTransfer(transferId);

            assertThat(transfer.getStatus()).isEqualTo(TransferStatusEnum.CANCELLED);
        }

        @Test
        @DisplayName("should throw when already received")
        void shouldThrowAlreadyReceived() {
            transfer.setStatus(TransferStatusEnum.RECEIVED);
            when(stockTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));

            assertThatThrownBy(() -> stockTransferService.cancelTransfer(transferId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot cancel");
        }
    }
}
