package com.flashfood.flash_food.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service for Redis Geo-spatial operations
 * Used for finding nearby stores based on user location
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisGeoService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String STORE_GEO_KEY = "geo:stores";
    private static final String USER_GEO_KEY = "geo:users";
    
    /**
     * Add store location to Redis Geo index
     */
    public void addStoreLocation(Long storeId, Double longitude, Double latitude) {
        try {
            Point point = new Point(longitude, latitude);
            redisTemplate.opsForGeo().add(STORE_GEO_KEY, point, storeId.toString());
            log.info("Added store {} to geo index at ({}, {})", storeId, longitude, latitude);
        } catch (Exception e) {
            log.error("Error adding store location to Redis", e);
        }
    }
    
    /**
     * Add user location to Redis Geo index
     */
    public void addUserLocation(Long userId, Double longitude, Double latitude) {
        try {
            Point point = new Point(longitude, latitude);
            redisTemplate.opsForGeo().add(USER_GEO_KEY, point, userId.toString());
            log.info("Added user {} to geo index at ({}, {})", userId, longitude, latitude);
        } catch (Exception e) {
            log.error("Error adding user location to Redis", e);
        }
    }
    
    /**
     * Find stores within a certain radius from a point
     * @param longitude User's longitude
     * @param latitude User's latitude
     * @param radiusInKm Radius in kilometers
     * @return List of store IDs within the radius
     */
    public List<Long> findNearbyStores(Double longitude, Double latitude, Double radiusInKm) {
        try {
            Point center = new Point(longitude, latitude);
            Distance radius = new Distance(radiusInKm, Metrics.KILOMETERS);
            
            Circle area = new Circle(center, radius);
            
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeDistance()
                    .sortAscending();
            
            GeoResults<RedisGeoCommands.GeoLocation<Object>> results = 
                    redisTemplate.opsForGeo().radius(STORE_GEO_KEY, area, args);
            
            List<Long> storeIds = new ArrayList<>();
            if (results != null) {
                results.getContent().forEach(result -> {
                    String storeId = result.getContent().getName().toString();
                    storeIds.add(Long.parseLong(storeId));
                });
            }
            
            log.info("Found {} stores within {} km", storeIds.size(), radiusInKm);
            return storeIds;
        } catch (Exception e) {
            log.error("Error finding nearby stores", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Find users within a certain radius from a store
     * Used for sending flash sale notifications
     */
    public List<Long> findNearbyUsers(Double longitude, Double latitude, Double radiusInKm) {
        try {
            Point center = new Point(longitude, latitude);
            Distance radius = new Distance(radiusInKm, Metrics.KILOMETERS);
            
            Circle area = new Circle(center, radius);
            
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeDistance()
                    .sortAscending();
            
            GeoResults<RedisGeoCommands.GeoLocation<Object>> results = 
                    redisTemplate.opsForGeo().radius(USER_GEO_KEY, area, args);
            
            List<Long> userIds = new ArrayList<>();
            if (results != null) {
                results.getContent().forEach(result -> {
                    String userId = result.getContent().getName().toString();
                    userIds.add(Long.parseLong(userId));
                });
            }
            
            log.info("Found {} users within {} km", userIds.size(), radiusInKm);
            return userIds;
        } catch (Exception e) {
            log.error("Error finding nearby users", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Remove store from geo index
     */
    public void removeStoreLocation(Long storeId) {
        try {
            redisTemplate.opsForGeo().remove(STORE_GEO_KEY, storeId.toString());
            log.info("Removed store {} from geo index", storeId);
        } catch (Exception e) {
            log.error("Error removing store location from Redis", e);
        }
    }
    
    /**
     * Get distance between two points
     */
    public Double getDistance(Double lon1, Double lat1, Double lon2, Double lat2) {
        try {
            Distance distance = redisTemplate.opsForGeo().distance(
                    STORE_GEO_KEY,
                    lon1 + "," + lat1,
                    lon2 + "," + lat2,
                    Metrics.KILOMETERS
            );
            return distance != null ? distance.getValue() : null;
        } catch (Exception e) {
            log.error("Error calculating distance", e);
            return null;
        }
    }
}
