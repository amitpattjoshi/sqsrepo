package com.ecommerce.messaging.consumer;

import com.ecommerce.domain.event.OrderCreatedEvent;
import com.ecommerce.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Inventory Consumer - Listens to Order Created events.
 * Checks inventory availability and reserves stock.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    /**
     * Listen to the inventory queue for OrderCreatedEvent messages.
     * When an order is created, this consumer:
     * 1. Reserves stock for all items
     * 2. If successful, publishes StockReservedEvent to payment queue
     * 3. If failed, updates order status to FAILED and sends failure notification
     */
    @SqsListener("${app.sqs.queues.inventory}")
    public void handleOrderCreated(String message) {
        log.info("Received OrderCreatedEvent message from inventory queue");

        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            log.info("Processing order: {} with {} items", event.getOrderId(), event.getItems().size());

            // Process inventory synchronously (blocking on reactive chain)
            inventoryService.processOrderInventory(event)
                    .doOnSuccess(v -> log.info("Inventory processing completed for order: {}", event.getOrderId()))
                    .doOnError(e -> log.error("Inventory processing failed for order: {}", event.getOrderId(), e))
                    .block();

        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process inventory event", e);
        }
    }
}
