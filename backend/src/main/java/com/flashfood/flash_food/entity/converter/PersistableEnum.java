package com.flashfood.flash_food.entity.converter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Base interface for enums that can be converted between Integer (DB), Enum (Java), and String (Client)
 * 
 * Flow:
 * - Database: Stores as INTEGER
 * - Backend: Uses as ENUM
 * - Client: Sends/Receives as STRING
 * 
 * @param <E> The enum type
 */
public interface PersistableEnum<E extends Enum<E>> {
    
    /**
     * Get the integer value to store in database
     */
    Integer getValue();
    
    /**
     * Get the string representation for client communication
     */
    @JsonValue
    String getDisplayName();
    
    /**
     * Get enum constant from integer value (for loading from DB)
     */
    static <E extends Enum<E> & PersistableEnum<E>> E fromValue(Class<E> enumClass, Integer value) {
        if (value == null) {
            return null;
        }
        
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getValue().equals(value)) {
                return enumConstant;
            }
        }
        
        throw new IllegalArgumentException(
            String.format("No enum constant %s with value %d", enumClass.getSimpleName(), value)
        );
    }
    
    /**
     * Get enum constant from string display name (for parsing from client)
     */
    @JsonCreator
    static <E extends Enum<E> & PersistableEnum<E>> E fromDisplayName(Class<E> enumClass, String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getDisplayName().equalsIgnoreCase(displayName)) {
                return enumConstant;
            }
        }
        
        throw new IllegalArgumentException(
            String.format("No enum constant %s with display name '%s'", enumClass.getSimpleName(), displayName)
        );
    }
}
