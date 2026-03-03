package com.ecommerce.service;

import com.ecommerce.domain.entity.OrderStatus;
import com.ecommerce.domain.event.NotificationEvent;
import com.ecommerce.domain.event.OrderCreatedEvent;
import com.ecommerce.domain.event.StockReservedEvent;
import com.ecommerce.messaging.publisher.EventPublisher;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory Service - Handles stock reservation and release.
 * Consumes OrderCreatedEvent and produces StockReservedEvent.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;
    private final OrderService orderService;

    /**
     * Process order for inventory - reserve stock for all items.
     * If successful, publish StockReservedEvent.
     * If failed, update order status to FAILED and send failure notification.
     */
    @Transactional
    public Mono<Void> processOrderInventory(OrderCreatedEvent event) {
        log.info("Processing inventory for order: {}", event.getOrderId());

        return reserveStockForOrder(event)
                .flatMap(success -> {
                    if (success) {
                        return handleSuccessfulReservation(event);
                    } else {
                        return handleFailedReservation(event);
                    }
                })
                .doOnError(e -> log.error("Error processing inventory for order {}: {}",
                        event.getOrderId(), e.getMessage()));
    }

    private Mono<Boolean> reserveStockForOrder(OrderCreatedEvent event) {
        List<OrderCreatedEvent.OrderItemDetail> reservedItems = new ArrayList<>();

        return Flux.fromIterable(event.getItems())
                .concatMap(item -> productRepository.reserveStock(item.getProductId(), item.getQuantity())
                        .map(rowsUpdated -> {
                            if (rowsUpdated > 0) {
                                reservedItems.add(item);
                                log.info("Reserved stock for product {} qty {}",
                                        item.getProductId(), item.getQuantity());
                                return true;
                            } else {
                                log.warn("Failed to reserve stock for product {}", item.getProductId());
                                return false;
                            }
                        }))
                .reduce(true, (a, b) -> a && b)
                .flatMap(allSuccess -> {
                    if (!allSuccess) {
                        // Rollback reserved items
                        return rollbackReservedStock(reservedItems)
                                .thenReturn(false);
                    }
                    return Mono.just(true);
                });
    }

    private Mono<Void> rollbackReservedStock(List<OrderCreatedEvent.OrderItemDetail> reservedItems) {
        return Flux.fromIterable(reservedItems)
                .flatMap(item -> productRepository.releaseStock(item.getProductId(), item.getQuantity()))
                .then()
                .doOnSuccess(v -> log.info("Rolled back {} reserved items", reservedItems.size()));
    }

    private Mono<Void> handleSuccessfulReservation(OrderCreatedEvent event) {
        log.info("Stock reserved successfully for order: {}", event.getOrderId());

        // Update order status
        return orderService.updateOrderStatus(event.getOrderId(), OrderStatus.INVENTORY_RESERVED)
                .flatMap(order -> {
                    // Publish StockReservedEvent for Payment Service
                    StockReservedEvent stockEvent = StockReservedEvent.builder()
                            .orderId(event.getOrderId())
                            .customerId(event.getCustomerId())
                            .customerEmail(event.getCustomerEmail())
                            .totalAmount(event.getTotalAmount())
                            .reservedAt(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishStockReserved(stockEvent);
                });
    }

    private Mono<Void> handleFailedReservation(OrderCreatedEvent event) {
        log.warn("Stock reservation failed for order: {}", event.getOrderId());

        // Update order status
        return orderService.updateOrderStatus(event.getOrderId(), OrderStatus.FAILED)
                .flatMap(order -> {
                    // Send failure notification
                    NotificationEvent notification = NotificationEvent.builder()
                            .orderId(event.getOrderId())
                            .customerId(event.getCustomerId())
                            .customerEmail(event.getCustomerEmail())
                            .type(NotificationEvent.NotificationType.ORDER_FAILED)
                            .message("Order failed: Insufficient stock available")
                            .createdAt(LocalDateTime.now())
                            .build();

                    return eventPublisher.publishNotification(notification);
                });
    }

    /**
     * Release stock - called when payment fails or order is cancelled.
     */
    @Transactional
    public Mono<Void> releaseStock(String orderId, List<OrderCreatedEvent.OrderItemDetail> items) {
        log.info("Releasing stock for order: {}", orderId);

        return Flux.fromIterable(items)
                .flatMap(item -> productRepository.releaseStock(item.getProductId(), item.getQuantity()))
                .then()
                .doOnSuccess(v -> log.info("Stock released for order: {}", orderId));
    }
}
