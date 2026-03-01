package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Store status enumeration
 * DB: Integer (1, 2, 3, 4) | Backend: Enum (ACTIVE, INACTIVE...) | Client: String ("active", "inactive"...)
 */
public enum StoreStatus {
    ACTIVE(1, "active"),                   // Store is active
    INACTIVE(2, "inactive"),               // Temporarily inactive
    SUSPENDED(3, "suspended"),             // Suspended by admin
    PENDING_APPROVAL(4, "pending_approval"); // Waiting for approval
    
    private final int code;
    private final String displayName;
    
    private static final Map<Integer, StoreStatus> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(StoreStatus::getCode, e -> e));
                  
    private static final Map<String, StoreStatus> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    StoreStatus(int code, String displayName) {
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
    
    public static StoreStatus fromCode(int code) {
        StoreStatus status = CODE_LOOKUP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Invalid StoreStatus code: " + code);
        }
        return status;
    }
    
    @JsonCreator
    public static StoreStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("StoreStatus displayName cannot be null or empty");
        }
        StoreStatus status = NAME_LOOKUP.get(displayName.toLowerCase());
        if (status == null) {
            throw new IllegalArgumentException("Invalid StoreStatus name: " + displayName);
        }
        return status;
    }
}
