package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.PaymentMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for PaymentMethod enum
 * Converts PaymentMethod (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(PaymentMethod attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public PaymentMethod convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : PaymentMethod.fromCode(dbData);
    }
}
