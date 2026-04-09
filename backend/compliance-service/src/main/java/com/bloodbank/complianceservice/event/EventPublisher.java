package com.bloodbank.complianceservice.event;

import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.RecallInitiatedEvent;

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

    public void publishRecallInitiated(RecallInitiatedEvent event) {
        log.info("Publishing RecallInitiatedEvent for recallId={}", event.recallId());
        rabbitTemplate.convertAndSend(
                EventConstants.EXCHANGE_NAME,
                EventConstants.RECALL_INITIATED,
                event
        );
    }
}
