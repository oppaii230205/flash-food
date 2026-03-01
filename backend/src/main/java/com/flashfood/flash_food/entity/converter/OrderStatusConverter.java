package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for OrderStatus enum
 * Converts OrderStatus (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(OrderStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public OrderStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : OrderStatus.fromCode(dbData);
    }
}
