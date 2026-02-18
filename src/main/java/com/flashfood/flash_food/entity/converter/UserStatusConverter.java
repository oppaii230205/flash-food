package com.flashfood.flash_food.entity.converter;

import com.flashfood.flash_food.entity.UserStatus;
import jakarta.persistence.Converter;

/**
 * JPA Converter for UserStatus enum
 */
@Converter(autoApply = true)
public class UserStatusConverter extends AbstractEnumConverter<UserStatus> {
    public UserStatusConverter() {
        super(UserStatus.class);
    }
}
