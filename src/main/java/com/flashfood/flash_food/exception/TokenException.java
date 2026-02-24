package com.flashfood.flash_food.exception;

/**
 * Thrown when a refresh token is invalid, expired, or has been revoked.
 * Maps to HTTP 401 Unauthorized in the global exception handler.
 */
public class TokenException extends BaseException {

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
