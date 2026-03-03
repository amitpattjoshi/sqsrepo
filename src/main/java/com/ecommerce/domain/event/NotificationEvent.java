package com.ecommerce.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event for sending notifications (email, SMS, push).
 * Consumed by Notification Service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String eventId;
    private String orderId;
    private String customerId;
    private String customerEmail;
    private NotificationType type;
    private String subject;
    private String message;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;

    public enum NotificationType {
        ORDER_CONFIRMATION,
        ORDER_CONFIRMED,
        ORDER_FAILED,
        PAYMENT_SUCCESS,
        PAYMENT_RECEIVED,
        PAYMENT_FAILED,
        ORDER_SHIPPED,
        ORDER_DELIVERED
    }
}
