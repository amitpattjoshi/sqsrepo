package com.ecommerce.service;

import com.ecommerce.domain.dto.CreateOrderRequest;
import com.ecommerce.domain.dto.OrderResponse;
import com.ecommerce.domain.entity.Order;
import com.ecommerce.domain.entity.OrderItem;
import com.ecommerce.domain.entity.OrderStatus;
import com.ecommerce.domain.event.OrderCreatedEvent;
import com.ecommerce.messaging.publisher.EventPublisher;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Order Service - Handles order creation and orchestrates the event-driven
 * flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final ProductRepository productRepository;
        private final EventPublisher eventPublisher;

        /**
         * Create a new order and trigger the event-driven processing flow.
         * Flow: Order Created → Inventory Check → Payment → Notification
         */
        @Transactional
        public Mono<OrderResponse> createOrder(CreateOrderRequest request) {
                String orderId = UUID.randomUUID().toString();
                log.info("Creating order: {} for customer: {}", orderId, request.getCustomerId());

                // Calculate total amount
                return Flux.fromIterable(request.getItems())
                                .flatMap(item -> productRepository.findById(item.getProductId())
                                                .map(product -> product.getPrice()
                                                                .multiply(BigDecimal.valueOf(item.getQuantity()))))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .flatMap(totalAmount -> {
                                        // Create Order entity
                                        Order order = Order.builder()
                                                        .id(orderId)
                                                        .customerId(request.getCustomerId())
                                                        .customerEmail(request.getCustomerEmail())
                                                        .status(OrderStatus.PENDING)
                                                        .totalAmount(totalAmount)
                                                        .createdAt(LocalDateTime.now())
                                                        .updatedAt(LocalDateTime.now())
                                                        .build();

                                        return orderRepository.save(order)
                                                        .flatMap(savedOrder -> saveOrderItems(savedOrder,
                                                                        request.getItems()))
                                                        .flatMap(savedOrder -> publishOrderCreatedEvent(savedOrder,
                                                                        request))
                                                        .map(this::toOrderResponse);
                                });
        }

        private Mono<Order> saveOrderItems(Order order, List<CreateOrderRequest.OrderItemRequest> items) {
                return Flux.fromIterable(items)
                                .flatMap(item -> productRepository.findById(item.getProductId())
                                                .map(product -> OrderItem.builder()
                                                                .orderId(order.getId())
                                                                .productId(item.getProductId())
                                                                .quantity(item.getQuantity())
                                                                .unitPrice(product.getPrice())
                                                                .build())
                                                .flatMap(orderItemRepository::save))
                                .then(Mono.just(order));
        }

        private Mono<Order> publishOrderCreatedEvent(Order order, CreateOrderRequest request) {
                List<OrderCreatedEvent.OrderItemDetail> itemDetails = request.getItems().stream()
                                .map(item -> OrderCreatedEvent.OrderItemDetail.builder()
                                                .productId(item.getProductId())
                                                .quantity(item.getQuantity())
                                                .build())
                                .collect(Collectors.toList());

                OrderCreatedEvent event = OrderCreatedEvent.builder()
                                .orderId(order.getId())
                                .customerId(order.getCustomerId())
                                .customerEmail(order.getCustomerEmail())
                                .totalAmount(order.getTotalAmount())
                                .items(itemDetails)
                                .createdAt(LocalDateTime.now())
                                .build();

                return eventPublisher.publishOrderCreated(event)
                                .doOnSuccess(v -> log.info("Order created event published for order: {}",
                                                order.getId()))
                                .thenReturn(order);
        }

        /**
         * Get order by orderId (public facing ID)
         */
        public Mono<OrderResponse> getOrder(String orderId) {
                return orderRepository.findById(orderId)
                                .flatMap(this::enrichOrderWithItems)
                                .doOnSuccess(o -> {
                                        if (o != null) {
                                                log.info("Found order: {}", orderId);
                                        } else {
                                                log.warn("Order not found: {}", orderId);
                                        }
                                });
        }

        /**
         * Get all orders for a customer
         */
        public Flux<OrderResponse> getOrdersByCustomerId(String customerId) {
                return orderRepository.findByCustomerId(customerId)
                                .flatMap(this::enrichOrderWithItems)
                                .doOnComplete(() -> log.info("Retrieved orders for customer: {}", customerId));
        }

        /**
         * Update order status (called by event consumers)
         */
        @Transactional
        public Mono<Order> updateOrderStatus(String orderId, OrderStatus status) {
                return orderRepository.findById(orderId)
                                .flatMap(order -> {
                                        order.setStatus(status);
                                        order.setUpdatedAt(LocalDateTime.now());
                                        return orderRepository.save(order);
                                })
                                .doOnSuccess(o -> log.info("Updated order {} status to {}", orderId, status));
        }

        private Mono<OrderResponse> enrichOrderWithItems(Order order) {
                return orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .map(items -> toOrderResponse(order, items));
        }

        private OrderResponse toOrderResponse(Order order) {
                return OrderResponse.builder()
                                .orderId(order.getId())
                                .customerId(order.getCustomerId())
                                .status(order.getStatus())
                                .totalAmount(order.getTotalAmount())
                                .createdAt(order.getCreatedAt())
                                .updatedAt(order.getUpdatedAt())
                                .build();
        }

        private OrderResponse toOrderResponse(Order order, List<OrderItem> items) {
                List<OrderResponse.OrderItemResponse> itemResponses = items.stream()
                                .map(item -> OrderResponse.OrderItemResponse.builder()
                                                .productId(item.getProductId())
                                                .quantity(item.getQuantity())
                                                .build())
                                .collect(Collectors.toList());

                return OrderResponse.builder()
                                .orderId(order.getId())
                                .customerId(order.getCustomerId())
                                .status(order.getStatus())
                                .totalAmount(order.getTotalAmount())
                                .items(itemResponses)
                                .createdAt(order.getCreatedAt())
                                .updatedAt(order.getUpdatedAt())
                                .build();
        }
}
