package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.entity.*;
import com.bloodbank.inventoryservice.enums.*;
import com.bloodbank.inventoryservice.mapper.*;
import com.bloodbank.inventoryservice.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogisticsServiceTest {

    @Mock private TransportRequestRepository transportRequestRepository;
    @Mock private ColdChainLogRepository coldChainLogRepository;
    @Mock private TransportBoxRepository transportBoxRepository;
    @Mock private DeliveryConfirmationRepository deliveryConfirmationRepository;
    @Mock private TransportRequestMapper transportRequestMapper;
    @Mock private ColdChainLogMapper coldChainLogMapper;
    @Mock private TransportBoxMapper transportBoxMapper;
    @Mock private DeliveryConfirmationMapper deliveryConfirmationMapper;

    @InjectMocks
    private LogisticsService logisticsService;

    private UUID branchId;

    @BeforeEach
    void setUp() {
        branchId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("createTransportRequest")
    class CreateTransportRequest {

        @Test
        @DisplayName("should create transport request")
        void shouldCreate() {
            TransportRequestCreateRequest request = new TransportRequestCreateRequest(
                    UUID.randomUUID(), UUID.randomUUID(), null, null,
                    TransportTypeEnum.ROUTINE, 5, null, "John", "555-1234",
                    "VEH-001", null, branchId);

            TransportRequest entity = new TransportRequest("TRQ-TEST", UUID.randomUUID(),
                    TransportTypeEnum.ROUTINE, 5);
            entity.setId(UUID.randomUUID());

            TransportRequestResponse response = new TransportRequestResponse(
                    entity.getId(), "TRQ-TEST", UUID.randomUUID(), UUID.randomUUID(),
                    null, null, TransportTypeEnum.ROUTINE, 5, Instant.now(),
                    null, null, "John", "555-1234", "VEH-001",
                    TransportStatusEnum.REQUESTED, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(transportRequestMapper.toEntity(request)).thenReturn(entity);
            when(transportRequestRepository.save(any(TransportRequest.class))).thenReturn(entity);
            when(transportRequestMapper.toResponse(entity)).thenReturn(response);

            TransportRequestResponse result = logisticsService.createTransportRequest(request);

            assertThat(result).isNotNull();
            verify(transportRequestRepository).save(any(TransportRequest.class));
        }
    }

    @Nested
    @DisplayName("getTransportRequest")
    class GetTransportRequest {

        @Test
        @DisplayName("should return when found")
        void shouldReturn() {
            UUID id = UUID.randomUUID();
            TransportRequest entity = new TransportRequest("TRQ-TEST", UUID.randomUUID(),
                    TransportTypeEnum.ROUTINE, 5);
            entity.setId(id);

            TransportRequestResponse response = new TransportRequestResponse(
                    id, "TRQ-TEST", UUID.randomUUID(), null, null, null,
                    TransportTypeEnum.ROUTINE, 5, null, null, null,
                    null, null, null, TransportStatusEnum.REQUESTED, null,
                    branchId, LocalDateTime.now(), LocalDateTime.now());

            when(transportRequestRepository.findById(id)).thenReturn(Optional.of(entity));
            when(transportRequestMapper.toResponse(entity)).thenReturn(response);

            TransportRequestResponse result = logisticsService.getTransportRequest(id);

            assertThat(result.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            UUID id = UUID.randomUUID();
            when(transportRequestRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> logisticsService.getTransportRequest(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateTransportStatus")
    class UpdateTransportStatus {

        @Test
        @DisplayName("should update status")
        void shouldUpdate() {
            UUID id = UUID.randomUUID();
            TransportRequest entity = new TransportRequest("TRQ-TEST", UUID.randomUUID(),
                    TransportTypeEnum.ROUTINE, 5);
            entity.setId(id);

            TransportRequestResponse response = new TransportRequestResponse(
                    id, "TRQ-TEST", UUID.randomUUID(), null, null, null,
                    TransportTypeEnum.ROUTINE, 5, null, null, null,
                    null, null, null, TransportStatusEnum.DISPATCHED, null,
                    branchId, LocalDateTime.now(), LocalDateTime.now());

            when(transportRequestRepository.findById(id)).thenReturn(Optional.of(entity));
            when(transportRequestRepository.save(any(TransportRequest.class))).thenReturn(entity);
            when(transportRequestMapper.toResponse(entity)).thenReturn(response);

            TransportRequestResponse result = logisticsService.updateTransportStatus(id, TransportStatusEnum.DISPATCHED);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should set delivery time when DELIVERED")
        void shouldSetDeliveryTime() {
            UUID id = UUID.randomUUID();
            TransportRequest entity = new TransportRequest("TRQ-TEST", UUID.randomUUID(),
                    TransportTypeEnum.ROUTINE, 5);
            entity.setId(id);

            TransportRequestResponse response = new TransportRequestResponse(
                    id, "TRQ-TEST", UUID.randomUUID(), null, null, null,
                    TransportTypeEnum.ROUTINE, 5, null, null, Instant.now(),
                    null, null, null, TransportStatusEnum.DELIVERED, null,
                    branchId, LocalDateTime.now(), LocalDateTime.now());

            when(transportRequestRepository.findById(id)).thenReturn(Optional.of(entity));
            when(transportRequestRepository.save(any(TransportRequest.class))).thenReturn(entity);
            when(transportRequestMapper.toResponse(entity)).thenReturn(response);

            logisticsService.updateTransportStatus(id, TransportStatusEnum.DELIVERED);

            assertThat(entity.getActualDeliveryTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("logColdChain")
    class LogColdChain {

        @Test
        @DisplayName("should log in-range temperature")
        void shouldLogInRange() {
            ColdChainLogCreateRequest request = new ColdChainLogCreateRequest(
                    UUID.randomUUID(), null, null,
                    BigDecimal.valueOf(4.0), BigDecimal.valueOf(60), "tech1", null, branchId);

            ColdChainLog logEntry = new ColdChainLog(UUID.randomUUID(), BigDecimal.valueOf(4.0),
                    BigDecimal.valueOf(60), "tech1");
            logEntry.setId(UUID.randomUUID());

            ColdChainLogResponse response = new ColdChainLogResponse(
                    logEntry.getId(), UUID.randomUUID(), null, null,
                    BigDecimal.valueOf(4.0), BigDecimal.valueOf(60), Instant.now(),
                    true, false, "tech1", null, branchId, LocalDateTime.now());

            when(coldChainLogMapper.toEntity(request)).thenReturn(logEntry);
            when(coldChainLogRepository.save(any(ColdChainLog.class))).thenReturn(logEntry);
            when(coldChainLogMapper.toResponse(logEntry)).thenReturn(response);

            ColdChainLogResponse result = logisticsService.logColdChain(request);

            assertThat(result).isNotNull();
            assertThat(logEntry.isWithinRange()).isTrue();
            assertThat(logEntry.isAlertTriggered()).isFalse();
        }

        @Test
        @DisplayName("should detect out-of-range temperature")
        void shouldDetectOutOfRange() {
            ColdChainLogCreateRequest request = new ColdChainLogCreateRequest(
                    UUID.randomUUID(), null, null,
                    BigDecimal.valueOf(10.0), null, "tech1", null, branchId);

            ColdChainLog logEntry = new ColdChainLog(UUID.randomUUID(), BigDecimal.valueOf(10.0),
                    null, "tech1");
            logEntry.setId(UUID.randomUUID());

            ColdChainLogResponse response = new ColdChainLogResponse(
                    logEntry.getId(), UUID.randomUUID(), null, null,
                    BigDecimal.valueOf(10.0), null, Instant.now(),
                    false, true, "tech1", null, branchId, LocalDateTime.now());

            when(coldChainLogMapper.toEntity(request)).thenReturn(logEntry);
            when(coldChainLogRepository.save(any(ColdChainLog.class))).thenReturn(logEntry);
            when(coldChainLogMapper.toResponse(logEntry)).thenReturn(response);

            logisticsService.logColdChain(request);

            assertThat(logEntry.isWithinRange()).isFalse();
            assertThat(logEntry.isAlertTriggered()).isTrue();
        }
    }

    @Nested
    @DisplayName("createTransportBox")
    class CreateTransportBox {

        @Test
        @DisplayName("should create transport box")
        void shouldCreate() {
            TransportBoxCreateRequest request = new TransportBoxCreateRequest(
                    "BOX-001", TransportBoxTypeEnum.INSULATED, 10, "2-6°C", branchId);

            TransportBox box = new TransportBox("BOX-001", TransportBoxTypeEnum.INSULATED, 10, "2-6°C");
            box.setId(UUID.randomUUID());

            TransportBoxResponse response = new TransportBoxResponse(
                    box.getId(), "BOX-001", TransportBoxTypeEnum.INSULATED, 10,
                    "2-6°C", TransportBoxStatusEnum.AVAILABLE, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(transportBoxMapper.toEntity(request)).thenReturn(box);
            when(transportBoxRepository.save(any(TransportBox.class))).thenReturn(box);
            when(transportBoxMapper.toResponse(box)).thenReturn(response);

            TransportBoxResponse result = logisticsService.createTransportBox(request);

            assertThat(result).isNotNull();
            assertThat(result.boxCode()).isEqualTo("BOX-001");
        }
    }

    @Nested
    @DisplayName("confirmDelivery")
    class ConfirmDelivery {

        @Test
        @DisplayName("should confirm delivery for dispatched transport")
        void shouldConfirm() {
            UUID transportId = UUID.randomUUID();
            TransportRequest entity = new TransportRequest("TRQ-TEST", UUID.randomUUID(),
                    TransportTypeEnum.ROUTINE, 5);
            entity.setId(transportId);
            entity.setStatus(TransportStatusEnum.DISPATCHED);

            DeliveryConfirmationCreateRequest request = new DeliveryConfirmationCreateRequest(
                    transportId, "Receiver", DeliveryConditionEnum.GOOD,
                    BigDecimal.valueOf(4.0), 5, 0, null, "SIG-001", null, branchId);

            DeliveryConfirmation confirmation = new DeliveryConfirmation(
                    transportId, "Receiver", DeliveryConditionEnum.GOOD, 5);
            confirmation.setId(UUID.randomUUID());

            DeliveryConfirmationResponse response = new DeliveryConfirmationResponse(
                    confirmation.getId(), transportId, "Receiver", Instant.now(),
                    DeliveryConditionEnum.GOOD, BigDecimal.valueOf(4.0), 5, 0,
                    null, "SIG-001", null, branchId, LocalDateTime.now());

            when(transportRequestRepository.findById(transportId)).thenReturn(Optional.of(entity));
            when(transportRequestRepository.save(any(TransportRequest.class))).thenReturn(entity);
            when(deliveryConfirmationMapper.toEntity(request)).thenReturn(confirmation);
            when(deliveryConfirmationRepository.save(any(DeliveryConfirmation.class))).thenReturn(confirmation);
            when(deliveryConfirmationMapper.toResponse(confirmation)).thenReturn(response);

            DeliveryConfirmationResponse result = logisticsService.confirmDelivery(request);

            assertThat(result).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(TransportStatusEnum.DELIVERED);
        }

        @Test
        @DisplayName("should throw when transport not dispatched/in-transit")
        void shouldThrowWrongStatus() {
            UUID transportId = UUID.randomUUID();
            TransportRequest entity = new TransportRequest("TRQ-TEST", UUID.randomUUID(),
                    TransportTypeEnum.ROUTINE, 5);
            entity.setId(transportId);
            entity.setStatus(TransportStatusEnum.REQUESTED);

            DeliveryConfirmationCreateRequest request = new DeliveryConfirmationCreateRequest(
                    transportId, "Receiver", DeliveryConditionEnum.GOOD,
                    BigDecimal.valueOf(4.0), 5, 0, null, null, null, branchId);

            when(transportRequestRepository.findById(transportId)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> logisticsService.confirmDelivery(request))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
