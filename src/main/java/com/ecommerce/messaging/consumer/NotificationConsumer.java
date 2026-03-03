package com.ecommerce.messaging.consumer;

import com.ecommerce.domain.event.NotificationEvent;
import com.ecommerce.domain.event.PaymentCompletedEvent;
import com.ecommerce.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Notification Consumer - Listens to notification queue.
 * Handles both PaymentCompletedEvent and NotificationEvent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * Listen to the notification queue for various notification events.
     * Processes:
     * - PaymentCompletedEvent: Order completion confirmation
     * - NotificationEvent: General notifications (failures, shipping updates, etc.)
     */
    @SqsListener("${app.sqs.queues.notification}")
    public void handleNotification(String message) {
        log.info("Received message from notification queue");

        try {
            // Use Jackson tree model for safe type detection instead of brittle string
            // matching
            var jsonNode = objectMapper.readTree(message);

            if (jsonNode.has("transactionId") && jsonNode.has("paymentMethod")) {
                PaymentCompletedEvent event = objectMapper.treeToValue(jsonNode, PaymentCompletedEvent.class);
                log.info("Processing PaymentCompletedEvent for order: {}", event.getOrderId());

                notificationService.processOrderCompletion(event)
                        .doOnSuccess(
                                v -> log.info("Order completion notification sent for order: {}", event.getOrderId()))
                        .doOnError(e -> log.error("Failed to process order completion notification: {}",
                                event.getOrderId(), e))
                        .block();
            } else {
                // Parse as NotificationEvent
                NotificationEvent event = objectMapper.treeToValue(jsonNode, NotificationEvent.class);
                log.info("Processing NotificationEvent for order: {}, type: {}",
                        event.getOrderId(), event.getType());

                notificationService.processNotification(event)
                        .doOnSuccess(v -> log.info("Notification processed for order: {}", event.getOrderId()))
                        .doOnError(e -> log.error("Failed to process notification: {}", event.getOrderId(), e))
                        .block();
            }

        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process notification event", e);
        }
    }
}
