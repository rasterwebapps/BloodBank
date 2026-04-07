package com.bloodbank.billingservice.event;

import com.bloodbank.common.events.BloodRequestMatchedEvent;
import com.bloodbank.billingservice.config.RabbitMQConfig;
import com.bloodbank.billingservice.service.InvoiceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BloodRequestMatchedListener {

    private static final Logger log = LoggerFactory.getLogger(BloodRequestMatchedListener.class);

    private final InvoiceService invoiceService;

    public BloodRequestMatchedListener(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @RabbitListener(queues = RabbitMQConfig.BLOOD_REQUEST_MATCHED_QUEUE)
    public void handleBloodRequestMatched(BloodRequestMatchedEvent event) {
        log.info("Received BloodRequestMatchedEvent: requestId={}, branchId={}, matchedUnits={}",
                event.requestId(), event.branchId(), event.matchedUnitIds().size());
        invoiceService.createInvoiceFromMatchedRequest(event);
    }
}
