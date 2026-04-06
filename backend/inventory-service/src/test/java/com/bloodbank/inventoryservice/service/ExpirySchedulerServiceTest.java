package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.events.UnitExpiringEvent;
import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.entity.BloodComponent;
import com.bloodbank.inventoryservice.entity.BloodUnit;
import com.bloodbank.inventoryservice.event.InventoryEventPublisher;
import com.bloodbank.inventoryservice.repository.BloodComponentRepository;
import com.bloodbank.inventoryservice.repository.BloodUnitRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpirySchedulerServiceTest {

    @Mock private BloodUnitRepository bloodUnitRepository;
    @Mock private BloodComponentRepository bloodComponentRepository;
    @Mock private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private ExpirySchedulerService expirySchedulerService;

    @Test
    @DisplayName("should mark expired blood units")
    void shouldMarkExpiredUnits() {
        BloodUnit expired = new BloodUnit(UUID.randomUUID(), UUID.randomUUID(), "BU-EXP",
                UUID.randomUUID(), "POSITIVE", 450, Instant.now().minus(50, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.DAYS));
        expired.setId(UUID.randomUUID());
        expired.setBranchId(UUID.randomUUID());
        expired.setStatus(BloodUnitStatusEnum.AVAILABLE);

        // First call: expired units, Second call: expiring-soon units
        when(bloodUnitRepository.findByExpiryDateBeforeAndStatusIn(any(Instant.class), anyList()))
                .thenReturn(List.of(expired))
                .thenReturn(List.of());
        when(bloodUnitRepository.save(any(BloodUnit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bloodComponentRepository.findByExpiryDateBeforeAndStatusIn(any(Instant.class), anyList()))
                .thenReturn(List.of());

        expirySchedulerService.processExpiredUnits();

        assertThat(expired.getStatus()).isEqualTo(BloodUnitStatusEnum.EXPIRED);
        verify(bloodUnitRepository).save(expired);
    }

    @Test
    @DisplayName("should mark expired components")
    void shouldMarkExpiredComponents() {
        BloodComponent expired = new BloodComponent(UUID.randomUUID(), UUID.randomUUID(), "BC-EXP",
                UUID.randomUUID(), 200, Instant.now().minus(1, ChronoUnit.DAYS));
        expired.setId(UUID.randomUUID());
        expired.setStatus(ComponentStatusEnum.AVAILABLE);

        when(bloodUnitRepository.findByExpiryDateBeforeAndStatusIn(any(Instant.class), anyList()))
                .thenReturn(List.of())
                .thenReturn(List.of());
        when(bloodComponentRepository.findByExpiryDateBeforeAndStatusIn(any(Instant.class), anyList()))
                .thenReturn(List.of(expired));
        when(bloodComponentRepository.save(any(BloodComponent.class))).thenAnswer(inv -> inv.getArgument(0));

        expirySchedulerService.processExpiredUnits();

        assertThat(expired.getStatus()).isEqualTo(ComponentStatusEnum.EXPIRED);
        verify(bloodComponentRepository).save(expired);
    }

    @Test
    @DisplayName("should publish UnitExpiringEvent for units expiring soon")
    void shouldPublishExpiringEvent() {
        BloodUnit expiringSoon = new BloodUnit(UUID.randomUUID(), UUID.randomUUID(), "BU-SOON",
                UUID.randomUUID(), "POSITIVE", 450, Instant.now().minus(30, ChronoUnit.DAYS),
                Instant.now().plus(3, ChronoUnit.DAYS));
        expiringSoon.setId(UUID.randomUUID());
        expiringSoon.setBranchId(UUID.randomUUID());
        expiringSoon.setStatus(BloodUnitStatusEnum.AVAILABLE);

        // First call (expired): none; Second call (expiring within warning): the unit
        when(bloodUnitRepository.findByExpiryDateBeforeAndStatusIn(any(Instant.class), anyList()))
                .thenReturn(List.of())
                .thenReturn(List.of(expiringSoon));
        when(bloodComponentRepository.findByExpiryDateBeforeAndStatusIn(any(Instant.class), anyList()))
                .thenReturn(List.of());

        expirySchedulerService.processExpiredUnits();

        verify(eventPublisher).publishUnitExpiring(any(UnitExpiringEvent.class));
    }
}
