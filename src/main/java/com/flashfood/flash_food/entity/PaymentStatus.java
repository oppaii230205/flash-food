package com.flashfood.flash_food.entity;

/**
 * Payment status enumeration
 */
public enum PaymentStatus {
    PENDING,        // Waiting for payment
    PAID,           // Payment successful
    FAILED,         // Payment failed
    REFUNDED        // Payment refunded
}
