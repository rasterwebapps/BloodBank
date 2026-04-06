package com.bloodbank.inventoryservice.event;

import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.inventoryservice.config.RabbitMQConfig;
import com.bloodbank.inventoryservice.service.BloodUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DonationCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(DonationCompletedListener.class);

    private final BloodUnitService bloodUnitService;

    public DonationCompletedListener(BloodUnitService bloodUnitService) {
        this.bloodUnitService = bloodUnitService;
    }

    @RabbitListener(queues = RabbitMQConfig.DONATION_COMPLETED_QUEUE)
    public void handleDonationCompleted(DonationCompletedEvent event) {
        log.info("Received DonationCompletedEvent for donation: {}, donor: {}",
                event.donationId(), event.donorId());
        bloodUnitService.createFromDonation(event);
    }
}
