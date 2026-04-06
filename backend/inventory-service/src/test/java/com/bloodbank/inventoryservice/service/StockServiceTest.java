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
import com.bloodbank.inventoryservice.enums.DisposalReasonEnum;
import com.bloodbank.inventoryservice.enums.ReservationStatusEnum;
import com.bloodbank.inventoryservice.enums.TtiStatusEnum;
import com.bloodbank.inventoryservice.event.InventoryEventPublisher;
import com.bloodbank.inventoryservice.mapper.BloodComponentMapper;
import com.bloodbank.inventoryservice.mapper.UnitDisposalMapper;
import com.bloodbank.inventoryservice.mapper.UnitReservationMapper;
import com.bloodbank.inventoryservice.repository.BloodComponentRepository;
import com.bloodbank.inventoryservice.repository.BloodUnitRepository;
import com.bloodbank.inventoryservice.repository.UnitDisposalRepository;
import com.bloodbank.inventoryservice.repository.UnitReservationRepository;

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
class StockServiceTest {

    @Mock private BloodUnitRepository bloodUnitRepository;
    @Mock private BloodComponentRepository bloodComponentRepository;
    @Mock private UnitDisposalRepository unitDisposalRepository;
    @Mock private UnitReservationRepository unitReservationRepository;
    @Mock private BloodComponentMapper bloodComponentMapper;
    @Mock private UnitDisposalMapper unitDisposalMapper;
    @Mock private UnitReservationMapper unitReservationMapper;
    @Mock private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private StockService stockService;

    private UUID branchId;
    private UUID bloodGroupId;
    private UUID componentTypeId;

    @BeforeEach
    void setUp() {
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("getStockLevels")
    class GetStockLevels {

        @Test
        @DisplayName("should return levels by component type and blood group")
        void shouldReturnByComponentTypeAndBloodGroup() {
            when(bloodComponentRepository.countByComponentTypeIdAndBloodGroupIdAndStatus(
                    componentTypeId, bloodGroupId, ComponentStatusEnum.AVAILABLE)).thenReturn(10L);

            List<StockLevelResponse> result = stockService.getStockLevels(branchId, bloodGroupId, componentTypeId);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().availableCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("should return levels by blood group only")
        void shouldReturnByBloodGroupOnly() {
            when(bloodUnitRepository.countByBloodGroupIdAndStatus(
                    bloodGroupId, BloodUnitStatusEnum.AVAILABLE)).thenReturn(5L);

            List<StockLevelResponse> result = stockService.getStockLevels(branchId, bloodGroupId, null);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().availableCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("should return empty when no filters")
        void shouldReturnEmpty() {
            List<StockLevelResponse> result = stockService.getStockLevels(branchId, null, null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("dispatchComponent — FEFO")
    class DispatchComponent {

        @Test
        @DisplayName("should dispatch oldest-expiring component first (FEFO)")
        void shouldDispatchOldestExpiringFirst() {
            Instant now = Instant.now();
            BloodComponent earlyExpiry = new BloodComponent(UUID.randomUUID(), componentTypeId,
                    "BC-EARLY", bloodGroupId, 200, now.plus(5, ChronoUnit.DAYS));
            earlyExpiry.setId(UUID.randomUUID());
            earlyExpiry.setStatus(ComponentStatusEnum.AVAILABLE);

            BloodComponent lateExpiry = new BloodComponent(UUID.randomUUID(), componentTypeId,
                    "BC-LATE", bloodGroupId, 200, now.plus(30, ChronoUnit.DAYS));
            lateExpiry.setId(UUID.randomUUID());
            lateExpiry.setStatus(ComponentStatusEnum.AVAILABLE);

            // Returned ordered by expiryDate ascending — early first
            when(bloodComponentRepository.findByComponentTypeIdAndStatusOrderByExpiryDateAsc(
                    componentTypeId, ComponentStatusEnum.AVAILABLE))
                    .thenReturn(List.of(earlyExpiry, lateExpiry));
            when(bloodComponentRepository.save(any(BloodComponent.class))).thenAnswer(inv -> inv.getArgument(0));

            BloodComponentResponse expectedResponse = new BloodComponentResponse(
                    earlyExpiry.getId(), earlyExpiry.getBloodUnitId(), componentTypeId,
                    "BC-EARLY", bloodGroupId, 200, null, Instant.now(),
                    now.plus(5, ChronoUnit.DAYS), ComponentStatusEnum.ISSUED,
                    null, false, false, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(bloodComponentMapper.toResponse(any(BloodComponent.class))).thenReturn(expectedResponse);

            BloodComponentResponse result = stockService.dispatchComponent(componentTypeId, bloodGroupId);

            assertThat(result.componentNumber()).isEqualTo("BC-EARLY");
            assertThat(earlyExpiry.getStatus()).isEqualTo(ComponentStatusEnum.ISSUED);
            // Ensure the late one was NOT dispatched
            assertThat(lateExpiry.getStatus()).isEqualTo(ComponentStatusEnum.AVAILABLE);
        }

        @Test
        @DisplayName("should throw when no stock available")
        void shouldThrowWhenNoStock() {
            when(bloodComponentRepository.findByComponentTypeIdAndStatusOrderByExpiryDateAsc(
                    componentTypeId, ComponentStatusEnum.AVAILABLE))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> stockService.dispatchComponent(componentTypeId, bloodGroupId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No available components");
        }

        @Test
        @DisplayName("should filter by blood group when dispatching")
        void shouldFilterByBloodGroup() {
            UUID otherBloodGroup = UUID.randomUUID();
            BloodComponent wrongGroup = new BloodComponent(UUID.randomUUID(), componentTypeId,
                    "BC-WRONG", otherBloodGroup, 200, Instant.now().plus(5, ChronoUnit.DAYS));
            wrongGroup.setId(UUID.randomUUID());
            wrongGroup.setStatus(ComponentStatusEnum.AVAILABLE);

            BloodComponent rightGroup = new BloodComponent(UUID.randomUUID(), componentTypeId,
                    "BC-RIGHT", bloodGroupId, 200, Instant.now().plus(10, ChronoUnit.DAYS));
            rightGroup.setId(UUID.randomUUID());
            rightGroup.setStatus(ComponentStatusEnum.AVAILABLE);

            when(bloodComponentRepository.findByComponentTypeIdAndStatusOrderByExpiryDateAsc(
                    componentTypeId, ComponentStatusEnum.AVAILABLE))
                    .thenReturn(List.of(wrongGroup, rightGroup));
            when(bloodComponentRepository.save(any(BloodComponent.class))).thenAnswer(inv -> inv.getArgument(0));

            BloodComponentResponse expectedResponse = new BloodComponentResponse(
                    rightGroup.getId(), rightGroup.getBloodUnitId(), componentTypeId,
                    "BC-RIGHT", bloodGroupId, 200, null, Instant.now(),
                    Instant.now().plus(10, ChronoUnit.DAYS), ComponentStatusEnum.ISSUED,
                    null, false, false, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(bloodComponentMapper.toResponse(any(BloodComponent.class))).thenReturn(expectedResponse);

            BloodComponentResponse result = stockService.dispatchComponent(componentTypeId, bloodGroupId);

            assertThat(result.componentNumber()).isEqualTo("BC-RIGHT");
            assertThat(rightGroup.getStatus()).isEqualTo(ComponentStatusEnum.ISSUED);
            assertThat(wrongGroup.getStatus()).isEqualTo(ComponentStatusEnum.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("getExpiringUnits")
    class GetExpiringUnits {

        @Test
        @DisplayName("should return units expiring within threshold")
        void shouldReturnExpiring() {
            BloodUnit unit = new BloodUnit(UUID.randomUUID(), UUID.randomUUID(), "BU-TEST",
                    bloodGroupId, "POSITIVE", 450, Instant.now(),
                    Instant.now().plus(3, ChronoUnit.DAYS));
            unit.setId(UUID.randomUUID());
            unit.setBranchId(branchId);

            when(bloodUnitRepository.findByExpiryDateBeforeAndStatusIn(any(Instant.class), anyList()))
                    .thenReturn(List.of(unit));

            List<BloodUnitResponse> result = stockService.getExpiringUnits(7);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("disposeUnit")
    class DisposeUnit {

        @Test
        @DisplayName("should dispose blood unit")
        void shouldDisposeBloodUnit() {
            UUID bloodUnitId = UUID.randomUUID();
            BloodUnit unit = new BloodUnit(UUID.randomUUID(), UUID.randomUUID(), "BU-TEST",
                    bloodGroupId, "POSITIVE", 450, Instant.now(),
                    Instant.now().plus(35, ChronoUnit.DAYS));
            unit.setId(bloodUnitId);

            UnitDisposalCreateRequest request = new UnitDisposalCreateRequest(
                    bloodUnitId, null, DisposalReasonEnum.EXPIRED, "tech1", "mgr1", "Expired", branchId);

            UnitDisposal disposal = new UnitDisposal(bloodUnitId, null, DisposalReasonEnum.EXPIRED, "tech1", "mgr1");
            disposal.setId(UUID.randomUUID());

            UnitDisposalResponse response = new UnitDisposalResponse(
                    disposal.getId(), bloodUnitId, null, DisposalReasonEnum.EXPIRED,
                    Instant.now(), "tech1", "mgr1", "Expired", branchId, LocalDateTime.now());

            when(bloodUnitRepository.findById(bloodUnitId)).thenReturn(Optional.of(unit));
            when(bloodUnitRepository.save(any(BloodUnit.class))).thenReturn(unit);
            when(unitDisposalMapper.toEntity(request)).thenReturn(disposal);
            when(unitDisposalRepository.save(any(UnitDisposal.class))).thenReturn(disposal);
            when(unitDisposalMapper.toResponse(disposal)).thenReturn(response);

            UnitDisposalResponse result = stockService.disposeUnit(request);

            assertThat(result).isNotNull();
            assertThat(unit.getStatus()).isEqualTo(BloodUnitStatusEnum.DISCARDED);
        }
    }

    @Nested
    @DisplayName("reserveComponent")
    class ReserveComponent {

        @Test
        @DisplayName("should reserve available component")
        void shouldReserve() {
            UUID compId = UUID.randomUUID();
            BloodComponent component = new BloodComponent(UUID.randomUUID(), componentTypeId,
                    "BC-TEST", bloodGroupId, 200, Instant.now().plus(30, ChronoUnit.DAYS));
            component.setId(compId);
            component.setStatus(ComponentStatusEnum.AVAILABLE);

            UnitReservationCreateRequest request = new UnitReservationCreateRequest(
                    compId, "Patient X", Instant.now().plus(1, ChronoUnit.HOURS), "tech1", null, branchId);

            UnitReservation reservation = new UnitReservation(compId, "Patient X",
                    Instant.now().plus(1, ChronoUnit.HOURS), "tech1");
            reservation.setId(UUID.randomUUID());

            UnitReservationResponse response = new UnitReservationResponse(
                    reservation.getId(), compId, "Patient X", Instant.now(),
                    Instant.now().plus(1, ChronoUnit.HOURS), ReservationStatusEnum.ACTIVE,
                    "tech1", null, branchId, LocalDateTime.now());

            when(bloodComponentRepository.findById(compId)).thenReturn(Optional.of(component));
            when(bloodComponentRepository.save(any(BloodComponent.class))).thenReturn(component);
            when(unitReservationMapper.toEntity(request)).thenReturn(reservation);
            when(unitReservationRepository.save(any(UnitReservation.class))).thenReturn(reservation);
            when(unitReservationMapper.toResponse(reservation)).thenReturn(response);

            UnitReservationResponse result = stockService.reserveComponent(request);

            assertThat(result).isNotNull();
            assertThat(component.getStatus()).isEqualTo(ComponentStatusEnum.RESERVED);
        }

        @Test
        @DisplayName("should throw when component not available")
        void shouldThrowWhenNotAvailable() {
            UUID compId = UUID.randomUUID();
            BloodComponent component = new BloodComponent(UUID.randomUUID(), componentTypeId,
                    "BC-TEST", bloodGroupId, 200, Instant.now().plus(30, ChronoUnit.DAYS));
            component.setId(compId);
            component.setStatus(ComponentStatusEnum.RESERVED);

            UnitReservationCreateRequest request = new UnitReservationCreateRequest(
                    compId, "Patient X", Instant.now().plus(1, ChronoUnit.HOURS), "tech1", null, branchId);

            when(bloodComponentRepository.findById(compId)).thenReturn(Optional.of(component));

            assertThatThrownBy(() -> stockService.reserveComponent(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("not available");
        }
    }
}
