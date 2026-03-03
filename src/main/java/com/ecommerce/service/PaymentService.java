package com.ecommerce.service;

import com.ecommerce.domain.entity.OrderStatus;
import com.ecommerce.domain.event.NotificationEvent;
import com.ecommerce.domain.event.PaymentCompletedEvent;
import com.ecommerce.domain.event.StockReservedEvent;
import com.ecommerce.messaging.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Payment Service - Simulates payment processing.
 * Consumes StockReservedEvent and produces PaymentCompletedEvent.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final EventPublisher eventPublisher;
    private final OrderService orderService;

    // Use SecureRandom instead of Random for unpredictable payment simulation
    private final SecureRandom random = new SecureRandom();
    private static final double PAYMENT_SUCCESS_RATE = 0.9;

    /**
     * Process payment for an order.
     * Simulates payment gateway integration.
     */
    public Mono<Void> processPayment(StockReservedEvent event) {
        log.info("Processing payment for order: {}, amount: {}",
                event.getOrderId(), event.getTotalAmount());

        // Use non-blocking delay instead of Thread.sleep
        return Mono.delay(java.time.Duration.ofMillis(100))
                .map(ignored -> random.nextDouble() < PAYMENT_SUCCESS_RATE)
                .flatMap(paymentSuccess -> {
                    if (paymentSuccess) {
                        return handleSuccessfulPayment(event);
                    } else {
                        return handleFailedPayment(event);
                    }
                })
                .doOnError(e -> log.error("Error processing payment for order {}: {}",
                        event.getOrderId(), e.getMessage()));
    }

    private Mono<Void> handleSuccessfulPayment(StockReservedEvent event) {
        log.info("Payment successful for order: {}", event.getOrderId());

        String transactionId = "TXN-" + System.currentTimeMillis();

        // Update order status
        return orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAYMENT_COMPLETED)
                .flatMap(order -> {
                    // Publish PaymentCompletedEvent
                    PaymentCompletedEvent paymentEvent = PaymentCompletedEvent.builder()
                            .orderId(event.getOrderId())
                            .customerId(event.getCustomerId())
                            .customerEmail(event.getCustomerEmail())
                            .transactionId(transactionId)
                            .amount(event.getTotalAmount())
                            .paymentMethod("CREDIT_CARD")
                            .completedAt(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishPaymentCompleted(paymentEvent);
                });
    }

    private Mono<Void> handleFailedPayment(StockReservedEvent event) {
        log.warn("Payment failed for order: {}", event.getOrderId());

        // Update order status
        return orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAYMENT_FAILED)
                .flatMap(order -> {
                    // Send failure notification
                    NotificationEvent notification = NotificationEvent.builder()
                            .orderId(event.getOrderId())
                            .customerId(event.getCustomerId())
                            .customerEmail(event.getCustomerEmail())
                            .type(NotificationEvent.NotificationType.ORDER_FAILED)
                            .message("Payment failed. Please try again or use a different payment method.")
                            .createdAt(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishNotification(notification);
                });
    }

    /**
     * Process refund for cancelled/failed orders.
     */
    public Mono<Boolean> processRefund(String orderId, String transactionId) {
        log.info("Processing refund for order: {}", orderId);

        // Use non-blocking delay instead of Thread.sleep
        return Mono.delay(java.time.Duration.ofMillis(100))
                .map(ignored -> true) // Assume refund always succeeds for demo
                .doOnSuccess(success -> {
                    if (success) {
                        log.info("Refund processed for order: {}", orderId);
                    } else {
                        log.warn("Refund failed for order: {}", orderId);
                    }
                });
    }
}
