package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Notification type enumeration
 * DB: 1, 2, 3... | Backend: NEW_FLASH_SALE... | Client: "new_flash_sale"...
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType implements PersistableEnum<NotificationType> {
    NEW_FLASH_SALE(1, "new_flash_sale"),   // New flash sale nearby
    ORDER_CONFIRMED(2, "order_confirmed"), // Order confirmation
    ORDER_READY(3, "order_ready"),         // Order ready for pickup
    ORDER_CANCELLED(4, "order_cancelled"), // Order cancelled
    PROMOTION(5, "promotion"),             // Promotional notification
    SYSTEM(6, "system");                   // System notification
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static NotificationType fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(NotificationType.class, displayName);
    }
    
    public static NotificationType fromValue(Integer value) {
        return PersistableEnum.fromValue(NotificationType.class, value);
    }
}
