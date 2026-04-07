package com.bloodbank.notificationservice.config;

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

    public static final String DONATION_COMPLETED_QUEUE = "notification.donation.completed.queue";
    public static final String CAMP_COMPLETED_QUEUE = "notification.camp.completed.queue";
    public static final String TEST_RESULT_AVAILABLE_QUEUE = "notification.test.result.available.queue";
    public static final String UNIT_RELEASED_QUEUE = "notification.unit.released.queue";
    public static final String BLOOD_STOCK_UPDATED_QUEUE = "notification.blood.stock.updated.queue";
    public static final String STOCK_CRITICAL_QUEUE = "notification.stock.critical.queue";
    public static final String UNIT_EXPIRING_QUEUE = "notification.unit.expiring.queue";
    public static final String BLOOD_REQUEST_CREATED_QUEUE = "notification.blood.request.created.queue";
    public static final String BLOOD_REQUEST_MATCHED_QUEUE = "notification.blood.request.matched.queue";
    public static final String EMERGENCY_REQUEST_QUEUE = "notification.emergency.request.queue";
    public static final String TRANSFUSION_COMPLETED_QUEUE = "notification.transfusion.completed.queue";
    public static final String TRANSFUSION_REACTION_QUEUE = "notification.transfusion.reaction.queue";
    public static final String INVOICE_GENERATED_QUEUE = "notification.invoice.generated.queue";
    public static final String RECALL_INITIATED_QUEUE = "notification.recall.initiated.queue";

    @Bean
    public TopicExchange bloodbankExchange() {
        return new TopicExchange(EventConstants.EXCHANGE_NAME);
    }

    // Queues
    @Bean
    public Queue donationCompletedQueue() {
        return new Queue(DONATION_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue campCompletedQueue() {
        return new Queue(CAMP_COMPLETED_QUEUE, true);
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
    public Queue bloodStockUpdatedQueue() {
        return new Queue(BLOOD_STOCK_UPDATED_QUEUE, true);
    }

    @Bean
    public Queue stockCriticalQueue() {
        return new Queue(STOCK_CRITICAL_QUEUE, true);
    }

    @Bean
    public Queue unitExpiringQueue() {
        return new Queue(UNIT_EXPIRING_QUEUE, true);
    }

    @Bean
    public Queue bloodRequestCreatedQueue() {
        return new Queue(BLOOD_REQUEST_CREATED_QUEUE, true);
    }

    @Bean
    public Queue bloodRequestMatchedQueue() {
        return new Queue(BLOOD_REQUEST_MATCHED_QUEUE, true);
    }

    @Bean
    public Queue emergencyRequestQueue() {
        return new Queue(EMERGENCY_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue transfusionCompletedQueue() {
        return new Queue(TRANSFUSION_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue transfusionReactionQueue() {
        return new Queue(TRANSFUSION_REACTION_QUEUE, true);
    }

    @Bean
    public Queue invoiceGeneratedQueue() {
        return new Queue(INVOICE_GENERATED_QUEUE, true);
    }

    @Bean
    public Queue recallInitiatedQueue() {
        return new Queue(RECALL_INITIATED_QUEUE, true);
    }

    // Bindings
    @Bean
    public Binding donationCompletedBinding(Queue donationCompletedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(donationCompletedQueue)
                .to(bloodbankExchange).with(EventConstants.DONATION_COMPLETED);
    }

    @Bean
    public Binding campCompletedBinding(Queue campCompletedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(campCompletedQueue)
                .to(bloodbankExchange).with(EventConstants.CAMP_COMPLETED);
    }

    @Bean
    public Binding testResultAvailableBinding(Queue testResultAvailableQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(testResultAvailableQueue)
                .to(bloodbankExchange).with(EventConstants.TEST_RESULT_AVAILABLE);
    }

    @Bean
    public Binding unitReleasedBinding(Queue unitReleasedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(unitReleasedQueue)
                .to(bloodbankExchange).with(EventConstants.UNIT_RELEASED);
    }

    @Bean
    public Binding bloodStockUpdatedBinding(Queue bloodStockUpdatedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(bloodStockUpdatedQueue)
                .to(bloodbankExchange).with(EventConstants.BLOOD_STOCK_UPDATED);
    }

    @Bean
    public Binding stockCriticalBinding(Queue stockCriticalQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(stockCriticalQueue)
                .to(bloodbankExchange).with(EventConstants.STOCK_CRITICAL);
    }

    @Bean
    public Binding unitExpiringBinding(Queue unitExpiringQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(unitExpiringQueue)
                .to(bloodbankExchange).with(EventConstants.UNIT_EXPIRING);
    }

    @Bean
    public Binding bloodRequestCreatedBinding(Queue bloodRequestCreatedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(bloodRequestCreatedQueue)
                .to(bloodbankExchange).with(EventConstants.BLOOD_REQUEST_CREATED);
    }

    @Bean
    public Binding bloodRequestMatchedBinding(Queue bloodRequestMatchedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(bloodRequestMatchedQueue)
                .to(bloodbankExchange).with(EventConstants.BLOOD_REQUEST_MATCHED);
    }

    @Bean
    public Binding emergencyRequestBinding(Queue emergencyRequestQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(emergencyRequestQueue)
                .to(bloodbankExchange).with(EventConstants.EMERGENCY_REQUEST);
    }

    @Bean
    public Binding transfusionCompletedBinding(Queue transfusionCompletedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(transfusionCompletedQueue)
                .to(bloodbankExchange).with(EventConstants.TRANSFUSION_COMPLETED);
    }

    @Bean
    public Binding transfusionReactionBinding(Queue transfusionReactionQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(transfusionReactionQueue)
                .to(bloodbankExchange).with(EventConstants.TRANSFUSION_REACTION);
    }

    @Bean
    public Binding invoiceGeneratedBinding(Queue invoiceGeneratedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(invoiceGeneratedQueue)
                .to(bloodbankExchange).with(EventConstants.INVOICE_GENERATED);
    }

    @Bean
    public Binding recallInitiatedBinding(Queue recallInitiatedQueue, TopicExchange bloodbankExchange) {
        return BindingBuilder.bind(recallInitiatedQueue)
                .to(bloodbankExchange).with(EventConstants.RECALL_INITIATED);
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
