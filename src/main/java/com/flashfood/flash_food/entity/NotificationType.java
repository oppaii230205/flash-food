package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Notification type enumeration
 * DB: Integer (1, 2, 3...) | Backend: Enum (NEW_FLASH_SALE...) | Client: String ("new_flash_sale"...)
 */
public enum NotificationType {
    NEW_FLASH_SALE(1, "new_flash_sale"),   // New flash sale nearby
    ORDER_CONFIRMED(2, "order_confirmed"), // Order confirmation
    ORDER_READY(3, "order_ready"),         // Order ready for pickup
    ORDER_CANCELLED(4, "order_cancelled"), // Order cancelled
    PROMOTION(5, "promotion"),             // Promotional notification
    SYSTEM(6, "system");                   // System notification
    
    private final int code;
    private final String displayName;
    
    private static final Map<Integer, NotificationType> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(NotificationType::getCode, e -> e));
                  
    private static final Map<String, NotificationType> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    NotificationType(int code, String displayName) {
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
    
    public static NotificationType fromCode(int code) {
        NotificationType type = CODE_LOOKUP.get(code);
        if (type == null) {
            throw new IllegalArgumentException("Invalid NotificationType code: " + code);
        }
        return type;
    }
    
    @JsonCreator
    public static NotificationType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("NotificationType displayName cannot be null or empty");
        }
        NotificationType type = NAME_LOOKUP.get(displayName.toLowerCase());
        if (type == null) {
            throw new IllegalArgumentException("Invalid NotificationType name: " + displayName);
        }
        return type;
    }
}
