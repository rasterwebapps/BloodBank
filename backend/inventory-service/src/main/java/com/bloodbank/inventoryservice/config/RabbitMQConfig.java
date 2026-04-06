package com.bloodbank.inventoryservice.config;

import com.bloodbank.common.events.EventConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DONATION_COMPLETED_QUEUE = "inventory.donation.completed.queue";
    public static final String TEST_RESULT_AVAILABLE_QUEUE = "inventory.test.result.available.queue";
    public static final String UNIT_RELEASED_QUEUE = "inventory.unit.released.queue";

    @Bean
    public TopicExchange bloodbankExchange() {
        return new TopicExchange(EventConstants.EXCHANGE_NAME);
    }

    @Bean
    public Queue donationCompletedQueue() {
        return new Queue(DONATION_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue testResultAvailableQueue() {
        return new Queue(TEST_RESULT_AVAILABLE_QUEUE, true);
    }

    @Bean
    public Queue unitReleasedQueue() {
        return new Queue(UNIT_RELEASED_QUEUE, true);
    }

    @Bean
    public Binding donationCompletedBinding(Queue donationCompletedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(donationCompletedQueue)
                .to(bloodbankExchange)
                .with(EventConstants.DONATION_COMPLETED);
    }

    @Bean
    public Binding testResultAvailableBinding(Queue testResultAvailableQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(testResultAvailableQueue)
                .to(bloodbankExchange)
                .with(EventConstants.TEST_RESULT_AVAILABLE);
    }

    @Bean
    public Binding unitReleasedBinding(Queue unitReleasedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(unitReleasedQueue)
                .to(bloodbankExchange)
                .with(EventConstants.UNIT_RELEASED);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }
}
