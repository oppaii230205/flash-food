package com.flashfood.flash_food.exception;

/**
 * Exception thrown when a resource already exists
 */
public class ResourceAlreadyExistsException extends FlashFoodException {
    
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
    
    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
