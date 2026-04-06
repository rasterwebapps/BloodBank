package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.events.UnitExpiringEvent;
import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.entity.BloodComponent;
import com.bloodbank.inventoryservice.entity.BloodUnit;
import com.bloodbank.inventoryservice.event.InventoryEventPublisher;
import com.bloodbank.inventoryservice.repository.BloodComponentRepository;
import com.bloodbank.inventoryservice.repository.BloodUnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ExpirySchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ExpirySchedulerService.class);

    private final BloodUnitRepository bloodUnitRepository;
    private final BloodComponentRepository bloodComponentRepository;
    private final InventoryEventPublisher eventPublisher;

    @Value("${inventory.expiry.warning-days:7}")
    private int warningDays;

    public ExpirySchedulerService(BloodUnitRepository bloodUnitRepository,
                                  BloodComponentRepository bloodComponentRepository,
                                  InventoryEventPublisher eventPublisher) {
        this.bloodUnitRepository = bloodUnitRepository;
        this.bloodComponentRepository = bloodComponentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void processExpiredUnits() {
        log.info("Running expiry check for blood units and components");
        Instant now = Instant.now();

        // Mark expired blood units
        List<BloodUnit> expiredUnits = bloodUnitRepository.findByExpiryDateBeforeAndStatusIn(
                now, List.of(BloodUnitStatusEnum.AVAILABLE, BloodUnitStatusEnum.RESERVED));
        for (BloodUnit unit : expiredUnits) {
            log.info("Marking blood unit {} as expired", unit.getUnitNumber());
            unit.setStatus(BloodUnitStatusEnum.EXPIRED);
            bloodUnitRepository.save(unit);
        }

        // Mark expired components
        List<BloodComponent> expiredComponents = bloodComponentRepository.findByExpiryDateBeforeAndStatusIn(
                now, List.of(ComponentStatusEnum.AVAILABLE, ComponentStatusEnum.RESERVED));
        for (BloodComponent component : expiredComponents) {
            log.info("Marking component {} as expired", component.getComponentNumber());
            component.setStatus(ComponentStatusEnum.EXPIRED);
            bloodComponentRepository.save(component);
        }

        // Publish warning events for units expiring within warning period
        Instant warningThreshold = now.plus(warningDays, ChronoUnit.DAYS);
        List<BloodUnit> expiringUnits = bloodUnitRepository.findByExpiryDateBeforeAndStatusIn(
                warningThreshold, List.of(BloodUnitStatusEnum.AVAILABLE, BloodUnitStatusEnum.RESERVED));

        for (BloodUnit unit : expiringUnits) {
            if (unit.getExpiryDate().isAfter(now)) {
                LocalDate expiryLocalDate = unit.getExpiryDate().atZone(ZoneOffset.UTC).toLocalDate();
                eventPublisher.publishUnitExpiring(new UnitExpiringEvent(
                        unit.getId(), unit.getBranchId(), expiryLocalDate, Instant.now()));
            }
        }

        log.info("Expiry check complete. Expired: {} units, {} components. Expiring soon: {} units",
                expiredUnits.size(), expiredComponents.size(),
                expiringUnits.stream().filter(u -> u.getExpiryDate().isAfter(now)).count());
    }
}
