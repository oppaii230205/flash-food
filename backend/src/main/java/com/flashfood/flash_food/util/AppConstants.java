package com.flashfood.flash_food.util;

/**
 * Application constants
 */
public final class AppConstants {
    
    private AppConstants() {
        // Prevent instantiation
    }
    
    // Pagination
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String MAX_PAGE_SIZE = "100";
    
    // Date/Time formats
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    
    // Flash Sale
    public static final double DEFAULT_NOTIFICATION_RADIUS_KM = 1.0;
    public static final int DEFAULT_FLASH_SALE_DISCOUNT_PERCENT = 70;
    public static final int MAX_FLASH_SALE_DURATION_HOURS = 3;
    
    // Order
    public static final int ORDER_EXPIRY_HOURS = 2;
    public static final int MAX_ITEMS_PER_ORDER = 20;
    
    // Redis Keys
    public static final String REDIS_FOOD_ITEM_CACHE_PREFIX = "food_item:";
    public static final String REDIS_STORE_CACHE_PREFIX = "store:";
    public static final String REDIS_USER_CACHE_PREFIX = "user:";
    
    // Redis Lock Keys
    public static final String LOCK_ORDER_PREFIX = "order:";
    public static final String LOCK_FOOD_ITEM_PREFIX = "food_item:";
    
    // API Response Messages
    public static final String SUCCESS_MESSAGE = "Operation completed successfully";
    public static final String ERROR_MESSAGE = "An error occurred";
    
    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;
}
