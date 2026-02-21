package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.FoodItemRequest;
import com.flashfood.flash_food.dto.response.ApiResponse;
import com.flashfood.flash_food.dto.response.FoodItemResponse;
import com.flashfood.flash_food.service.FoodItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for FoodItem management
 * Handles flash sale items with time-based availability and stock management
 */
@Slf4j
@RestController
@RequestMapping("/api/food-items")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemService foodItemService;

    /**
     * Create a new food item for a store
     * @param storeId Store ID
     * @param request Food item data
     * @return Created food item
     */
    @PostMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemResponse>> createFoodItem(
            @PathVariable Long storeId,
            @Valid @RequestBody FoodItemRequest request) {
        
        log.info("POST /api/food-items/store/{} - Creating food item", storeId);
        FoodItemResponse response = foodItemService.createFoodItem(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Food item created successfully", response));
    }

    /**
     * Update an existing food item
     * @param id Food item ID
     * @param request Updated food item data
     * @return Updated food item
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemResponse>> updateFoodItem(
            @PathVariable Long id,
            @Valid @RequestBody FoodItemRequest request) {
        
        log.info("PUT /api/food-items/{} - Updating food item", id);
        FoodItemResponse response = foodItemService.updateFoodItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Food item updated successfully", response));
    }

    /**
     * Delete a food item
     * @param id Food item ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFoodItem(@PathVariable Long id) {
        log.info("DELETE /api/food-items/{} - Deleting food item", id);
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Food item deleted successfully")
                .httpCode(HttpStatus.OK.value())
                .build());
    }

    /**
     * Get food item by ID
     * @param id Food item ID
     * @return Food item details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodItemResponse>> getFoodItemById(@PathVariable Long id) {
        log.info("GET /api/food-items/{} - Getting food item", id);
        FoodItemResponse response = foodItemService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all food items with pagination
     * @param pageable Pagination parameters
     * @return Page of food items
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> getAllFoodItems(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/food-items - Getting all food items");
        Page<FoodItemResponse> response = foodItemService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get food items by store
     * @param storeId Store ID
     * @param pageable Pagination parameters
     * @return Page of food items
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> getFoodItemsByStore(
            @PathVariable Long storeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/food-items/store/{} - Getting food items by store", storeId);
        Page<FoodItemResponse> response = foodItemService.findByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get food items by category
     * @param categoryId Category ID
     * @param pageable Pagination parameters
     * @return Page of food items
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> getFoodItemsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/food-items/category/{} - Getting food items by category", categoryId);
        Page<FoodItemResponse> response = foodItemService.findByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get food items by status
     * @param status Status as string
     * @param pageable Pagination parameters
     * @return Page of food items
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> getFoodItemsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/food-items/status/{} - Getting food items by status", status);
        Page<FoodItemResponse> response = foodItemService.findByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get available food items (in stock, not expired, within sale period)
     * @param pageable Pagination parameters
     * @return Page of available food items
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> getAvailableFoodItems(
            @PageableDefault(size = 20, sort = "flashPrice", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.info("GET /api/food-items/available - Getting available food items");
        Page<FoodItemResponse> response = foodItemService.findAvailableFoodItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get flash sale food items (currently on sale)
     * @param pageable Pagination parameters
     * @return Page of flash sale food items
     */
    @GetMapping("/flash-sale")
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> getFlashSaleFoodItems(
            @PageableDefault(size = 20, sort = "discountPercentage", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/food-items/flash-sale - Getting flash sale food items");
        Page<FoodItemResponse> response = foodItemService.findFlashSaleFoodItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search food items by keyword
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Page of matching food items
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> searchFoodItems(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/food-items/search?keyword={} - Searching food items", keyword);
        Page<FoodItemResponse> response = foodItemService.searchFoodItems(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update food item status
     * @param id Food item ID
     * @param status New status
     * @return Updated food item
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemResponse>> updateFoodItemStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        log.info("PATCH /api/food-items/{}/status?status={} - Updating status", id, status);
        FoodItemResponse response = foodItemService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Food item status updated successfully", response));
    }
}
