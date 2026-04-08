package com.bloodbank.requestmatchingservice.event;

import com.bloodbank.common.events.BloodStockUpdatedEvent;
import com.bloodbank.requestmatchingservice.config.RabbitMQConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BloodStockUpdatedListener {

    private static final Logger log = LoggerFactory.getLogger(BloodStockUpdatedListener.class);

    @RabbitListener(queues = RabbitMQConfig.BLOOD_STOCK_UPDATED_QUEUE)
    public void handleBloodStockUpdated(BloodStockUpdatedEvent event) {
        log.info("Received BloodStockUpdatedEvent for branch: {}, bloodGroup: {}, quantity: {}",
                event.branchId(), event.bloodGroup(), event.quantity());
    }
}
