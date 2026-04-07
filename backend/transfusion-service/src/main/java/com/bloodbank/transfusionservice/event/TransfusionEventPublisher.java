package com.bloodbank.transfusionservice.event;

import com.bloodbank.common.events.EventConstants;
import com.bloodbank.common.events.TransfusionCompletedEvent;
import com.bloodbank.common.events.TransfusionReactionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransfusionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransfusionEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public TransfusionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTransfusionCompleted(TransfusionCompletedEvent event) {
        log.info("Publishing TransfusionCompletedEvent for transfusion: {}", event.transfusionId());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME, EventConstants.TRANSFUSION_COMPLETED, event);
    }

    public void publishTransfusionReaction(TransfusionReactionEvent event) {
        log.info("Publishing TransfusionReactionEvent for transfusion: {}", event.transfusionId());
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME, EventConstants.TRANSFUSION_REACTION, event);
    }
}
