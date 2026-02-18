package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.UserRole;
import jakarta.persistence.Converter;

/**
 * JPA Converter for UserRole enum
 * Converts UserRole <-> Integer for database storage
 */
@Converter(autoApply = true)
public class UserRoleConverter extends AbstractEnumConverter<UserRole> {
    public UserRoleConverter() {
        super(UserRole.class);
    }
}
