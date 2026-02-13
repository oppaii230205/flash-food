package com.flashfood.flash_food.exception;

/**
 * Base exception for application-specific errors
 */
public class FlashFoodException extends RuntimeException {
    
    public FlashFoodException(String message) {
        super(message);
    }
    
    public FlashFoodException(String message, Throwable cause) {
        super(message, cause);
    }
}
