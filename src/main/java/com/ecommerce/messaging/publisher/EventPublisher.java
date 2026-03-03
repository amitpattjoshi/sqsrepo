package com.ecommerce.messaging.publisher;

import com.ecommerce.domain.event.NotificationEvent;
import com.ecommerce.domain.event.OrderCreatedEvent;
import com.ecommerce.domain.event.PaymentCompletedEvent;
import com.ecommerce.domain.event.StockReservedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

/**
 * Event Publisher - Sends messages to SQS queues reactively.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.sqs.queues.order}")
    private String orderQueue;

    @Value("${app.sqs.queues.payment}")
    private String paymentQueue;

    @Value("${app.sqs.queues.notification}")
    private String notificationQueue;

    @Value("${app.sqs.queues.inventory}")
    private String inventoryQueue;

    /**
     * Publish OrderCreatedEvent to order queue (consumed by Inventory Service)
     */
    public Mono<Void> publishOrderCreated(OrderCreatedEvent event) {
        return publishEvent(inventoryQueue, event, event.getOrderId())
                .doOnSuccess(v -> log.info("Published OrderCreatedEvent for order: {}", event.getOrderId()));
    }

    /**
     * Publish StockReservedEvent to payment queue (consumed by Payment Service)
     */
    public Mono<Void> publishStockReserved(StockReservedEvent event) {
        return publishEvent(paymentQueue, event, event.getOrderId())
                .doOnSuccess(v -> log.info("Published StockReservedEvent for order: {}", event.getOrderId()));
    }

    /**
     * Publish PaymentCompletedEvent to notification queue
     */
    public Mono<Void> publishPaymentCompleted(PaymentCompletedEvent event) {
        return publishEvent(notificationQueue, event, event.getOrderId())
                .doOnSuccess(v -> log.info("Published PaymentCompletedEvent for order: {}", event.getOrderId()));
    }

    /**
     * Publish NotificationEvent directly to notification queue
     */
    public Mono<Void> publishNotification(NotificationEvent event) {
        return publishEvent(notificationQueue, event, event.getOrderId())
                .doOnSuccess(v -> log.info("Published NotificationEvent for order: {}", event.getOrderId()));
    }

    /**
     * Generic event publisher - uses sendAsync for non-blocking operation
     * Includes retry with exponential backoff for resilience
     */
    private <T> Mono<Void> publishEvent(String queueName, T event, String messageGroupId) {
        return Mono.fromCallable(() -> serializeEvent(event))
                .flatMap(payload -> Mono.fromFuture(() -> sqsTemplate.sendAsync(to -> to
                        .queue(queueName)
                        .payload(payload)
                        .header("message-group-id", messageGroupId)
                        .header("message-deduplication-id", UUID.randomUUID().toString()))))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(10))
                        .doBeforeRetry(signal -> log.warn("Retrying publish to {} (attempt {}): {}",
                                queueName, signal.totalRetries() + 1, signal.failure().getMessage())))
                .doOnError(e -> log.error("Failed to publish event to {} after all retries: {}", queueName,
                        e.getMessage()))
                .then();
    }

    private String serializeEvent(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
