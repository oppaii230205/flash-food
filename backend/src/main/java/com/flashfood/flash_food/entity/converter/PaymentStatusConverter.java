package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for PaymentStatus enum
 * Converts PaymentStatus (enum) <-> Integer (database)
 */
@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(PaymentStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    
    @Override
    public PaymentStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : PaymentStatus.fromCode(dbData);
    }
}
