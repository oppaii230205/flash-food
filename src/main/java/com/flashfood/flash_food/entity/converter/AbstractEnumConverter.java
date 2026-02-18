package com.flashfood.flash_food.entity.converter;

import jakarta.persistence.AttributeConverter;

/**
 * Base JPA converter for enums implementing PersistableEnum
 * Converts Enum <-> Integer for database storage
 * 
 * @param <E> The enum type
 */
public abstract class AbstractEnumConverter<E extends Enum<E> & PersistableEnum<E>> 
        implements AttributeConverter<E, Integer> {
    
    private final Class<E> enumClass;
    
    protected AbstractEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }
    
    @Override
    public Integer convertToDatabaseColumn(E attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }
    
    @Override
    public E convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return PersistableEnum.fromValue(enumClass, dbData);
    }
}
