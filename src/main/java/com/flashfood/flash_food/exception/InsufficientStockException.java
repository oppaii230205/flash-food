package com.flashfood.flash_food.exception;

/**
 * Exception thrown when insufficient stock for an order
 */
public class InsufficientStockException extends BaseException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String foodItemName, Integer requested, Integer available) {
        super(String.format("Insufficient stock for '%s'. Requested: %d, Available: %d", 
                foodItemName, requested, available));
    }
}
