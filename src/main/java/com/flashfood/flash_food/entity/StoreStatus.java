package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Store status enumeration
 * DB: 1, 2, 3, 4 | Backend: ACTIVE, INACTIVE... | Client: "active", "inactive"...
 */
@Getter
@RequiredArgsConstructor
public enum StoreStatus implements PersistableEnum<StoreStatus> {
    ACTIVE(1, "active"),                   // Store is active
    INACTIVE(2, "inactive"),               // Temporarily inactive
    SUSPENDED(3, "suspended"),             // Suspended by admin
    PENDING_APPROVAL(4, "pending_approval"); // Waiting for approval
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static StoreStatus fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(StoreStatus.class, displayName);
    }
    
    public static StoreStatus fromValue(Integer value) {
        return PersistableEnum.fromValue(StoreStatus.class, value);
    }
}
