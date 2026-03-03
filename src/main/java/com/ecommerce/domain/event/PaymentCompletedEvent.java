package com.ecommerce.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when payment is completed.
 * Consumed by Notification Service to send confirmation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {

    private String eventId;
    private String orderId;
    private String customerId;
    private String customerEmail;
    private String paymentId;
    private String transactionId;
    private String paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private LocalDateTime completedAt;

    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        REFUNDED
    }
}
