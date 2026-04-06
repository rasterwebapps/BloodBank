package com.bloodbank.labservice.event;

import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.TestResultAvailableEvent;
import com.bloodbank.common.events.UnitReleasedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTestResultAvailable(TestResultAvailableEvent event) {
        log.info("Publishing TestResultAvailableEvent for testOrderId={}, bloodUnitId={}",
                event.testOrderId(), event.bloodUnitId());
        rabbitTemplate.convertAndSend(
                EventConstants.EXCHANGE_NAME,
                EventConstants.TEST_RESULT_AVAILABLE,
                event
        );
    }

    public void publishUnitReleased(UnitReleasedEvent event) {
        log.info("Publishing UnitReleasedEvent for bloodUnitId={}, branchId={}",
                event.bloodUnitId(), event.branchId());
        rabbitTemplate.convertAndSend(
                EventConstants.EXCHANGE_NAME,
                EventConstants.UNIT_RELEASED,
                event
        );
    }
}
