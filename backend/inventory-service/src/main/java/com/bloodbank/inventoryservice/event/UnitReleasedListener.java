package com.bloodbank.inventoryservice.event;

import com.bloodbank.common.events.UnitReleasedEvent;
import com.bloodbank.inventoryservice.config.RabbitMQConfig;
import com.bloodbank.inventoryservice.service.BloodUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UnitReleasedListener {

    private static final Logger log = LoggerFactory.getLogger(UnitReleasedListener.class);

    private final BloodUnitService bloodUnitService;

    public UnitReleasedListener(BloodUnitService bloodUnitService) {
        this.bloodUnitService = bloodUnitService;
    }

    @RabbitListener(queues = RabbitMQConfig.UNIT_RELEASED_QUEUE)
    public void handleUnitReleased(UnitReleasedEvent event) {
        log.info("Received UnitReleasedEvent for unit: {}", event.bloodUnitId());
        bloodUnitService.markAsAvailable(event);
    }
}
