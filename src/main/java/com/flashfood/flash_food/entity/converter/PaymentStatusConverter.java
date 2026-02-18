package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.PaymentStatus;
import jakarta.persistence.Converter;

/**
 * JPA Converter for PaymentStatus enum
 */
@Converter(autoApply = true)
public class PaymentStatusConverter extends AbstractEnumConverter<PaymentStatus> {
    public PaymentStatusConverter() {
        super(PaymentStatus.class);
    }
}
