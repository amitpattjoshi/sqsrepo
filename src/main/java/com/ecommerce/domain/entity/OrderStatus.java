package com.ecommerce.domain.entity;

/**
 * Enum representing the various states an order can be in during its lifecycle.
 */
public enum OrderStatus {
    PENDING, // Order created, awaiting inventory check
    INVENTORY_RESERVED, // Stock has been reserved
    PAYMENT_PENDING, // Awaiting payment processing
    PAYMENT_COMPLETED, // Payment successful
    PAYMENT_FAILED, // Payment failed
    SHIPPED, // Order has been shipped
    DELIVERED, // Order delivered to customer
    CANCELLED, // Order cancelled
    FAILED, // Order failed (inventory or other issues)
    COMPLETED // Order fully completed
}
