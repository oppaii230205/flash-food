package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.OrderStatus;
import jakarta.persistence.Converter;

/**
 * JPA Converter for OrderStatus enum
 */
@Converter(autoApply = true)
public class OrderStatusConverter extends AbstractEnumConverter<OrderStatus> {
    public OrderStatusConverter() {
        super(OrderStatus.class);
    }
}
