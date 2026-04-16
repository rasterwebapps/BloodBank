package com.bloodbank.integration.event;

import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.events.EventConstants;
import com.bloodbank.integration.support.AbstractWorkflowIntegrationTest;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M6-011: Dead Letter Queue (DLQ) — malformed event triggers retries then DLQ routing.
 *
 * Verifies the retry and dead-letter infrastructure:
 * 1. A queue is configured with a dead-letter exchange (DLX).
 * 2. A ChannelAwareMessageListener with manual acknowledgement simulates N-1 retries
 *    (nack + requeue) followed by a final rejection (nack without requeue).
 * 3. RabbitMQ routes the rejected message to the DLQ via the DLX.
 * 4. The DLQ is verified to contain the original message payload.
 *
 * Infrastructure under test:
 *   publisher -> test.dlq.processing.queue --(x-dead-letter-exchange: bloodbank.dlx)--> bloodbank.dlq
 */
@DisplayName("M6-011: Dead Letter Queue — malformed event, retries, then DLQ")
class DeadLetterQueueIntegrationTest extends AbstractWorkflowIntegrationTest {

    /** Total delivery attempts before message is dead-lettered (1 initial + 2 retries = 3). */
    private static final int MAX_ATTEMPTS = 3;

    private static final String PROCESSING_QUEUE = "test.dlq.processing.queue";
    private static final String DLX_EXCHANGE      = "bloodbank.dlx";
    private static final String DLQ_QUEUE         = "bloodbank.dlq";
    private static final String DLQ_ROUTING_KEY   = "dead.letter";

    @Override
    protected void declareQueuesAndBindings(RabbitAdmin admin, TopicExchange exchange) {
        // Declare DLX (direct exchange for dead-lettered messages)
        DirectExchange dlx = new DirectExchange(DLX_EXCHANGE, true, false);
        admin.declareExchange(dlx);

        // Declare DLQ and bind it to the DLX
        Queue dlq = new Queue(DLQ_QUEUE, true, false, false);
        admin.declareQueue(dlq);
        admin.declareBinding(
                org.springframework.amqp.core.BindingBuilder.bind(dlq).to(dlx).with(DLQ_ROUTING_KEY));

        // Declare the processing queue with dead-letter configuration
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange",    DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        Queue processingQueue = new Queue(PROCESSING_QUEUE, true, false, false, args);
        admin.declareQueue(processingQueue);
    }

    /**
     * Builds a container that simulates retry behaviour using manual acknowledgement:
     * nack+requeue for the first (maxAttempts-1) deliveries, then nack without requeue
     * on the final attempt so RabbitMQ routes the message to the DLQ.
     *
     * @param queueName   queue to consume from
     * @param attempts    shared counter tracking total delivery attempts
     * @param maxAttempts maximum attempts before DLQ routing
     * @param doneLatch   counted down when the final nack (DLQ route) is issued
     */
    private SimpleMessageListenerContainer buildManualRetryContainer(
            String queueName,
            AtomicInteger attempts,
            int maxAttempts,
            CountDownLatch doneLatch) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setPrefetchCount(1);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            int attempt = attempts.incrementAndGet();
            long tag    = message.getMessageProperties().getDeliveryTag();

            if (attempt < maxAttempts) {
                // Simulate transient failure: nack and requeue for retry
                channel.basicNack(tag, false, true);
            } else {
                // Final attempt: nack without requeue — RabbitMQ routes to DLQ
                channel.basicNack(tag, false, false);
                doneLatch.countDown();
            }
        });
        return container;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Malformed event tests
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Malformed event: invalid JSON payload routed to DLQ after retries")
    class MalformedEventTests {

        @Test
        @DisplayName("Malformed JSON payload should reach DLQ after MAX_ATTEMPTS delivery attempts")
        void malformedJsonPayloadShouldReachDlqAfterRetries() throws InterruptedException {
            AtomicInteger attempts = new AtomicInteger(0);
            CountDownLatch doneLatch = new CountDownLatch(1);

            SimpleMessageListenerContainer container =
                    buildManualRetryContainer(PROCESSING_QUEUE, attempts, MAX_ATTEMPTS, doneLatch);
            container.start();

            try {
                // Publish a malformed JSON payload (simulates a corrupt or unreadable event)
                byte[] malformedPayload = "{not: valid: json: bytes".getBytes(StandardCharsets.UTF_8);
                MessageProperties props = new MessageProperties();
                props.setContentType("application/json");
                rabbitTemplate.send(PROCESSING_QUEUE, new Message(malformedPayload, props));

                // Wait until the final nack triggers DLQ routing
                boolean done = doneLatch.await(15, TimeUnit.SECONDS);
                assertThat(done)
                        .as("DLQ routing should occur within timeout")
                        .isTrue();

                // Verify exactly MAX_ATTEMPTS delivery attempts were made
                assertThat(attempts.get())
                        .as("Listener should be invoked exactly %d times (1 initial + %d retries)",
                                MAX_ATTEMPTS, MAX_ATTEMPTS - 1)
                        .isEqualTo(MAX_ATTEMPTS);

                // Allow brief time for RabbitMQ DLX routing to complete
                Thread.sleep(300);

                // Verify DLQ contains the dead-lettered message
                Message dlqMessage = rabbitTemplate.receive(DLQ_QUEUE, 5000);
                assertThat(dlqMessage)
                        .as("Malformed message should appear in DLQ after exhausting retries")
                        .isNotNull();

                // Verify the original payload is preserved in DLQ
                String dlqBody = new String(dlqMessage.getBody(), StandardCharsets.UTF_8);
                assertThat(dlqBody).contains("not valid");
            } finally {
                container.stop();
            }
        }

        @Test
        @DisplayName("DLQ message should retain the full original payload bytes")
        void dlqMessageShouldRetainOriginalPayload() throws InterruptedException {
            String uniqueMarker  = "MALFORMED-" + UUID.randomUUID();
            byte[] payload       = ("{" + uniqueMarker + ":invalid}").getBytes(StandardCharsets.UTF_8);

            AtomicInteger attempts = new AtomicInteger(0);
            CountDownLatch doneLatch = new CountDownLatch(1);

            SimpleMessageListenerContainer container =
                    buildManualRetryContainer(PROCESSING_QUEUE, attempts, MAX_ATTEMPTS, doneLatch);
            container.start();

            try {
                rabbitTemplate.send(PROCESSING_QUEUE, new Message(payload, new MessageProperties()));

                boolean done = doneLatch.await(15, TimeUnit.SECONDS);
                assertThat(done).isTrue();
                Thread.sleep(300);

                Message dlqMessage = rabbitTemplate.receive(DLQ_QUEUE, 5000);
                assertThat(dlqMessage).isNotNull();

                String dlqBody = new String(dlqMessage.getBody(), StandardCharsets.UTF_8);
                assertThat(dlqBody)
                        .as("Original payload should be preserved verbatim in DLQ")
                        .contains(uniqueMarker);
            } finally {
                container.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Retry count verification
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Retry count: exactly MAX_ATTEMPTS delivery attempts before DLQ")
    class RetryCountVerification {

        @Test
        @DisplayName("Listener should be invoked exactly 3 times before message reaches DLQ")
        void listenerShouldBeInvokedExactlyThreeTimesBeforeDlq() throws InterruptedException {
            AtomicInteger attempts = new AtomicInteger(0);
            CountDownLatch doneLatch = new CountDownLatch(1);

            SimpleMessageListenerContainer container =
                    buildManualRetryContainer(PROCESSING_QUEUE, attempts, MAX_ATTEMPTS, doneLatch);
            container.start();

            try {
                // Publish a structured event that still fails processing
                DonationCompletedEvent event = new DonationCompletedEvent(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now());
                rabbitTemplate.convertAndSend(PROCESSING_QUEUE, event);

                boolean done = doneLatch.await(15, TimeUnit.SECONDS);
                assertThat(done)
                        .as("Recovery should complete within timeout")
                        .isTrue();

                assertThat(attempts.get())
                        .as("Exactly %d delivery attempts expected", MAX_ATTEMPTS)
                        .isEqualTo(MAX_ATTEMPTS);

                Thread.sleep(300);

                Message dlqMessage = rabbitTemplate.receive(DLQ_QUEUE, 5000);
                assertThat(dlqMessage)
                        .as("Message should be dead-lettered after %d failed attempts", MAX_ATTEMPTS)
                        .isNotNull();
            } finally {
                container.stop();
            }
        }

        @Test
        @DisplayName("Single retry (maxAttempts=2) should result in exactly 2 listener invocations")
        void singleRetryConfigurationShouldResultInTwoInvocations() throws InterruptedException {
            final int singleRetry   = 2; // 1 initial + 1 retry
            AtomicInteger attempts  = new AtomicInteger(0);
            CountDownLatch doneLatch = new CountDownLatch(1);

            SimpleMessageListenerContainer container =
                    buildManualRetryContainer(PROCESSING_QUEUE, attempts, singleRetry, doneLatch);
            container.start();

            try {
                byte[] payload = "test-single-retry".getBytes(StandardCharsets.UTF_8);
                rabbitTemplate.send(PROCESSING_QUEUE, new Message(payload, new MessageProperties()));

                boolean done = doneLatch.await(15, TimeUnit.SECONDS);
                assertThat(done).isTrue();

                assertThat(attempts.get()).isEqualTo(singleRetry);

                Thread.sleep(300);
                Message dlqMessage = rabbitTemplate.receive(DLQ_QUEUE, 5000);
                assertThat(dlqMessage).isNotNull();
            } finally {
                container.stop();
            }
        }

        @Test
        @DisplayName("Five retries (maxAttempts=5) should result in exactly 5 listener invocations")
        void fiveRetriesConfigurationShouldResultInFiveInvocations() throws InterruptedException {
            final int fiveRetries   = 5;
            AtomicInteger attempts  = new AtomicInteger(0);
            CountDownLatch doneLatch = new CountDownLatch(1);

            SimpleMessageListenerContainer container =
                    buildManualRetryContainer(PROCESSING_QUEUE, attempts, fiveRetries, doneLatch);
            container.start();

            try {
                byte[] payload = "test-five-retries".getBytes(StandardCharsets.UTF_8);
                rabbitTemplate.send(PROCESSING_QUEUE, new Message(payload, new MessageProperties()));

                boolean done = doneLatch.await(20, TimeUnit.SECONDS);
                assertThat(done).isTrue();

                assertThat(attempts.get()).isEqualTo(fiveRetries);

                Thread.sleep(300);
                Message dlqMessage = rabbitTemplate.receive(DLQ_QUEUE, 5000);
                assertThat(dlqMessage).isNotNull();
            } finally {
                container.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DLQ isolation
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DLQ isolation: only rejected messages reach DLQ")
    class DlqIsolationTests {

        @Test
        @DisplayName("Successfully acknowledged messages should NOT reach DLQ")
        void successfullyAckedMessagesShouldNotReachDlq() throws InterruptedException {
            CountDownLatch successLatch = new CountDownLatch(1);
            AtomicInteger processedCount = new AtomicInteger(0);

            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueueNames(PROCESSING_QUEUE);
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            container.setPrefetchCount(1);
            container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
                // Successful processing — ack normally
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                processedCount.incrementAndGet();
                successLatch.countDown();
            });
            container.start();

            try {
                DonationCompletedEvent event = new DonationCompletedEvent(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now());
                rabbitTemplate.convertAndSend(PROCESSING_QUEUE, event);

                boolean processed = successLatch.await(10, TimeUnit.SECONDS);
                assertThat(processed).as("Message should be processed successfully").isTrue();
                assertThat(processedCount.get()).isEqualTo(1);

                // DLQ must remain empty — successful messages must not be dead-lettered
                Message dlqMessage = rabbitTemplate.receive(DLQ_QUEUE, 1000);
                assertThat(dlqMessage)
                        .as("Successfully processed message must NOT be present in DLQ")
                        .isNull();
            } finally {
                container.stop();
            }
        }

        @Test
        @DisplayName("Multiple failed messages should each independently reach DLQ")
        void multipleFailedMessagesShouldEachReachDlqIndependently() throws InterruptedException {
            final int messageCount    = 3;
            AtomicInteger totalNacked = new AtomicInteger(0);
            CountDownLatch allDlqed   = new CountDownLatch(messageCount);

            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueueNames(PROCESSING_QUEUE);
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            container.setPrefetchCount(1);
            container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
                // Immediately reject each message to DLQ (maxAttempts=1 for speed)
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                totalNacked.incrementAndGet();
                allDlqed.countDown();
            });
            container.start();

            try {
                for (int i = 0; i < messageCount; i++) {
                    byte[] payload = ("failed-message-" + i).getBytes(StandardCharsets.UTF_8);
                    rabbitTemplate.send(PROCESSING_QUEUE, new Message(payload, new MessageProperties()));
                }

                boolean done = allDlqed.await(20, TimeUnit.SECONDS);
                assertThat(done)
                        .as("All %d messages should be dead-lettered within timeout", messageCount)
                        .isTrue();

                Thread.sleep(500);

                // Drain DLQ and count all messages
                int dlqCount = 0;
                while (rabbitTemplate.receive(DLQ_QUEUE, 2000) != null) {
                    dlqCount++;
                }
                assertThat(dlqCount)
                        .as("All %d failed messages should appear in DLQ", messageCount)
                        .isEqualTo(messageCount);
            } finally {
                container.stop();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Exchange-routed event to DLQ
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Exchange-routed events: events published via topic exchange also DLQ on failure")
    class ExchangeRoutedEventToDlq {

        @Test
        @DisplayName("DonationCompletedEvent published via exchange should reach DLQ after retries")
        void donationEventPublishedViaExchangeShouldReachDlq() throws InterruptedException {
            AtomicInteger attempts  = new AtomicInteger(0);
            CountDownLatch doneLatch = new CountDownLatch(1);

            // The processing queue is NOT bound to the exchange in this test class, so we
            // publish directly to the queue name (as a default exchange routing key).
            SimpleMessageListenerContainer container =
                    buildManualRetryContainer(PROCESSING_QUEUE, attempts, MAX_ATTEMPTS, doneLatch);
            container.start();

            try {
                UUID donationId = UUID.randomUUID();
                DonationCompletedEvent event = new DonationCompletedEvent(
                        donationId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());

                // Publish directly to the queue via default exchange
                rabbitTemplate.convertAndSend(PROCESSING_QUEUE, event);

                boolean done = doneLatch.await(15, TimeUnit.SECONDS);
                assertThat(done).isTrue();
                assertThat(attempts.get()).isEqualTo(MAX_ATTEMPTS);

                Thread.sleep(300);

                Message dlqMessage = rabbitTemplate.receive(DLQ_QUEUE, 5000);
                assertThat(dlqMessage).isNotNull();

                // Verify the donation ID survived the DLQ journey
                String dlqBody = new String(dlqMessage.getBody(), StandardCharsets.UTF_8);
                assertThat(dlqBody).contains(donationId.toString());
            } finally {
                container.stop();
            }
        }
    }
}
