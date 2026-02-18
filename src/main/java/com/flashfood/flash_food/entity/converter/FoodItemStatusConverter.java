package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.FoodItemStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for FoodItemStatus enum
 * Converts FoodItemStatus (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class FoodItemStatusConverter implements AttributeConverter<FoodItemStatus, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(FoodItemStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public FoodItemStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : FoodItemStatus.fromCode(dbData);
    }
}
