package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Food item status enumeration
 * DB: Integer (1, 2, 3...) | Backend: Enum (PENDING, AVAILABLE...) | Client: String ("pending", "available"...)
 */
public enum FoodItemStatus {
    PENDING(1, "pending"),       // Pending flash sale
    AVAILABLE(2, "available"),   // Available for purchase
    SOLD_OUT(3, "sold_out"),     // Sold out
    EXPIRED(4, "expired"),       // Sale period expired
    CANCELLED(5, "cancelled"),   // Cancelled by store
    DELETED(6, "deleted");       // Soft-deleted
    
    private final int code;
    private final String displayName;
    
    private static final Map<Integer, FoodItemStatus> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(FoodItemStatus::getCode, e -> e));
                  
    private static final Map<String, FoodItemStatus> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    FoodItemStatus(int code, String displayName) {
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
    
    public static FoodItemStatus fromCode(int code) {
        FoodItemStatus status = CODE_LOOKUP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Invalid FoodItemStatus code: " + code);
        }
        return status;
    }
    
    @JsonCreator
    public static FoodItemStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("FoodItemStatus displayName cannot be null or empty");
        }
        FoodItemStatus status = NAME_LOOKUP.get(displayName.toLowerCase());
        if (status == null) {
            throw new IllegalArgumentException("Invalid FoodItemStatus name: " + displayName);
        }
        return status;
    }
}
