package com.bloodbank.integration.support;

import com.bloodbank.common.events.EventConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for cross-service clinical workflow integration tests.
 * Provides shared RabbitMQ Testcontainer infrastructure, exchange/queue setup,
 * and helper methods for publishing and consuming events.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractWorkflowIntegrationTest {

    private final List<String> declaredQueueNames = new ArrayList<>();

    @Container
    protected static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    protected RabbitTemplate rabbitTemplate;
    protected RabbitAdmin rabbitAdmin;
    protected CachingConnectionFactory connectionFactory;
    protected ObjectMapper objectMapper;

    @BeforeAll
    void setUpRabbitMQ() {
        connectionFactory = new CachingConnectionFactory(RABBITMQ.getHost(), RABBITMQ.getAmqpPort());
        connectionFactory.setUsername(RABBITMQ.getAdminUsername());
        connectionFactory.setPassword(RABBITMQ.getAdminPassword());

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter(objectMapper);

        rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setReceiveTimeout(Duration.ofSeconds(5).toMillis());

        rabbitAdmin = new RabbitAdmin(connectionFactory);

        // Declare the shared topic exchange
        TopicExchange exchange = new TopicExchange(EventConstants.EXCHANGE_NAME);
        rabbitAdmin.declareExchange(exchange);

        // Allow subclasses to declare their specific queues and bindings
        declareQueuesAndBindings(rabbitAdmin, exchange);
    }

    @BeforeEach
    void purgeAllQueues() {
        for (String queueName : declaredQueueNames) {
            rabbitAdmin.purgeQueue(queueName);
        }
    }

    @AfterAll
    void tearDownRabbitMQ() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    /**
     * Subclasses declare their specific queues and bindings for the workflow under test.
     */
    protected abstract void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange);

    /**
     * Publishes an event to the bloodbank exchange with the specified routing key.
     */
    protected void publishEvent(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(EventConstants.EXCHANGE_NAME, routingKey, event);
    }

    /**
     * Receives a message from a queue with a timeout.
     * Returns the deserialized event or null if no message was available.
     */
    protected <T> T receiveEvent(String queueName, Class<T> eventType) {
        Message message = rabbitTemplate.receive(queueName, 5000);
        if (message == null) {
            return null;
        }
        try {
            return objectMapper.readValue(message.getBody(), eventType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event from queue: " + queueName, e);
        }
    }

    /**
     * Asserts that a message is received on the specified queue within the timeout.
     */
    protected <T> T assertEventReceived(String queueName, Class<T> eventType) {
        T event = receiveEvent(queueName, eventType);
        assertThat(event)
                .as("Expected event of type %s on queue %s", eventType.getSimpleName(), queueName)
                .isNotNull();
        return event;
    }

    /**
     * Asserts that no message is received on the specified queue (within a short timeout).
     */
    protected void assertNoEventReceived(String queueName) {
        Message message = rabbitTemplate.receive(queueName, 1000);
        assertThat(message)
                .as("Expected no message on queue %s, but found one", queueName)
                .isNull();
    }

    /**
     * Declares a queue and binds it to the exchange with the specified routing key.
     */
    protected Queue declareAndBindQueue(RabbitAdmin admin, TopicExchange exchange,
                                        String queueName, String routingKey) {
        Queue queue = new Queue(queueName, true);
        admin.declareQueue(queue);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey));
        declaredQueueNames.add(queueName);
        return queue;
    }
}
