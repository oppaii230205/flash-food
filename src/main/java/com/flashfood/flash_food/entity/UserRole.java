package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User role enumeration
 * DB: Integer (1, 2, 3) | Backend: Enum (CUSTOMER, STORE_OWNER, ADMIN) | Client: String ("customer", "store_owner", "admin")
 */
public enum UserRole {
    CUSTOMER(1, "customer"),       // Regular customer
    STORE_OWNER(2, "store_owner"), // Store owner
    ADMIN(3, "admin");             // System administrator
    
    private final int code;
    private final String displayName;
    
    private static final Map<Integer, UserRole> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(UserRole::getCode, e -> e));
                  
    private static final Map<String, UserRole> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    UserRole(int code, String displayName) {
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
    
    public static UserRole fromCode(int code) {
        UserRole role = CODE_LOOKUP.get(code);
        if (role == null) {
            throw new IllegalArgumentException("Invalid UserRole code: " + code);
        }
        return role;
    }
    
    @JsonCreator
    public static UserRole fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("UserRole displayName cannot be null or empty");
        }
        UserRole role = NAME_LOOKUP.get(displayName.toLowerCase());
        if (role == null) {
            throw new IllegalArgumentException("Invalid UserRole name: " + displayName);
        }
        return role;
    }
}
