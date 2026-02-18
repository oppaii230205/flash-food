package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * User role enumeration
 * DB: 1, 2, 3 | Backend: CUSTOMER, STORE_OWNER, ADMIN | Client: "customer", "store_owner", "admin"
 */
@Getter
@RequiredArgsConstructor
public enum UserRole implements PersistableEnum<UserRole> {
    CUSTOMER(1, "customer"),       // Regular customer
    STORE_OWNER(2, "store_owner"), // Store owner
    ADMIN(3, "admin");             // System administrator
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static UserRole fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(UserRole.class, displayName);
    }
    
    public static UserRole fromValue(Integer value) {
        return PersistableEnum.fromValue(UserRole.class, value);
    }
}
