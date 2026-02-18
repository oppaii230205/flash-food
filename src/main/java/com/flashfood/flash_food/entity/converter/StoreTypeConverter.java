package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.StoreType;
import jakarta.persistence.Converter;

/**
 * JPA Converter for StoreType enum
 */
@Converter(autoApply = true)
public class StoreTypeConverter extends AbstractEnumConverter<StoreType> {
    public StoreTypeConverter() {
        super(StoreType.class);
    }
}
