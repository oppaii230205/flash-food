package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User status enumeration
 * DB: Integer (1, 2, 3, 4) | Backend: Enum (ACTIVE, INACTIVE...) | Client: String ("active", "inactive"...)
 */
public enum UserStatus {
    ACTIVE(1, "active"),       // Active user
    INACTIVE(2, "inactive"),   // Temporarily inactive
    SUSPENDED(3, "suspended"), // Suspended due to violations
    DELETED(4, "deleted");     // Soft deleted
    
    private final int code;
    private final String displayName;
    
    private static final Map<Integer, UserStatus> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(UserStatus::getCode, e -> e));
                  
    private static final Map<String, UserStatus> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    UserStatus(int code, String displayName) {
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
    
    public static UserStatus fromCode(int code) {
        UserStatus status = CODE_LOOKUP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Invalid UserStatus code: " + code);
        }
        return status;
    }
    
    @JsonCreator
    public static UserStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("UserStatus displayName cannot be null or empty");
        }
        UserStatus status = NAME_LOOKUP.get(displayName.toLowerCase());
        if (status == null) {
            throw new IllegalArgumentException("Invalid UserStatus name: " + displayName);
        }
        return status;
    }
}
