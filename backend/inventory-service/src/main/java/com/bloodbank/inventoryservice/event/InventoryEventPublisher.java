package com.bloodbank.inventoryservice.event;

import com.bloodbank.common.events.BloodStockUpdatedEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.StockCriticalEvent;
import com.bloodbank.common.events.UnitExpiringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public InventoryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBloodStockUpdated(BloodStockUpdatedEvent event) {
        log.info("Publishing BloodStockUpdatedEvent for branch: {}, blood group: {}",
                event.branchId(), event.bloodGroup());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME,
                EventConstants.BLOOD_STOCK_UPDATED, event);
    }

    public void publishStockCritical(StockCriticalEvent event) {
        log.info("Publishing StockCriticalEvent for branch: {}, blood group: {}",
                event.branchId(), event.bloodGroup());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME,
                EventConstants.STOCK_CRITICAL, event);
    }

    public void publishUnitExpiring(UnitExpiringEvent event) {
        log.info("Publishing UnitExpiringEvent for unit: {}", event.bloodUnitId());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME,
                EventConstants.UNIT_EXPIRING, event);
    }
}
