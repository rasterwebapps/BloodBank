package com.bloodbank.billingservice.config;

import com.bloodbank.common.events.EventConstants;

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

    public static final String BLOOD_REQUEST_MATCHED_QUEUE = "billing.blood.request.matched.queue";

    @Bean
    public TopicExchange bloodbankExchange() {
        return new TopicExchange(EventConstants.EXCHANGE_NAME);
    }

    @Bean
    public Queue bloodRequestMatchedQueue() {
        return new Queue(BLOOD_REQUEST_MATCHED_QUEUE, true);
    }

    @Bean
    public Binding bloodRequestMatchedBinding(Queue bloodRequestMatchedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(bloodRequestMatchedQueue)
                .to(bloodbankExchange)
                .with(EventConstants.BLOOD_REQUEST_MATCHED);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                        MessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }
}
