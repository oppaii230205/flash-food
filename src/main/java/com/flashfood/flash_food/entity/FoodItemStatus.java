package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Food item status enumeration
 * DB: 1, 2, 3... | Backend: PENDING, AVAILABLE... | Client: "pending", "available"...
 */
@Getter
@RequiredArgsConstructor
public enum FoodItemStatus implements PersistableEnum<FoodItemStatus> {
    PENDING(1, "pending"),       // Pending flash sale
    AVAILABLE(2, "available"),   // Available for purchase
    SOLD_OUT(3, "sold_out"),     // Sold out
    EXPIRED(4, "expired"),       // Sale period expired
    CANCELLED(5, "cancelled");   // Cancelled by store
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static FoodItemStatus fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(FoodItemStatus.class, displayName);
    }
    
    public static FoodItemStatus fromValue(Integer value) {
        return PersistableEnum.fromValue(FoodItemStatus.class, value);
    }
}
