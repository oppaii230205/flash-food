package com.flashfood.flash_food.entity;

/**
 * Notification type enumeration
 */
public enum NotificationType {
    NEW_FLASH_SALE,     // New flash sale nearby
    ORDER_CONFIRMED,    // Order confirmation
    ORDER_READY,        // Order ready for pickup
    ORDER_CANCELLED,    // Order cancelled
    PROMOTION,          // Promotional notification
    SYSTEM              // System notification
}
