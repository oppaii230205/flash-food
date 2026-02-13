package com.flashfood.flash_food.entity;

/**
 * Order status enumeration
 */
public enum OrderStatus {
    PENDING,        // Order placed, waiting for payment
    CONFIRMED,      // Payment confirmed
    PREPARING,      // Store is preparing the order
    READY,          // Order ready for pickup
    COMPLETED,      // Order picked up and completed
    CANCELLED,      // Order cancelled
    EXPIRED         // Order expired (not picked up in time)
}
