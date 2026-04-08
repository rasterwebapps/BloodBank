package com.bloodbank.requestmatchingservice.event;

import com.bloodbank.common.events.BloodRequestMatchedEvent;
import com.bloodbank.common.events.EmergencyRequestEvent;
import com.bloodbank.common.events.EventConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RequestMatchingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RequestMatchingEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RequestMatchingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBloodRequestMatched(BloodRequestMatchedEvent event) {
        log.info("Publishing BloodRequestMatchedEvent for request: {}", event.requestId());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME, EventConstants.BLOOD_REQUEST_MATCHED, event);
    }

    public void publishEmergencyRequest(EmergencyRequestEvent event) {
        log.info("Publishing EmergencyRequestEvent for request: {}", event.requestId());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME, EventConstants.EMERGENCY_REQUEST, event);
    }
}
