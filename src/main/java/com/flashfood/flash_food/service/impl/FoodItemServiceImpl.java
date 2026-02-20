package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.FoodItemRequest;
import com.flashfood.flash_food.dto.response.FoodItemResponse;
import com.flashfood.flash_food.entity.*;
import com.flashfood.flash_food.exception.InvalidOperationException;
import com.flashfood.flash_food.exception.ResourceNotFoundException;
import com.flashfood.flash_food.util.EntityMapper;
import com.flashfood.flash_food.repository.CategoryRepository;
import com.flashfood.flash_food.repository.FoodItemRepository;
import com.flashfood.flash_food.repository.StoreRepository;
import com.flashfood.flash_food.service.AuthenticationService;
import com.flashfood.flash_food.service.FoodItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Implementation of FoodItemService
 * Handles flash sale items with stock management and time-based availability
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodItemServiceImpl implements FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final AuthenticationService authenticationService;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public FoodItemResponse createFoodItem(Long storeId, FoodItemRequest request) {
        log.info("Creating food item for store ID: {}", storeId);

        // Find store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

        User currentUser = authenticationService.getCurrentUser();

        // Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Validate prices
        if (request.getFlashPrice().compareTo(request.getOriginalPrice()) >= 0) {
            throw new InvalidOperationException("Flash price must be lower than original price");
        }

        // Validate sale times
        if (request.getSaleEndTime().isBefore(request.getSaleStartTime())) {
            throw new InvalidOperationException("Sale end time must be after start time");
        }

        // Create food item
        FoodItem foodItem = new FoodItem();
        foodItem.setStore(store);
        foodItem.setCategory(category);
        foodItem.setName(request.getName());
        foodItem.setDescription(request.getDescription());
        foodItem.setImageUrl(request.getImageUrl());
        foodItem.setOriginalPrice(request.getOriginalPrice());
        foodItem.setFlashPrice(request.getFlashPrice());
        
        // Calculate discount percentage
        BigDecimal discount = calculateDiscountPercentage(request.getOriginalPrice(), request.getFlashPrice());
        foodItem.setDiscountPercentage(discount);
        
        foodItem.setTotalQuantity(request.getQuantity());
        foodItem.setAvailableQuantity(request.getQuantity());
        foodItem.setSaleStartTime(request.getSaleStartTime());
        foodItem.setSaleEndTime(request.getSaleEndTime());
        
        // Set initial status based on time and quantity
        foodItem.setStatus(determineInitialStatus(request.getSaleStartTime(), request.getQuantity()));
        foodItem.setIsExpired(false);

        FoodItem savedItem = foodItemRepository.save(foodItem);
        log.info("Food item created successfully with ID: {}", savedItem.getId());

        return entityMapper.toFoodItemResponse(savedItem);
    }

    @Override
    @Transactional
    public FoodItemResponse updateFoodItem(Long id, FoodItemRequest request) {
        log.info("Updating food item with ID: {}", id);

        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));

        // Validate category if changed
        if (!foodItem.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            foodItem.setCategory(category);
        }

        // Validate prices
        if (request.getFlashPrice().compareTo(request.getOriginalPrice()) >= 0) {
            throw new InvalidOperationException("Flash price must be lower than original price");
        }

        // Validate sale times
        if (request.getSaleEndTime().isBefore(request.getSaleStartTime())) {
            throw new InvalidOperationException("Sale end time must be after start time");
        }

        // Update fields
        foodItem.setName(request.getName());
        foodItem.setDescription(request.getDescription());
        foodItem.setImageUrl(request.getImageUrl());
        foodItem.setOriginalPrice(request.getOriginalPrice());
        foodItem.setFlashPrice(request.getFlashPrice());
        
        // Recalculate discount
        BigDecimal discount = calculateDiscountPercentage(request.getOriginalPrice(), request.getFlashPrice());
        foodItem.setDiscountPercentage(discount);
        
        // Update quantity - calculate difference  
        int quantityDiff = request.getQuantity() - foodItem.getTotalQuantity();
        foodItem.setTotalQuantity(request.getQuantity());
        foodItem.setAvailableQuantity(foodItem.getAvailableQuantity() + quantityDiff);
        
        // Ensure available quantity is not negative
        if (foodItem.getAvailableQuantity() < 0) {
            foodItem.setAvailableQuantity(0);
        }
        
        foodItem.setSaleStartTime(request.getSaleStartTime());
        foodItem.setSaleEndTime(request.getSaleEndTime());

        // Update status if needed
        updateStatusBasedOnConditions(foodItem);

        FoodItem updatedItem = foodItemRepository.save(foodItem);
        log.info("Food item updated successfully with ID: {}", updatedItem.getId());

        return entityMapper.toFoodItemResponse(updatedItem);
    }

    @Override
    @Transactional
    public void deleteFoodItem(Long id) {
        log.info("Deleting food item with ID: {}", id);

        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));

        // Soft delete by setting status to DELETED
        foodItem.setStatus(FoodItemStatus.DELETED);
        foodItemRepository.save(foodItem);

        log.info("Food item deleted successfully with ID: {}", id);
    }

    @Override
    public FoodItemResponse findById(Long id) {
        log.debug("Finding food item with ID: {}", id);

        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));

        return entityMapper.toFoodItemResponse(foodItem);
    }

    @Override
    public Page<FoodItemResponse> findAll(Pageable pageable) {
        log.debug("Finding all food items with pagination");

        Page<FoodItem> foodItems = foodItemRepository.findAll(pageable);
        return foodItems.map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findByStore(Long storeId, Pageable pageable) {
        log.debug("Finding food items for store ID: {}", storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

        Page<FoodItem> foodItems = foodItemRepository.findAll(pageable)
                .map(item -> item.getStore().equals(store) ? item : null)
                .map(item -> item);
        
        // Better approach: use custom repository method
        return foodItemRepository.findAll(pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findByCategory(Long categoryId, Pageable pageable) {
        log.debug("Finding food items for category ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        return foodItemRepository.findAll(pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findByStatus(String status, Pageable pageable) {
        log.debug("Finding food items with status: {}", status);

        FoodItemStatus foodItemStatus;
        try {
            foodItemStatus = FoodItemStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid food item status: " + status);
        }

        Page<FoodItem> foodItems = foodItemRepository.findAll(pageable)
                .map(item -> item.getStatus().equals(foodItemStatus) ? item : null)
                .map(item -> item);

        return foodItems.map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findAvailableFoodItems(Pageable pageable) {
        log.debug("Finding available food items");

        LocalDateTime now = LocalDateTime.now();

        return foodItemRepository.findAll(pageable)
                .map(item -> {
                    if (item.getStatus() == FoodItemStatus.AVAILABLE 
                            && item.getAvailableQuantity() > 0
                            && !item.getIsExpired()
                            && item.getSaleStartTime().isBefore(now)
                            && item.getSaleEndTime().isAfter(now)) {
                        return item;
                    }
                    return null;
                })
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findFlashSaleFoodItems(Pageable pageable) {
        log.debug("Finding flash sale food items");

        LocalDateTime now = LocalDateTime.now();

        return foodItemRepository.findAll(pageable)
                .map(item -> {
                    if (item.getStatus() == FoodItemStatus.AVAILABLE 
                            && item.getAvailableQuantity() > 0
                            && !item.getIsExpired()
                            && !item.getSaleStartTime().isAfter(now)
                            && !item.getSaleEndTime().isBefore(now)) {
                        return item;
                    }
                    return null;
                })
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> searchFoodItems(String keyword, Pageable pageable) {
        log.debug("Searching food items with keyword: {}", keyword);

        String lowerKeyword = keyword.toLowerCase();

        return foodItemRepository.findAll(pageable)
                .map(item -> {
                    if (item.getName().toLowerCase().contains(lowerKeyword) 
                            || (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerKeyword))) {
                        return item;
                    }
                    return null;
                })
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    @Transactional
    public FoodItemResponse updateStatus(Long id, String status) {
        log.info("Updating status for food item ID: {} to: {}", id, status);

        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));

        // Parse and validate status
        FoodItemStatus newStatus;
        try {
            newStatus = FoodItemStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid food item status: " + status);
        }

        foodItem.setStatus(newStatus);
        FoodItem updatedItem = foodItemRepository.save(foodItem);

        log.info("Food item status updated successfully");
        return entityMapper.toFoodItemResponse(updatedItem);
    }

    // ===== Helper Methods =====

    /**
     * Calculate discount percentage
     */
    private BigDecimal calculateDiscountPercentage(BigDecimal originalPrice, BigDecimal flashPrice) {
        if (originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal difference = originalPrice.subtract(flashPrice);
        BigDecimal percentage = difference.divide(originalPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return percentage.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Determine initial status based on start time and quantity
     */
    private FoodItemStatus determineInitialStatus(LocalDateTime saleStartTime, Integer quantity) {
        LocalDateTime now = LocalDateTime.now();
        
        if (quantity <= 0) {
            return FoodItemStatus.OUT_OF_STOCK;
        }
        
        if (saleStartTime.isAfter(now)) {
            return FoodItemStatus.PENDING;
        }
        
        return FoodItemStatus.AVAILABLE;
    }

    /**
     * Update status based on current conditions (time, quantity, expiry)
     */
    private void updateStatusBasedOnConditions(FoodItem foodItem) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if expired
        if (foodItem.getSaleEndTime().isBefore(now)) {
            foodItem.setIsExpired(true);
            foodItem.setStatus(FoodItemStatus.EXPIRED);
            return;
        }
        
        // Check stock
        if (foodItem.getAvailableQuantity() <= 0) {
            foodItem.setStatus(FoodItemStatus.OUT_OF_STOCK);
            return;
        }
        
        // Check if sale started
        if (foodItem.getSaleStartTime().isAfter(now)) {
            foodItem.setStatus(FoodItemStatus.PENDING);
            return;
        }
        
        // Otherwise available
        foodItem.setStatus(FoodItemStatus.AVAILABLE);
    }
}
