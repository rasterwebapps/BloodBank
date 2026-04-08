package com.bloodbank.requestmatchingservice.event;

import com.bloodbank.common.events.StockCriticalEvent;
import com.bloodbank.requestmatchingservice.config.RabbitMQConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockCriticalListener {

    private static final Logger log = LoggerFactory.getLogger(StockCriticalListener.class);

    @RabbitListener(queues = RabbitMQConfig.STOCK_CRITICAL_QUEUE)
    public void handleStockCritical(StockCriticalEvent event) {
        log.info("Received StockCriticalEvent for branch: {}, bloodGroup: {}, currentStock: {}, minimumStock: {}",
                event.branchId(), event.bloodGroup(), event.currentStock(), event.minimumStock());
    }
}
