package com.bloodbank.donorservice.event;

import com.bloodbank.common.events.CampCompletedEvent;
import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.events.EventConstants;

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

    public void publishDonationCompleted(DonationCompletedEvent event) {
        log.info("Publishing DonationCompletedEvent for donation: {}", event.donationId());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME, EventConstants.DONATION_COMPLETED, event);
    }

    public void publishCampCompleted(CampCompletedEvent event) {
        log.info("Publishing CampCompletedEvent for camp: {}", event.campId());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME, EventConstants.CAMP_COMPLETED, event);
    }
}
