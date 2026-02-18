package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.PaymentMethod;
import jakarta.persistence.Converter;

/**
 * JPA Converter for PaymentMethod enum
 */
@Converter(autoApply = true)
public class PaymentMethodConverter extends AbstractEnumConverter<PaymentMethod> {
    public PaymentMethodConverter() {
        super(PaymentMethod.class);
    }
}
