package com.ecommerce.service;

import com.ecommerce.domain.entity.OrderStatus;
import com.ecommerce.domain.event.NotificationEvent;
import com.ecommerce.domain.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Notification Service - Handles sending notifications to customers.
 * Consumes PaymentCompletedEvent and NotificationEvent.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final OrderService orderService;

    /**
     * Process successful payment notification.
     * Updates order status and sends confirmation email.
     */
    public Mono<Void> processOrderCompletion(PaymentCompletedEvent event) {
        log.info("Processing order completion notification for order: {}", event.getOrderId());

        return orderService.updateOrderStatus(event.getOrderId(), OrderStatus.COMPLETED)
                .flatMap(order -> sendOrderConfirmationEmail(event))
                .doOnSuccess(v -> log.info("Order completion notification sent for order: {}", event.getOrderId()));
    }

    /**
     * Process general notification event.
     */
    public Mono<Void> processNotification(NotificationEvent event) {
        log.info("Processing notification for order: {}, type: {}",
                event.getOrderId(), event.getType());

        return switch (event.getType()) {
            case ORDER_CONFIRMATION, ORDER_CONFIRMED -> sendOrderConfirmedNotification(event);
            case ORDER_FAILED -> sendOrderFailedNotification(event);
            case PAYMENT_SUCCESS, PAYMENT_RECEIVED -> sendPaymentReceivedNotification(event);
            case PAYMENT_FAILED -> sendPaymentFailedNotification(event);
            case ORDER_SHIPPED -> sendOrderShippedNotification(event);
            case ORDER_DELIVERED -> sendOrderDeliveredNotification(event);
        };
    }

    /**
     * Simulate sending order confirmation email.
     * In production, integrate with email service like SendGrid, SES, etc.
     */
    private Mono<Void> sendOrderConfirmationEmail(PaymentCompletedEvent event) {
        return Mono.fromRunnable(() -> {
            log.info("========================================");
            log.info("📧 SENDING ORDER CONFIRMATION EMAIL");
            log.info("========================================");
            log.info("To: {}", event.getCustomerEmail());
            log.info("Subject: Order Confirmed - {}", event.getOrderId());
            log.info("Body: ");
            log.info("  Dear Customer {},", event.getCustomerId());
            log.info("  Thank you for your order!");
            log.info("  Order ID: {}", event.getOrderId());
            log.info("  Transaction ID: {}", event.getTransactionId());
            log.info("  Amount Paid: ${}", event.getAmount());
            log.info("  Payment Method: {}", event.getPaymentMethod());
            log.info("  Your order is being processed and will be shipped soon.");
            log.info("========================================");
        });
    }

    private Mono<Void> sendOrderConfirmedNotification(NotificationEvent event) {
        return Mono.fromRunnable(() -> {
            log.info("📧 Sending order confirmed notification to: {}", event.getCustomerEmail());
            log.info("   Order: {} - {}", event.getOrderId(), event.getMessage());
        });
    }

    private Mono<Void> sendOrderFailedNotification(NotificationEvent event) {
        return Mono.fromRunnable(() -> {
            log.info("========================================");
            log.info("📧 SENDING ORDER FAILED EMAIL");
            log.info("========================================");
            log.info("To: {}", event.getCustomerEmail());
            log.info("Subject: Order Failed - {}", event.getOrderId());
            log.info("Body: ");
            log.info("  Dear Customer {},", event.getCustomerId());
            log.info("  We're sorry, but your order could not be processed.");
            log.info("  Order ID: {}", event.getOrderId());
            log.info("  Reason: {}", event.getMessage());
            log.info("  Please try again or contact support.");
            log.info("========================================");
        });
    }

    private Mono<Void> sendPaymentReceivedNotification(NotificationEvent event) {
        return Mono.fromRunnable(() -> {
            log.info("📧 Sending payment received notification to: {}", event.getCustomerEmail());
            log.info("   Order: {} - {}", event.getOrderId(), event.getMessage());
        });
    }

    private Mono<Void> sendPaymentFailedNotification(NotificationEvent event) {
        return Mono.fromRunnable(() -> {
            log.info("========================================");
            log.info("📧 SENDING PAYMENT FAILED EMAIL");
            log.info("========================================");
            log.info("To: {}", event.getCustomerEmail());
            log.info("Subject: Payment Failed - {}", event.getOrderId());
            log.info("Body: ");
            log.info("  Dear Customer {},", event.getCustomerId());
            log.info("  Your payment could not be processed.");
            log.info("  Order ID: {}", event.getOrderId());
            log.info("  Reason: {}", event.getMessage());
            log.info("  Please try again with a different payment method.");
            log.info("========================================");
        });
    }

    private Mono<Void> sendOrderShippedNotification(NotificationEvent event) {
        return Mono.fromRunnable(() -> {
            log.info("========================================");
            log.info("📧 SENDING ORDER SHIPPED EMAIL");
            log.info("========================================");
            log.info("To: {}", event.getCustomerEmail());
            log.info("Subject: Your Order Has Shipped - {}", event.getOrderId());
            log.info("Body: {}", event.getMessage());
            log.info("========================================");
        });
    }

    private Mono<Void> sendOrderDeliveredNotification(NotificationEvent event) {
        return Mono.fromRunnable(() -> {
            log.info("========================================");
            log.info("📧 SENDING ORDER DELIVERED EMAIL");
            log.info("========================================");
            log.info("To: {}", event.getCustomerEmail());
            log.info("Subject: Your Order Has Been Delivered - {}", event.getOrderId());
            log.info("Body: {}", event.getMessage());
            log.info("========================================");
        });
    }

    /**
     * Send SMS notification (simulation)
     */
    public Mono<Void> sendSmsNotification(String phoneNumber, String message) {
        return Mono.fromRunnable(() -> {
            log.info("📱 SMS to {}: {}", phoneNumber, message);
        });
    }

    /**
     * Send push notification (simulation)
     */
    public Mono<Void> sendPushNotification(String customerId, String title, String body) {
        return Mono.fromRunnable(() -> {
            log.info("🔔 Push notification to customer {}: {} - {}", customerId, title, body);
        });
    }
}
