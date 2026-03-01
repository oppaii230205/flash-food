package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.request.FoodItemRequest;
import com.flashfood.flash_food.dto.response.FoodItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for FoodItem operations
 */
public interface FoodItemService {
    
    /**
     * Create a new food item for a store
     * @param storeId Store ID
     * @param request Food item data
     * @return Created food item
     */
    FoodItemResponse createFoodItem(Long storeId, FoodItemRequest request);
    
    /**
     * Update an existing food item
     * @param id Food item ID
     * @param request Updated food item data
     * @return Updated food item
     */
    FoodItemResponse updateFoodItem(Long id, FoodItemRequest request);
    
    /**
     * Delete a food item
     * @param id Food item ID
     */
    void deleteFoodItem(Long id);
    
    /**
     * Find food item by ID
     * @param id Food item ID
     * @return Food item details
     */
    FoodItemResponse findById(Long id);
    
    /**
     * Find all food items with pagination
     * @param pageable Pagination parameters
     * @return Page of food items
     */
    Page<FoodItemResponse> findAll(Pageable pageable);
    
    /**
     * Find food items by store
     * @param storeId Store ID
     * @param pageable Pagination parameters
     * @return Page of food items
     */
    Page<FoodItemResponse> findByStore(Long storeId, Pageable pageable);
    
    /**
     * Find food items by category
     * @param categoryId Category ID
     * @param pageable Pagination parameters
     * @return Page of food items
     */
    Page<FoodItemResponse> findByCategory(Long categoryId, Pageable pageable);
    
    /**
     * Find food items by status
     * @param status Status as string
     * @param pageable Pagination parameters
     * @return Page of food items
     */
    Page<FoodItemResponse> findByStatus(String status, Pageable pageable);
    
    /**
     * Find available food items (status = AVAILABLE, not expired)
     * @param pageable Pagination parameters
     * @return Page of available food items
     */
    Page<FoodItemResponse> findAvailableFoodItems(Pageable pageable);
    
    /**
     * Find food items on flash sale now (within sale period)
     * @param pageable Pagination parameters
     * @return Page of flash sale food items
     */
    Page<FoodItemResponse> findFlashSaleFoodItems(Pageable pageable);
    
    /**
     * Search food items by name or description
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Page of matching food items
     */
    Page<FoodItemResponse> searchFoodItems(String keyword, Pageable pageable);
    
    /**
     * Update food item status
     * @param id Food item ID
     * @param status New status as string
     * @return Updated food item
     */
    FoodItemResponse updateStatus(Long id, String status);
}
