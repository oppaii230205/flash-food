package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for UserStatus enum
 * Converts UserStatus (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(UserStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public UserStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : UserStatus.fromCode(dbData);
    }
}
