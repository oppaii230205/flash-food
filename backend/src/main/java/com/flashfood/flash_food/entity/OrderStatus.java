package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Order status enumeration
 * DB: Integer (1, 2, 3...) | Backend: Enum (PENDING, CONFIRMED...) | Client: String ("pending", "confirmed"...)
 */
public enum OrderStatus {
    PENDING(1, "pending"),       // Order placed, waiting for payment
    CONFIRMED(2, "confirmed"),   // Payment confirmed
    PREPARING(3, "preparing"),   // Store is preparing the order
    READY(4, "ready"),           // Order ready for pickup
    COMPLETED(5, "completed"),   // Order picked up and completed
    CANCELLED(6, "cancelled"),   // Order cancelled
    EXPIRED(7, "expired");       // Order expired (not picked up in time)
    
    private final int code;
    private final String displayName;
    
    // Lookup maps for fast conversion
    private static final Map<Integer, OrderStatus> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(OrderStatus::getCode, e -> e));
                  
    private static final Map<String, OrderStatus> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    OrderStatus(int code, String displayName) {
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
    
    /**
     * Convert from database integer code to enum
     */
    public static OrderStatus fromCode(int code) {
        OrderStatus status = CODE_LOOKUP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Invalid OrderStatus code: " + code);
        }
        return status;
    }
    
    /**
     * Convert from client string to enum
     */
    @JsonCreator
    public static OrderStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("OrderStatus displayName cannot be null or empty");
        }
        OrderStatus status = NAME_LOOKUP.get(displayName.toLowerCase());
        if (status == null) {
            throw new IllegalArgumentException("Invalid OrderStatus name: " + displayName);
        }
        return status;
    }
}
