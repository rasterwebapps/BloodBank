package com.bloodbank.billingservice.event;

import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.InvoiceGeneratedEvent;

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

    public void publishInvoiceGenerated(InvoiceGeneratedEvent event) {
        log.info("Publishing InvoiceGeneratedEvent for invoiceId={}, hospitalId={}",
                event.invoiceId(), event.hospitalId());
        rabbitTemplate.convertAndSend(
                EventConstants.EXCHANGE_NAME,
                EventConstants.INVOICE_GENERATED,
                event
        );
    }
}
