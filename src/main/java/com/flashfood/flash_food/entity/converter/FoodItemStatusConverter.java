package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.FoodItemStatus;
import jakarta.persistence.Converter;

/**
 * JPA Converter for FoodItemStatus enum
 */
@Converter(autoApply = true)
public class FoodItemStatusConverter extends AbstractEnumConverter<FoodItemStatus> {
    public FoodItemStatusConverter() {
        super(FoodItemStatus.class);
    }
}
