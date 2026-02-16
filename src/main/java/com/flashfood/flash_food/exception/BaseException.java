package com.flashfood.flash_food.exception;

/**
 * Base exception for application-specific errors
 */
public class BaseException extends RuntimeException {
    
    public BaseException(String message) {
        super(message);
    }
    
    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
