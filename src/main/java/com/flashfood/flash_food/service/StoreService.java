package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.request.CreateStoreRequest;
import com.flashfood.flash_food.dto.response.StoreResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Store operations
 */
public interface StoreService {
    
    /**
     * Create a new store (for current logged-in user as owner)
     * @param request Store data
     * @return Created store
     */
    StoreResponse createStore(CreateStoreRequest request);
    
    /**
     * Update an existing store (owner or admin only)
     * @param id Store ID
     * @param request Updated store data
     * @return Updated store
     */
    StoreResponse updateStore(Long id, CreateStoreRequest request);
    
    /**
     * Delete a store (owner or admin only)
     * @param id Store ID
     */
    void deleteStore(Long id);
    
    /**
     * Find store by ID
     * @param id Store ID
     * @return Store details
     */
    StoreResponse findById(Long id);
    
    /**
     * Find all stores with pagination
     * @param pageable Pagination parameters
     * @return Page of stores
     */
    Page<StoreResponse> findAll(Pageable pageable);
    
    /**
     * Find stores by type
     * @param type Store type (as string from client)
     * @param pageable Pagination parameters
     * @return Page of stores
     */
    Page<StoreResponse> findByType(String type, Pageable pageable);
    
    /**
     * Find stores by status
     * @param status Store status (as string from client)
     * @param pageable Pagination parameters
     * @return Page of stores
     */
    Page<StoreResponse> findByStatus(String status, Pageable pageable);
    
    /**
     * Find nearby stores within radius
     * @param latitude User latitude
     * @param longitude User longitude
     * @param radiusInKm Radius in kilometers
     * @return List of nearby stores
     */
    List<StoreResponse> findNearbyStores(Double latitude, Double longitude, Double radiusInKm);
    
    /**
     * Find stores by owner (current logged-in user)
     * @return List of stores owned by current user
     */
    List<StoreResponse> findMyStores();
    
    /**
     * Update store status (admin or owner only)
     * @param id Store ID
     * @param status New status (as string from client)
     * @return Updated store
     */
    StoreResponse updateStoreStatus(Long id, String status);
}
