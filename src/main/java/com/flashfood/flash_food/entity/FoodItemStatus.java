package com.flashfood.flash_food.entity;

/**
 * Food item status enumeration
 */
public enum FoodItemStatus {
    PENDING,        // Pending flash sale
    AVAILABLE,      // Available for purchase
    SOLD_OUT,       // Sold out
    EXPIRED,        // Sale period expired
    CANCELLED       // Cancelled by store
}
