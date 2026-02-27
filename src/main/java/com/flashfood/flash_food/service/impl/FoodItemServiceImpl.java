package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.FoodItemRequest;
import com.flashfood.flash_food.dto.response.FoodItemResponse;
import com.flashfood.flash_food.entity.*;
import com.flashfood.flash_food.exception.AccessDeniedException;
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

import java.time.LocalDateTime;

/**
 * Implementation of FoodItemService.
 *
 * Authorization rules:
 *  - ADMIN can create / update / delete / status-change any item.
 *  - STORE_OWNER can only act on items belonging to their own store.
 *  - Unauthenticated or CUSTOMER callers are blocked by @PreAuthorize at the
 *    controller layer; these service methods add a second, deeper check via
 *    {@link AuthenticationService#isStoreOwnerOrAdmin(Store)}.
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

    // =========================================================================
    // Write operations
    // =========================================================================

    @Override
    @Transactional
    public FoodItemResponse createFoodItem(Long storeId, FoodItemRequest request) {
        log.info("Creating food item for store ID: {}", storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

        // Only the store's owner or an admin may add items
        if (!authenticationService.isStoreOwnerOrAdmin(store)) {
            throw new AccessDeniedException("You do not have permission to create items for this store");
        }

        // Only approved / active stores can offer flash-sale items
        if (store.getStatus() != StoreStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "Cannot create food items for a store that is not active (current status: "
                    + store.getStatus().getDisplayName() + ")");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with ID: " + request.getCategoryId()));

        validatePricesAndTimes(request);

        FoodItem foodItem = FoodItem.builder()
                .store(store)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .originalPrice(request.getOriginalPrice())
                .flashPrice(request.getFlashPrice())
                .discountPercentage(calculateDiscountPercentage(request.getOriginalPrice(), request.getFlashPrice()))
                .totalQuantity(request.getQuantity())
                .availableQuantity(request.getQuantity())
                .saleStartTime(request.getSaleStartTime())
                .saleEndTime(request.getSaleEndTime())
                .status(determineInitialStatus(request.getSaleStartTime(), request.getQuantity()))
                .isExpired(false)
                .build();

        FoodItem saved = foodItemRepository.save(foodItem);
        log.info("Food item created with ID: {}", saved.getId());
        return entityMapper.toFoodItemResponse(saved);
    }

    @Override
    @Transactional
    public FoodItemResponse updateFoodItem(Long id, FoodItemRequest request) {
        log.info("Updating food item ID: {}", id);

        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));

        if (!authenticationService.isStoreOwnerOrAdmin(foodItem.getStore())) {
            throw new AccessDeniedException("You do not have permission to update this food item");
        }

        // Prevent editing permanently-finished items
        FoodItemStatus current = foodItem.getStatus();
        if (current == FoodItemStatus.DELETED || current == FoodItemStatus.CANCELLED) {
            throw new InvalidOperationException(
                    "Cannot update a food item with status: " + current.getDisplayName());
        }

        validatePricesAndTimes(request);

        // Update category if it has changed
        Long currentCategoryId = foodItem.getCategory() != null ? foodItem.getCategory().getId() : null;
        if (!request.getCategoryId().equals(currentCategoryId)) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with ID: " + request.getCategoryId()));
            foodItem.setCategory(category);
        }

        foodItem.setName(request.getName());
        foodItem.setDescription(request.getDescription());
        foodItem.setImageUrl(request.getImageUrl());
        foodItem.setOriginalPrice(request.getOriginalPrice());
        foodItem.setFlashPrice(request.getFlashPrice());
        foodItem.setDiscountPercentage(
                calculateDiscountPercentage(request.getOriginalPrice(), request.getFlashPrice()));

        // Recalculate available quantity proportionally to the total quantity change
        int quantityDiff = request.getQuantity() - foodItem.getTotalQuantity();
        int newAvailable = Math.max(0, foodItem.getAvailableQuantity() + quantityDiff);
        foodItem.setTotalQuantity(request.getQuantity());
        foodItem.setAvailableQuantity(newAvailable);

        foodItem.setSaleStartTime(request.getSaleStartTime());
        foodItem.setSaleEndTime(request.getSaleEndTime());

        // Re-derive the status from the updated conditions
        refreshStatus(foodItem);

        FoodItem updated = foodItemRepository.save(foodItem);
        log.info("Food item updated, ID: {}", updated.getId());
        return entityMapper.toFoodItemResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFoodItem(Long id) {
        log.info("Soft-deleting food item ID: {}", id);

        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));

        if (!authenticationService.isStoreOwnerOrAdmin(foodItem.getStore())) {
            throw new AccessDeniedException("You do not have permission to delete this food item");
        }

        foodItem.setStatus(FoodItemStatus.DELETED);
        foodItemRepository.save(foodItem);
        log.info("Food item soft-deleted, ID: {}", id);
    }

    @Override
    @Transactional
    public FoodItemResponse updateStatus(Long id, String status) {
        log.info("Updating status for food item ID: {} to: {}", id, status);

        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));

        if (!authenticationService.isStoreOwnerOrAdmin(foodItem.getStore())) {
            throw new AccessDeniedException("You do not have permission to update this food item's status");
        }

        FoodItemStatus newStatus;
        try {
            newStatus = FoodItemStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid food item status: " + status);
        }

        // STORE_OWNER may only cancel; ADMIN may set any status (except DELETED — use delete endpoint)
        if (!authenticationService.isAdmin()) {
            if (newStatus != FoodItemStatus.CANCELLED) {
                throw new AccessDeniedException(
                        "Store owners may only cancel food items; use the admin portal for other status transitions");
            }
        } else if (newStatus == FoodItemStatus.DELETED) {
            throw new InvalidOperationException(
                    "Use the DELETE endpoint to remove a food item; do not set status = deleted manually");
        }

        foodItem.setStatus(newStatus);
        FoodItem updated = foodItemRepository.save(foodItem);
        log.info("Food item status updated to '{}', ID: {}", newStatus.getDisplayName(), id);
        return entityMapper.toFoodItemResponse(updated);
    }

    // =========================================================================
    // Read operations
    // =========================================================================

    @Override
    public FoodItemResponse findById(Long id) {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));
        return entityMapper.toFoodItemResponse(foodItem);
    }

    @Override
    public Page<FoodItemResponse> findAll(Pageable pageable) {
        return foodItemRepository.findAll(pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findByStore(Long storeId, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store not found with ID: " + storeId);
        }
        return foodItemRepository.findByStoreId(storeId, pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
        }
        return foodItemRepository.findByCategoryId(categoryId, pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findByStatus(String status, Pageable pageable) {
        FoodItemStatus foodItemStatus;
        try {
            foodItemStatus = FoodItemStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid food item status: " + status);
        }
        return foodItemRepository.findByStatus(foodItemStatus, pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findAvailableFoodItems(Pageable pageable) {
        // All items with AVAILABLE status and positive stock (may or may not be in sale window)
        return foodItemRepository.findAvailableItems(FoodItemStatus.AVAILABLE, pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> findFlashSaleFoodItems(Pageable pageable) {
        // Items currently within their active sale window — the "live" flash-sale feed
        return foodItemRepository.findActiveFlashSaleItems(FoodItemStatus.AVAILABLE, LocalDateTime.now(), pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    @Override
    public Page<FoodItemResponse> searchFoodItems(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            throw new InvalidOperationException("Search keyword must not be blank");
        }
        return foodItemRepository.searchByNameOrDescription(keyword.trim(), pageable)
                .map(entityMapper::toFoodItemResponse);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private void validatePricesAndTimes(FoodItemRequest request) {
        if (request.getFlashPrice() >= request.getOriginalPrice()) {
            throw new InvalidOperationException("Flash price must be lower than original price");
        }
        if (!request.getSaleEndTime().isAfter(request.getSaleStartTime())) {
            throw new InvalidOperationException("Sale end time must be after sale start time");
        }
    }

    private Integer calculateDiscountPercentage(Integer originalPrice, Integer flashPrice) {
        if (originalPrice == null || originalPrice == 0) return 0;
        return (int) Math.round((double) (originalPrice - flashPrice) / originalPrice * 100);
    }

    /**
     * Derives the initial status when a food item is first created.
     */
    private FoodItemStatus determineInitialStatus(LocalDateTime saleStartTime, Integer quantity) {
        if (quantity <= 0) return FoodItemStatus.SOLD_OUT;
        return saleStartTime.isAfter(LocalDateTime.now())
                ? FoodItemStatus.PENDING
                : FoodItemStatus.AVAILABLE;
    }

    /**
     * Re-evaluates an existing item's status based on its current time, stock,
     * and expiry state. Call after any update that could change these conditions.
     */
    private void refreshStatus(FoodItem foodItem) {
        LocalDateTime now = LocalDateTime.now();

        if (foodItem.getSaleEndTime().isBefore(now)) {
            foodItem.setIsExpired(true);
            foodItem.setStatus(FoodItemStatus.EXPIRED);
            return;
        }
        if (foodItem.getAvailableQuantity() <= 0) {
            foodItem.setStatus(FoodItemStatus.SOLD_OUT);
            return;
        }
        if (foodItem.getSaleStartTime().isAfter(now)) {
            foodItem.setStatus(FoodItemStatus.PENDING);
            return;
        }
        foodItem.setStatus(FoodItemStatus.AVAILABLE);
    }
}
