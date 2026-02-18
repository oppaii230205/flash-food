package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Store type enumeration
 * DB: 1, 2, 3... | Backend: BAKERY, RESTAURANT... | Client: "bakery", "restaurant"...
 */
@Getter
@RequiredArgsConstructor
public enum StoreType implements PersistableEnum<StoreType> {
    BAKERY(1, "bakery"),                      // Tiệm bánh
    RESTAURANT(2, "restaurant"),              // Nhà hàng
    CONVENIENCE_STORE(3, "convenience_store"), // Cửa hàng tiện lợi (Circle K, FamilyMart)
    CAFE(4, "cafe"),                          // Quán cafe
    FAST_FOOD(5, "fast_food"),                // Đồ ăn nhanh
    BUFFET(6, "buffet"),                      // Buffet
    OTHER(7, "other");                        // Khác
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static StoreType fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(StoreType.class, displayName);
    }
    
    public static StoreType fromValue(Integer value) {
        return PersistableEnum.fromValue(StoreType.class, value);
    }
}
