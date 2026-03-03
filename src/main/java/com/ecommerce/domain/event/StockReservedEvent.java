package com.ecommerce.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when stock is successfully reserved.
 * Consumed by Payment Service to process payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {

    private String eventId;
    private String orderId;
    private String customerId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private LocalDateTime reservedAt;
}
