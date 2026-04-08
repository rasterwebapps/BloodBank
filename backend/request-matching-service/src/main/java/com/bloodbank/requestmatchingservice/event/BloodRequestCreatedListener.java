package com.bloodbank.requestmatchingservice.event;

import com.bloodbank.common.events.BloodRequestCreatedEvent;
import com.bloodbank.requestmatchingservice.config.RabbitMQConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BloodRequestCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(BloodRequestCreatedListener.class);

    @RabbitListener(queues = RabbitMQConfig.BLOOD_REQUEST_CREATED_QUEUE)
    public void handleBloodRequestCreated(BloodRequestCreatedEvent event) {
        log.info("Received BloodRequestCreatedEvent for request: {}, hospital: {}",
                event.requestId(), event.hospitalId());
    }
}
