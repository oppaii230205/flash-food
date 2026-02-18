package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for UserRole enum
 * Converts UserRole (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(UserRole attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public UserRole convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : UserRole.fromCode(dbData);
    }
}
