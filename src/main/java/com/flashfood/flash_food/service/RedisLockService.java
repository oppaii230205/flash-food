package com.flashfood.flash_food.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for Redis distributed locking
 * Used to prevent overselling during high concurrency
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_LOCK_TIMEOUT = 10; // seconds
    
    /**
     * Try to acquire a distributed lock
     * @param key Lock key
     * @param value Lock value (usually a unique identifier)
     * @param timeoutSeconds Lock timeout in seconds
     * @return true if lock acquired, false otherwise
     */
    public boolean tryLock(String key, String value, long timeoutSeconds) {
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(LOCK_PREFIX + key, value, timeoutSeconds, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(result)) {
                log.debug("Lock acquired for key: {}", key);
                return true;
            } else {
                log.debug("Failed to acquire lock for key: {}", key);
                return false;
            }
        } catch (Exception e) {
            log.error("Error acquiring lock for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Try to acquire lock with default timeout
     */
    public boolean tryLock(String key, String value) {
        return tryLock(key, value, DEFAULT_LOCK_TIMEOUT);
    }
    
    /**
     * Release a distributed lock
     * @param key Lock key
     * @param value Lock value to verify ownership
     */
    public void releaseLock(String key, String value) {
        try {
            Object currentValue = redisTemplate.opsForValue().get(LOCK_PREFIX + key);
            
            // Only release if the lock is owned by this value
            if (value.equals(currentValue)) {
                redisTemplate.delete(LOCK_PREFIX + key);
                log.debug("Lock released for key: {}", key);
            } else {
                log.warn("Attempted to release lock not owned by this value: {}", key);
            }
        } catch (Exception e) {
            log.error("Error releasing lock for key: {}", key, e);
        }
    }
    
    /**
     * Check if a lock exists
     */
    public boolean isLocked(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(LOCK_PREFIX + key));
        } catch (Exception e) {
            log.error("Error checking lock for key: {}", key, e);
            return false;
        }
    }
}
