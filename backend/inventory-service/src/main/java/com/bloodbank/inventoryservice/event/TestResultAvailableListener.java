package com.bloodbank.inventoryservice.event;

import com.bloodbank.common.events.TestResultAvailableEvent;
import com.bloodbank.inventoryservice.config.RabbitMQConfig;
import com.bloodbank.inventoryservice.service.BloodUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TestResultAvailableListener {

    private static final Logger log = LoggerFactory.getLogger(TestResultAvailableListener.class);

    private final BloodUnitService bloodUnitService;

    public TestResultAvailableListener(BloodUnitService bloodUnitService) {
        this.bloodUnitService = bloodUnitService;
    }

    @RabbitListener(queues = RabbitMQConfig.TEST_RESULT_AVAILABLE_QUEUE)
    public void handleTestResultAvailable(TestResultAvailableEvent event) {
        log.info("Received TestResultAvailableEvent for test order: {}", event.testOrderId());
        bloodUnitService.updateTtiStatus(event);
    }
}
