package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment status enumeration
 * DB: Integer (1, 2, 3, 4) | Backend: Enum (PENDING, PAID...) | Client: String ("pending", "paid"...)
 */
public enum PaymentStatus {
    PENDING(1, "pending"),   // Waiting for payment
    PAID(2, "paid"),         // Payment successful
    FAILED(3, "failed"),     // Payment failed
    REFUNDED(4, "refunded"), // Payment refunded
    CANCELLED(5, "cancelled"); // Payment cancelled (order was cancelled)
    
    private final int code;
    private final String displayName;
    
    private static final Map<Integer, PaymentStatus> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(PaymentStatus::getCode, e -> e));
                  
    private static final Map<String, PaymentStatus> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    PaymentStatus(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public int getCode() {
        return code;
    }
    
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    
    public static PaymentStatus fromCode(int code) {
        PaymentStatus status = CODE_LOOKUP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Invalid PaymentStatus code: " + code);
        }
        return status;
    }
    
    @JsonCreator
    public static PaymentStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("PaymentStatus displayName cannot be null or empty");
        }
        PaymentStatus status = NAME_LOOKUP.get(displayName.toLowerCase());
        if (status == null) {
            throw new IllegalArgumentException("Invalid PaymentStatus name: " + displayName);
        }
        return status;
    }
}
