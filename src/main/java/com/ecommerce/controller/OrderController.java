package com.ecommerce.controller;

import com.ecommerce.domain.dto.CreateOrderRequest;
import com.ecommerce.domain.dto.OrderResponse;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Order Controller - REST API for order management.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order.
     * This triggers the event-driven flow:
     * Order → Inventory → Payment → Notification
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Received order creation request from customer: {}", request.getCustomerId());
        return orderService.createOrder(request);
    }

    /**
     * Get order by ID.
     */
    @GetMapping("/{orderId}")
    public Mono<OrderResponse> getOrder(@PathVariable String orderId) {
        log.info("Fetching order: {}", orderId);
        return orderService.getOrder(orderId);
    }

    /**
     * Get all orders for a customer.
     */
    @GetMapping("/customer/{customerId}")
    public Flux<OrderResponse> getOrdersByCustomer(@PathVariable String customerId) {
        log.info("Fetching orders for customer: {}", customerId);
        return orderService.getOrdersByCustomerId(customerId);
    }
}
