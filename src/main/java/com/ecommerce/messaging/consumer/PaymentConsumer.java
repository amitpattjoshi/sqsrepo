package com.ecommerce.messaging.consumer;

import com.ecommerce.domain.event.StockReservedEvent;
import com.ecommerce.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Payment Consumer - Listens to Stock Reserved events.
 * Processes payment when inventory is successfully reserved.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    /**
     * Listen to the payment queue for StockReservedEvent messages.
     * When stock is reserved, this consumer:
     * 1. Processes payment through payment gateway
     * 2. If successful, publishes PaymentCompletedEvent to notification queue
     * 3. If failed, updates order status to PAYMENT_FAILED and sends failure
     * notification
     */
    @SqsListener("${app.sqs.queues.payment}")
    public void handleStockReserved(String message) {
        log.info("Received StockReservedEvent message from payment queue");

        try {
            StockReservedEvent event = objectMapper.readValue(message, StockReservedEvent.class);
            log.info("Processing payment for order: {}, amount: {}",
                    event.getOrderId(), event.getTotalAmount());

            // Process payment synchronously (blocking on reactive chain)
            paymentService.processPayment(event)
                    .doOnSuccess(v -> log.info("Payment processing completed for order: {}", event.getOrderId()))
                    .doOnError(e -> log.error("Payment processing failed for order: {}", event.getOrderId(), e))
                    .block();

        } catch (Exception e) {
            log.error("Error processing StockReservedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process payment event", e);
        }
    }
}
