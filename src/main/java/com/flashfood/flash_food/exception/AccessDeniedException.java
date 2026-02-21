package com.flashfood.flash_food.exception;

/**
 * Exception thrown when a user attempts to access a resource they are not authorized to access.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
