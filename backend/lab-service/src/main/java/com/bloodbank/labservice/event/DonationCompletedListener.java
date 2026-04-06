package com.bloodbank.labservice.event;

import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.labservice.config.RabbitMQConfig;
import com.bloodbank.labservice.service.TestOrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DonationCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(DonationCompletedListener.class);

    private final TestOrderService testOrderService;

    public DonationCompletedListener(TestOrderService testOrderService) {
        this.testOrderService = testOrderService;
    }

    @RabbitListener(queues = RabbitMQConfig.DONATION_COMPLETED_QUEUE)
    public void handleDonationCompleted(DonationCompletedEvent event) {
        log.info("Received DonationCompletedEvent: donationId={}, donorId={}, branchId={}",
                event.donationId(), event.donorId(), event.branchId());
        testOrderService.createOrderFromDonation(event);
    }
}
