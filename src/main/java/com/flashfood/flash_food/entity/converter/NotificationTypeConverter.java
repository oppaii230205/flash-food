package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.NotificationType;
import jakarta.persistence.Converter;

/**
 * JPA Converter for NotificationType enum
 */
@Converter(autoApply = true)
public class NotificationTypeConverter extends AbstractEnumConverter<NotificationType> {
    public NotificationTypeConverter() {
        super(NotificationType.class);
    }
}
