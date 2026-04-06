package com.bloodbank.donorservice.event;

import com.bloodbank.common.events.EmergencyRequestEvent;
import com.bloodbank.donorservice.repository.DonorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmergencyRequestListener {

    private static final Logger log = LoggerFactory.getLogger(EmergencyRequestListener.class);

    private final DonorRepository donorRepository;

    public EmergencyRequestListener(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    @RabbitListener(queues = "donor.emergency.request.queue")
    public void handleEmergencyRequest(EmergencyRequestEvent event) {
        log.info("Received EmergencyRequestEvent for request: {}, blood group: {}",
                event.requestId(), event.bloodGroup());
        // In production this would flag matching donors for callback notification
    }
}
