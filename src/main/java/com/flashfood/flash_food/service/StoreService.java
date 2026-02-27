package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.request.CreateStoreRequest;
import com.flashfood.flash_food.dto.request.UpdateStoreRequest;
import com.flashfood.flash_food.dto.response.StoreResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Store operations.
 *
 * Access-control summary
 * ─────────────────────
 * createStore            → STORE_OWNER, ADMIN
 * updateStore            → store owner or ADMIN
 * deleteStore            → store owner or ADMIN
 * approveStore           → ADMIN only
 * updateStoreStatus      → store owner (ACTIVE ↔ INACTIVE only) or ADMIN (any)
 * findActiveStores       → public
 * findAll                → ADMIN
 * findByType / search    → public
 * findByStatus           → ADMIN
 * findMyStores           → STORE_OWNER, ADMIN
 * findNearbyStores       → authenticated
 */
public interface StoreService {

    // -------------------------------------------------------------------------
    // Mutating operations
    // -------------------------------------------------------------------------

    /**
     * Create a new store owned by the currently authenticated user.
     * The store starts in {@code PENDING_APPROVAL} status.
     */
    StoreResponse createStore(CreateStoreRequest request);

    /**
     * Update an existing store.  Only the store owner or an admin may do this.
     * Only non-null fields in the request are applied (partial-update semantics).
     */
    StoreResponse updateStore(Long id, UpdateStoreRequest request);

    /**
     * Hard-delete a store and remove it from the Redis Geo index.
     * Only the store owner or an admin may do this.
     */
    void deleteStore(Long id);

    /**
     * Approve a pending store (ADMIN only).
     * Transitions {@code PENDING_APPROVAL} → {@code ACTIVE}.
     */
    StoreResponse approveStore(Long id);

    /**
     * Update the lifecycle status of a store.
     * Store owners may only toggle between {@code ACTIVE} and {@code INACTIVE}.
     * Admins may set any status.
     */
    StoreResponse updateStoreStatus(Long id, String status);

    // -------------------------------------------------------------------------
    // Query operations
    // -------------------------------------------------------------------------

    /** Get a single store by ID (any status). */
    StoreResponse findById(Long id);

    /** Paginated list of ACTIVE stores — public endpoint. */
    Page<StoreResponse> findActiveStores(Pageable pageable);

    /** Paginated list of all stores regardless of status — admin use. */
    Page<StoreResponse> findAll(Pageable pageable);

    /** Paginated list of ACTIVE stores filtered by type. */
    Page<StoreResponse> findByType(String type, Pageable pageable);

    /** Paginated list of stores filtered by status — admin use. */
    Page<StoreResponse> findByStatus(String status, Pageable pageable);

    /**
     * Case-insensitive name search across ACTIVE stores.
     *
     * @param keyword search term
     */
    Page<StoreResponse> searchStores(String keyword, Pageable pageable);

    /**
     * Find ACTIVE stores within {@code radiusInKm} kilometres from the given
     * coordinates, sorted by distance ascending.
     */
    List<StoreResponse> findNearbyStores(Double latitude, Double longitude, Double radiusInKm);

    /** Return all stores owned by the currently authenticated user. */
    List<StoreResponse> findMyStores();
}

