package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.StoreType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for StoreType enum
 * Converts StoreType (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class StoreTypeConverter implements AttributeConverter<StoreType, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(StoreType attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public StoreType convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : StoreType.fromCode(dbData);
    }
}
