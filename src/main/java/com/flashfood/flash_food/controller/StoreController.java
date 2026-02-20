package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.CreateStoreRequest;
import com.flashfood.flash_food.dto.response.ApiResponse;
import com.flashfood.flash_food.dto.response.StoreResponse;
import com.flashfood.flash_food.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Store management
 */
@RestController
@RequestMapping("/api/stores")
@Slf4j
@RequiredArgsConstructor
public class StoreController {
    
    private final StoreService storeService;
    
    /**
     * Create a new store (Store owner or admin)
     * POST /api/stores
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody CreateStoreRequest request) {
        log.info("REST request to create store: {}", request.getName());
        StoreResponse store = storeService.createStore(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(store));
    }
    
    /**
     * Update an existing store (Owner or admin)
     * PUT /api/stores/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody CreateStoreRequest request) {
        log.info("REST request to update store with id: {}", id);
        StoreResponse store = storeService.updateStore(id, request);
        return ResponseEntity.ok(ApiResponse.success(store));
    }
    
    /**
     * Delete a store (Owner or admin)
     * DELETE /api/stores/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long id) {
        log.info("REST request to delete store with id: {}", id);
        storeService.deleteStore(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Store deleted successfully")
                .httpCode(HttpStatus.OK.value())
                .build());
    }
    
    /**
     * Get store by ID
     * GET /api/stores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long id) {
        log.info("REST request to get store with id: {}", id);
        StoreResponse store = storeService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(store));
    }
    
    /**
     * Get all stores with pagination
     * GET /api/stores?page=0&size=10&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        log.info("REST request to get all stores - page: {}, size: {}", page, size);
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<StoreResponse> stores = storeService.findAll(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(stores));
    }
    
    /**
     * Get stores by type
     * GET /api/stores/type/{type}?page=0&size=10
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> getStoresByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("REST request to get stores by type: {}", type);
        Pageable pageable = PageRequest.of(page, size);
        Page<StoreResponse> stores = storeService.findByType(type, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(stores));
    }
    
    /**
     * Get stores by status (Admin only)
     * GET /api/stores/status/{status}?page=0&size=10
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> getStoresByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("REST request to get stores by status: {}", status);
        Pageable pageable = PageRequest.of(page, size);
        Page<StoreResponse> stores = storeService.findByStatus(status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(stores));
    }
    
    /**
     * Find nearby stores within radius
     * GET /api/stores/nearby?lat=10.762622&lon=106.660172&radius=5
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> findNearbyStores(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "5.0") Double radius) {
        
        log.info("REST request to find nearby stores - lat: {}, lon: {}, radius: {}km", lat, lon, radius);
        List<StoreResponse> stores = storeService.findNearbyStores(lat, lon, radius);
        
        return ResponseEntity.ok(ApiResponse.success(stores));
    }
    
    /**
     * Get my stores (current user's stores)
     * GET /api/stores/my-stores
     */
    @GetMapping("/my-stores")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getMyStores() {
        log.info("REST request to get my stores");
        List<StoreResponse> stores = storeService.findMyStores();
        return ResponseEntity.ok(ApiResponse.success(stores));
    }
    
    /**
     * Update store status (Admin or owner)
     * PATCH /api/stores/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStoreStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        
        log.info("REST request to update store status for id: {}", id);
        String status = statusUpdate.get("status");
        
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, "Status is required"));
        }
        
        StoreResponse store = storeService.updateStoreStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(store));
    }
}
