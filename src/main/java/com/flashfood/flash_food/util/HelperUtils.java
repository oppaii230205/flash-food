package com.flashfood.flash_food.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility helper methods
 */
public final class HelperUtils {
    
    private HelperUtils() {
        // Prevent instantiation
    }
    
    /**
     * Generate unique order number
     */
    public static String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Calculate discount percentage
     */
    public static Integer calculateDiscountPercentage(Double originalPrice, Double discountedPrice) {
        if (originalPrice == null || discountedPrice == null || originalPrice == 0) {
            return 0;
        }
        
        double discount = ((originalPrice - discountedPrice) / originalPrice) * 100;
        return (int) Math.round(discount);
    }
    
    /**
     * Calculate time remaining in seconds
     */
    public static Long calculateTimeRemainingSeconds(LocalDateTime targetTime) {
        if (targetTime == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(targetTime)) {
            return 0L;
        }
        
        return Duration.between(now, targetTime).getSeconds();
    }
    
    /**
     * Check if time is in range
     */
    public static boolean isTimeInRange(LocalDateTime now, LocalDateTime start, LocalDateTime end) {
        return now.isAfter(start) && now.isBefore(end);
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Format distance for display
     */
    public static String formatDistance(Double distanceInKm) {
        if (distanceInKm == null) {
            return "N/A";
        }
        
        if (distanceInKm < 1) {
            return String.format("%.0f m", distanceInKm * 1000);
        } else {
            return String.format("%.1f km", distanceInKm);
        }
    }
}
