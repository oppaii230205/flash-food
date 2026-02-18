package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * User status enumeration
 * DB: 1, 2, 3, 4 | Backend: ACTIVE, INACTIVE, SUSPENDED, DELETED | Client: "active", "inactive", "suspended", "deleted"
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus implements PersistableEnum<UserStatus> {
    ACTIVE(1, "active"),       // Active user
    INACTIVE(2, "inactive"),   // Temporarily inactive
    SUSPENDED(3, "suspended"), // Suspended due to violations
    DELETED(4, "deleted");     // Soft deleted
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static UserStatus fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(UserStatus.class, displayName);
    }
    
    public static UserStatus fromValue(Integer value) {
        return PersistableEnum.fromValue(UserStatus.class, value);
    }
}
