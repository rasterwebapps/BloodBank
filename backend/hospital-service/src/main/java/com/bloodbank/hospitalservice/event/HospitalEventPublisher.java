package com.bloodbank.hospitalservice.event;

import com.bloodbank.common.events.BloodRequestCreatedEvent;
import com.bloodbank.common.events.EventConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class HospitalEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(HospitalEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public HospitalEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBloodRequestCreated(BloodRequestCreatedEvent event) {
        log.info("Publishing BloodRequestCreatedEvent for request: {}", event.requestId());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME, EventConstants.BLOOD_REQUEST_CREATED, event);
    }
}
