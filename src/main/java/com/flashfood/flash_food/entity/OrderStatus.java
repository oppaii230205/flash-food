package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Order status enumeration
 * DB: 1, 2, 3... | Backend: PENDING, CONFIRMED... | Client: "pending", "confirmed"...
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus implements PersistableEnum<OrderStatus> {
    PENDING(1, "pending"),       // Order placed, waiting for payment
    CONFIRMED(2, "confirmed"),   // Payment confirmed
    PREPARING(3, "preparing"),   // Store is preparing the order
    READY(4, "ready"),           // Order ready for pickup
    COMPLETED(5, "completed"),   // Order picked up and completed
    CANCELLED(6, "cancelled"),   // Order cancelled
    EXPIRED(7, "expired");       // Order expired (not picked up in time)
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static OrderStatus fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(OrderStatus.class, displayName);
    }
    
    public static OrderStatus fromValue(Integer value) {
        return PersistableEnum.fromValue(OrderStatus.class, value);
    }
}
