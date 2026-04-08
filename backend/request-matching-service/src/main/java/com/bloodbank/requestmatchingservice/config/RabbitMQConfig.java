package com.bloodbank.requestmatchingservice.config;

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

    public static final String BLOOD_STOCK_UPDATED_QUEUE = "request-matching.blood.stock.updated.queue";
    public static final String BLOOD_REQUEST_CREATED_QUEUE = "request-matching.blood.request.created.queue";
    public static final String STOCK_CRITICAL_QUEUE = "request-matching.stock.critical.queue";

    @Bean
    public TopicExchange bloodbankExchange() {
        return new TopicExchange(EventConstants.EXCHANGE_NAME);
    }

    @Bean
    public Queue bloodStockUpdatedQueue() {
        return new Queue(BLOOD_STOCK_UPDATED_QUEUE, true);
    }

    @Bean
    public Queue bloodRequestCreatedQueue() {
        return new Queue(BLOOD_REQUEST_CREATED_QUEUE, true);
    }

    @Bean
    public Queue stockCriticalQueue() {
        return new Queue(STOCK_CRITICAL_QUEUE, true);
    }

    @Bean
    public Binding bloodStockUpdatedBinding(Queue bloodStockUpdatedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(bloodStockUpdatedQueue)
                .to(bloodbankExchange)
                .with(EventConstants.BLOOD_STOCK_UPDATED);
    }

    @Bean
    public Binding bloodRequestCreatedBinding(Queue bloodRequestCreatedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(bloodRequestCreatedQueue)
                .to(bloodbankExchange)
                .with(EventConstants.BLOOD_REQUEST_CREATED);
    }

    @Bean
    public Binding stockCriticalBinding(Queue stockCriticalQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(stockCriticalQueue)
                .to(bloodbankExchange)
                .with(EventConstants.STOCK_CRITICAL);
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
