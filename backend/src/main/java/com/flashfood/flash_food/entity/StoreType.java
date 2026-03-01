package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Store type enumeration
 * DB: Integer (1, 2, 3...) | Backend: Enum (BAKERY, RESTAURANT...) | Client: String ("bakery", "restaurant"...)
 */
public enum StoreType {
    BAKERY(1, "bakery"),                       // Tiệm bánh
    RESTAURANT(2, "restaurant"),               // Nhà hàng
    CONVENIENCE_STORE(3, "convenience_store"), // Cửa hàng tiện lợi
    CAFE(4, "cafe"),                           // Quán cafe
    FAST_FOOD(5, "fast_food"),                 // Đồ ăn nhanh
    BUFFET(6, "buffet"),                       // Buffet
    OTHER(7, "other");                         // Khác
    
    private final int code;
    private final String displayName;
    
    private static final Map<Integer, StoreType> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(StoreType::getCode, e -> e));
                  
    private static final Map<String, StoreType> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    StoreType(int code, String displayName) {
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
    
    public static StoreType fromCode(int code) {
        StoreType type = CODE_LOOKUP.get(code);
        if (type == null) {
            throw new IllegalArgumentException("Invalid StoreType code: " + code);
        }
        return type;
    }
    
    @JsonCreator
    public static StoreType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("StoreType displayName cannot be null or empty");
        }
        StoreType type = NAME_LOOKUP.get(displayName.toLowerCase());
        if (type == null) {
            throw new IllegalArgumentException("Invalid StoreType name: " + displayName);
        }
        return type;
    }
}
