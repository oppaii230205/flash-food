package com.flashfood.flash_food.exception;

/**
 * Exception thrown for invalid business operations
 */
public class InvalidOperationException extends BaseException {
    
    public InvalidOperationException(String message) {
        super(message);
    }
}
