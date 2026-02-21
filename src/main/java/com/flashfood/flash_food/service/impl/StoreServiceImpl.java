package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.CreateStoreRequest;
import com.flashfood.flash_food.dto.response.StoreResponse;
import com.flashfood.flash_food.entity.*;
import com.flashfood.flash_food.exception.InvalidOperationException;
import com.flashfood.flash_food.exception.ResourceNotFoundException;
import com.flashfood.flash_food.repository.StoreRepository;
import com.flashfood.flash_food.service.AuthenticationService;
import com.flashfood.flash_food.service.StoreService;
import com.flashfood.flash_food.service.RedisGeoService;
import com.flashfood.flash_food.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of StoreService
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {
    
    private final StoreRepository storeRepository;
    private final AuthenticationService authenticationService;
    private final RedisGeoService redisGeoService;
    private final EntityMapper entityMapper;
    
    @Override
    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        log.info("Creating new store: {}", request.getName());
        
        User currentUser = authenticationService.getCurrentUser();
        
        // Convert type string to enum
        StoreType storeType;
        try {
            storeType = StoreType.fromDisplayName(request.getType());
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid store type: " + request.getType());
        }
        
        Store store = Store.builder()
                .owner(currentUser)
                .name(request.getName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .type(storeType)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .flashSaleTime(request.getFlashSaleTime())
                .status(StoreStatus.PENDING_APPROVAL) // New stores need admin approval
                .rating(0.0)
                .totalRatings(0)
                .build();
        
        Store savedStore = storeRepository.save(store);
        
        // Add store to Redis Geo for location-based queries
        redisGeoService.addStoreLocation(savedStore.getId(), savedStore.getLongitude(), savedStore.getLatitude());
        
        log.info("Store created successfully with id: {}", savedStore.getId());
        return entityMapper.toStoreResponse(savedStore);
    }
    
    @Override
    @Transactional
    public StoreResponse updateStore(Long id, CreateStoreRequest request) {
        log.info("Updating store with id: {}", id);
        
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        
        // Convert type string to enum
        StoreType storeType;
        try {
            storeType = StoreType.fromDisplayName(request.getType());
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid store type: " + request.getType());
        }
        
        // Update fields
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhoneNumber(request.getPhoneNumber());
        store.setType(storeType);
        store.setDescription(request.getDescription());
        store.setImageUrl(request.getImageUrl());
        store.setOpenTime(request.getOpenTime());
        store.setCloseTime(request.getCloseTime());
        store.setFlashSaleTime(request.getFlashSaleTime());
        
        // Update location if changed
        boolean locationChanged = !store.getLatitude().equals(request.getLatitude()) 
                || !store.getLongitude().equals(request.getLongitude());
        
        if (locationChanged) {
            store.setLatitude(request.getLatitude());
            store.setLongitude(request.getLongitude());
            
            // Update Redis Geo location
            redisGeoService.addStoreLocation(store.getId(), request.getLongitude(), request.getLatitude());
        }
        
        Store updatedStore = storeRepository.save(store);
        log.info("Store updated successfully with id: {}", updatedStore.getId());
        
        return entityMapper.toStoreResponse(updatedStore);
    }
    
    @Override
    @Transactional
    public void deleteStore(Long id) {
        log.info("Deleting store with id: {}", id);
        
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        
        // Remove from Redis Geo
        redisGeoService.removeStoreLocation(store.getId());
        
        storeRepository.delete(store);
        log.info("Store deleted successfully with id: {}", id);
    }
    
    @Override
    public StoreResponse findById(Long id) {
        log.debug("Finding store by id: {}", id);
        
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        
        return entityMapper.toStoreResponse(store);
    }
    
    @Override
    public Page<StoreResponse> findAll(Pageable pageable) {
        log.debug("Finding all stores with pagination: {}", pageable);
        
        Page<Store> stores = storeRepository.findAll(pageable);
        return stores.map(entityMapper::toStoreResponse);
    }
    
    @Override
    public Page<StoreResponse> findByType(String type, Pageable pageable) {
        log.debug("Finding stores by type: {}", type);
        
        StoreType storeType;
        try {
            storeType = StoreType.fromDisplayName(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid store type: " + type);
        }
        
        Page<Store> stores = storeRepository.findByType(storeType, pageable);
        return stores.map(entityMapper::toStoreResponse);
    }
    
    @Override
    public Page<StoreResponse> findByStatus(String status, Pageable pageable) {
        log.debug("Finding stores by status: {}", status);
        
        StoreStatus storeStatus;
        try {
            storeStatus = StoreStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid store status: " + status);
        }
        
        Page<Store> stores = storeRepository.findByStatus(storeStatus, pageable);
        return stores.map(entityMapper::toStoreResponse);
    }
    
    @Override
    public List<StoreResponse> findNearbyStores(Double latitude, Double longitude, Double radiusInKm) {
        log.debug("Finding stores near location: lat={}, lon={}, radius={}km", latitude, longitude, radiusInKm);
        
        // Get store IDs from Redis Geo
        List<Long> storeIds = redisGeoService.findNearbyStores(longitude, latitude, radiusInKm);
        
        if (storeIds.isEmpty()) {
            return List.of();
        }
        
        // Fetch stores from database and filter by ACTIVE status
        List<Store> stores = storeRepository.findByIdInAndStatus(storeIds, StoreStatus.ACTIVE);
        
        // Calculate distance and map to response
        return stores.stream()
                .map(store -> {
                    StoreResponse response = entityMapper.toStoreResponse(store);
                    // Calculate distance (simple approximation)
                    double distance = calculateDistance(latitude, longitude, store.getLatitude(), store.getLongitude());
                    response.setDistance(distance);
                    return response;
                })
                .sorted((s1, s2) -> Double.compare(s1.getDistance(), s2.getDistance()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StoreResponse> findMyStores() {
        log.debug("Finding stores for current user");
        
        User currentUser = authenticationService.getCurrentUser();
        List<Store> stores = storeRepository.findByOwner(currentUser);
        
        return stores.stream()
                .map(entityMapper::toStoreResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public StoreResponse updateStoreStatus(Long id, String status) {
        log.info("Updating store status for id: {} to {}", id, status);
        
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        
        StoreStatus storeStatus;
        try {
            storeStatus = StoreStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid store status: " + status);
        }
        
        store.setStatus(storeStatus);
        Store updatedStore = storeRepository.save(store);
        
        log.info("Store status updated successfully for id: {}", id);
        return entityMapper.toStoreResponse(updatedStore);
    }
    
    /**
     * Calculate distance between two coordinates in meters using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the Earth in meters
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in meters
    }
}
