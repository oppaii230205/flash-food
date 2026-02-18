package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.NotificationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for NotificationType enum
 * Converts NotificationType (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class NotificationTypeConverter implements AttributeConverter<NotificationType, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(NotificationType attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public NotificationType convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : NotificationType.fromCode(dbData);
    }
}
