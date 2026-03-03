package com.ecommerce.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when a new order is created.
 * Consumed by Inventory Service to reserve stock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private String eventId;
    private String orderId;
    private String customerId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private List<OrderItemDetail> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private String productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
