package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.StoreStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for StoreStatus enum
 * Converts StoreStatus (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class StoreStatusConverter implements AttributeConverter<StoreStatus, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(StoreStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public StoreStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : StoreStatus.fromCode(dbData);
    }
}
