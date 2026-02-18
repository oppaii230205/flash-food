package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Payment status enumeration
 * DB: 1, 2, 3, 4 | Backend: PENDING, PAID... | Client: "pending", "paid"...
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus implements PersistableEnum<PaymentStatus> {
    PENDING(1, "pending"),   // Waiting for payment
    PAID(2, "paid"),         // Payment successful
    FAILED(3, "failed"),     // Payment failed
    REFUNDED(4, "refunded"); // Payment refunded
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static PaymentStatus fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(PaymentStatus.class, displayName);
    }
    
    public static PaymentStatus fromValue(Integer value) {
        return PersistableEnum.fromValue(PaymentStatus.class, value);
    }
}
