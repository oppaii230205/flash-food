package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.StoreStatus;
import jakarta.persistence.Converter;

/**
 * JPA Converter for StoreStatus enum
 */
@Converter(autoApply = true)
public class StoreStatusConverter extends AbstractEnumConverter<StoreStatus> {
    public StoreStatusConverter() {
        super(StoreStatus.class);
    }
}
