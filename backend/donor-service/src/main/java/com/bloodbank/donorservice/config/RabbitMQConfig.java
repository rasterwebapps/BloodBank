package com.bloodbank.donorservice.config;

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

    public static final String EMERGENCY_REQUEST_QUEUE = "donor.emergency.request.queue";

    @Bean
    public TopicExchange bloodbankExchange() {
        return new TopicExchange(EventConstants.EXCHANGE_NAME);
    }

    @Bean
    public Queue emergencyRequestQueue() {
        return new Queue(EMERGENCY_REQUEST_QUEUE, true);
    }

    @Bean
    public Binding emergencyRequestBinding(Queue emergencyRequestQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(emergencyRequestQueue)
                .to(bloodbankExchange)
                .with(EventConstants.EMERGENCY_REQUEST);
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
