package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.CreateStoreRequest;
import com.flashfood.flash_food.dto.request.UpdateStoreRequest;
import com.flashfood.flash_food.dto.request.UpdateStoreStatusRequest;
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

/**
 * REST controller for Store management.
 *
 * Endpoint summary
 * ────────────────────────────────────────────────────
 * POST   /api/v1/stores                        → createStore       (STORE_OWNER, ADMIN)
 * PUT    /api/v1/stores/{id}                   → updateStore       (owner or ADMIN)
 * DELETE /api/v1/stores/{id}                   → deleteStore       (owner or ADMIN)
 * POST   /api/v1/stores/{id}/approve           → approveStore      (ADMIN)
 * PATCH  /api/v1/stores/{id}/status            → updateStoreStatus (owner or ADMIN)
 *
 * GET    /api/v1/stores                        → getActiveStores   (public)
 * GET    /api/v1/stores/admin/all              → getAllStores       (ADMIN)
 * GET    /api/v1/stores/{id}                   → getStoreById      (public)
 * GET    /api/v1/stores/search?keyword=        → searchStores      (public)
 * GET    /api/v1/stores/type/{type}            → getByType         (public)
 * GET    /api/v1/stores/status/{status}        → getByStatus       (ADMIN)
 * GET    /api/v1/stores/nearby?lat=&lon=&radius= → getNearbyStores (authenticated)
 * GET    /api/v1/stores/my-stores              → getMyStores       (STORE_OWNER, ADMIN)
 */
@RestController
@RequestMapping("/api/v1/stores")
@Slf4j
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // =========================================================================
    // Mutating endpoints
    // =========================================================================

    /**
     * POST /api/v1/stores
     * Create a new store.  The store is set to PENDING_APPROVAL until an admin approves it.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(
            @Valid @RequestBody CreateStoreRequest request) {

        log.info("REST request to create store: {}", request.getName());
        StoreResponse store = storeService.createStore(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Store created and pending approval", store));
    }

    /**
     * PUT /api/v1/stores/{id}
     * Partially update a store (only non-null fields are applied).
     * Only the store owner or an admin may call this.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request) {

        log.info("REST request to update store id={}", id);
        StoreResponse store = storeService.updateStore(id, request);
        return ResponseEntity.ok(ApiResponse.success(store));
    }

    /**
     * DELETE /api/v1/stores/{id}
     * Hard-delete a store.  Only the store owner or an admin may call this.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long id) {
        log.info("REST request to delete store id={}", id);
        storeService.deleteStore(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Store deleted successfully")
                        .httpCode(HttpStatus.OK.value())
                        .build());
    }

    /**
     * POST /api/v1/stores/{id}/approve
     * Approve a pending store (ADMIN only).
     * Transitions PENDING_APPROVAL → ACTIVE.
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> approveStore(@PathVariable Long id) {
        log.info("REST request to approve store id={}", id);
        StoreResponse store = storeService.approveStore(id);
        return ResponseEntity.ok(ApiResponse.success("Store approved successfully", store));
    }

    /**
     * PATCH /api/v1/stores/{id}/status
     * Update the store's lifecycle status.
     * Owners may only set "active" or "inactive"; admins may set any valid status.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStoreStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreStatusRequest request) {

        log.info("REST request to update status of store id={} to '{}'", id, request.getStatus());
        StoreResponse store = storeService.updateStoreStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(store));
    }

    // =========================================================================
    // Query endpoints
    // =========================================================================

    /**
     * GET /api/v1/stores?page=0&size=20&sortBy=name&sortDirection=asc
     * Public listing of ACTIVE stores.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> getActiveStores(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "20")   int    size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDirection) {

        log.info("REST request to get ACTIVE stores - page={}, size={}", page, size);
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(storeService.findActiveStores(pageable)));
    }

    /**
     * GET /api/v1/stores/admin/all?page=0&size=20
     * Full store list for admin — includes all statuses.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> getAllStores(
            @RequestParam(defaultValue = "0")          int    page,
            @RequestParam(defaultValue = "20")         int    size,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDirection) {

        log.info("REST request (admin) to get all stores");
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(storeService.findAll(pageable)));
    }

    /**
     * GET /api/v1/stores/{id}
     * Retrieve a single store by ID (available to anyone).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long id) {
        log.info("REST request to get store id={}", id);
        return ResponseEntity.ok(ApiResponse.success(storeService.findById(id)));
    }

    /**
     * GET /api/v1/stores/search?keyword=pizza&page=0&size=10
     * Case-insensitive name search across ACTIVE stores.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> searchStores(
            @RequestParam                        String keyword,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "20")   int    size) {

        log.info("REST request to search stores keyword='{}'", keyword);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(storeService.searchStores(keyword, pageable)));
    }

    /**
     * GET /api/v1/stores/type/{type}?page=0&size=10
     * Paginated list of ACTIVE stores filtered by type.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> getStoresByType(
            @PathVariable                        String type,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "20")   int    size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDirection) {

        log.info("REST request to get stores by type='{}'", type);
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(storeService.findByType(type, pageable)));
    }

    /**
     * GET /api/v1/stores/status/{status}?page=0&size=10  (ADMIN only)
     * Paginated list of stores filtered by status.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> getStoresByStatus(
            @PathVariable                        String status,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "20")   int    size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDirection) {

        log.info("REST request to get stores by status='{}'", status);
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(storeService.findByStatus(status, pageable)));
    }

    /**
     * GET /api/v1/stores/nearby?lat=10.762622&lon=106.660172&radius=5
     * Returns ACTIVE stores within {@code radius} kilometres, sorted by distance.
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> findNearbyStores(
            @RequestParam                         Double lat,
            @RequestParam                         Double lon,
            @RequestParam(defaultValue = "5.0")   Double radius) {

        log.info("REST request to find nearby stores - lat={}, lon={}, radius={}km", lat, lon, radius);
        List<StoreResponse> stores = storeService.findNearbyStores(lat, lon, radius);
        return ResponseEntity.ok(ApiResponse.success(stores));
    }

    /**
     * GET /api/v1/stores/my-stores
     * Returns all stores owned by the currently authenticated user.
     */
    @GetMapping("/my-stores")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getMyStores() {
        log.info("REST request to get my stores");
        return ResponseEntity.ok(ApiResponse.success(storeService.findMyStores()));
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private Pageable buildPageable(int page, int size, String sortBy, String sortDirection) {
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}

