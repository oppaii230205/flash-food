package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.CreateStoreRequest;
import com.flashfood.flash_food.dto.request.UpdateStoreRequest;
import com.flashfood.flash_food.dto.response.StoreResponse;
import com.flashfood.flash_food.entity.*;
import com.flashfood.flash_food.exception.AccessDeniedException;
import com.flashfood.flash_food.exception.InvalidOperationException;
import com.flashfood.flash_food.exception.ResourceNotFoundException;
import com.flashfood.flash_food.repository.StoreRepository;
import com.flashfood.flash_food.service.AuthenticationService;
import com.flashfood.flash_food.service.RedisGeoService;
import com.flashfood.flash_food.service.StoreService;
import com.flashfood.flash_food.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link StoreService}.
 *
 * Ownership / authorisation rules enforced here (not only at the controller
 * level) so that any programmatic callers also respect them.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    /**
     * Status values that a store owner is allowed to set on their own store.
     * Admins are unrestricted.
     */
    private static final Set<StoreStatus> OWNER_ALLOWED_STATUSES =
            Set.of(StoreStatus.ACTIVE, StoreStatus.INACTIVE);

    private final StoreRepository      storeRepository;
    private final AuthenticationService authenticationService;
    private final RedisGeoService      redisGeoService;
    private final EntityMapper         entityMapper;

    // =========================================================================
    // Mutating operations
    // =========================================================================

    @Override
    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        log.info("Creating new store: {}", request.getName());

        User currentUser = authenticationService.getCurrentUser();

        StoreType storeType = resolveStoreType(request.getType());

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
                .status(StoreStatus.PENDING_APPROVAL) // Requires admin approval before going live
                .build();

        Store savedStore = storeRepository.save(store);

        // Index in Redis Geo so the store is ready once approved
        redisGeoService.addStoreLocation(
                savedStore.getId(), savedStore.getLongitude(), savedStore.getLatitude());

        log.info("Store created with id={}, status=PENDING_APPROVAL", savedStore.getId());
        return entityMapper.toStoreResponse(savedStore);
    }

    @Override
    @Transactional
    public StoreResponse updateStore(Long id, UpdateStoreRequest request) {
        log.info("Updating store id={}", id);

        Store store = findStoreOrThrow(id);
        verifyOwnerOrAdmin(store, "update");

        // Apply only non-null fields (partial-update semantics)
        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getAddress() != null) {
            store.setAddress(request.getAddress());
        }
        if (request.getPhoneNumber() != null) {
            store.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDescription() != null) {
            store.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            store.setImageUrl(request.getImageUrl());
        }
        if (request.getOpenTime() != null) {
            store.setOpenTime(request.getOpenTime());
        }
        if (request.getCloseTime() != null) {
            store.setCloseTime(request.getCloseTime());
        }
        if (request.getFlashSaleTime() != null) {
            store.setFlashSaleTime(request.getFlashSaleTime());
        }
        if (request.getType() != null) {
            store.setType(resolveStoreType(request.getType()));
        }

        // Update coordinates if either is provided
        Double newLat = request.getLatitude();
        Double newLon = request.getLongitude();
        if (newLat != null || newLon != null) {
            double lat = newLat != null ? newLat : store.getLatitude();
            double lon = newLon != null ? newLon : store.getLongitude();
            store.setLatitude(lat);
            store.setLongitude(lon);
            redisGeoService.addStoreLocation(store.getId(), lon, lat);
            log.debug("Updated Redis Geo location for store id={}", id);
        }

        Store updated = storeRepository.save(store);
        log.info("Store id={} updated successfully", id);
        return entityMapper.toStoreResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStore(Long id) {
        log.info("Deleting store id={}", id);

        Store store = findStoreOrThrow(id);
        verifyOwnerOrAdmin(store, "delete");

        redisGeoService.removeStoreLocation(store.getId());
        storeRepository.delete(store);

        log.info("Store id={} deleted successfully", id);
    }

    @Override
    @Transactional
    public StoreResponse approveStore(Long id) {
        log.info("Approving store id={}", id);

        if (!authenticationService.isAdmin()) {
            throw new AccessDeniedException("Only admins can approve stores");
        }

        Store store = findStoreOrThrow(id);

        if (store.getStatus() != StoreStatus.PENDING_APPROVAL) {
            throw new InvalidOperationException(
                    "Store id=" + id + " is not in PENDING_APPROVAL status (current: "
                    + store.getStatus().getDisplayName() + ")");
        }

        store.setStatus(StoreStatus.ACTIVE);
        Store approved = storeRepository.save(store);

        log.info("Store id={} approved and set to ACTIVE", id);
        return entityMapper.toStoreResponse(approved);
    }

    @Override
    @Transactional
    public StoreResponse updateStoreStatus(Long id, String status) {
        log.info("Updating status for store id={} to '{}'", id, status);

        Store store = findStoreOrThrow(id);
        verifyOwnerOrAdmin(store, "update status of");

        StoreStatus newStatus = resolveStoreStatus(status);

        // Non-admin owners can only toggle between ACTIVE and INACTIVE
        if (!authenticationService.isAdmin() && !OWNER_ALLOWED_STATUSES.contains(newStatus)) {
            throw new AccessDeniedException(
                    "Store owners may only set status to 'active' or 'inactive'");
        }

        store.setStatus(newStatus);
        Store updated = storeRepository.save(store);

        log.info("Store id={} status changed to '{}'", id, newStatus.getDisplayName());
        return entityMapper.toStoreResponse(updated);
    }

    // =========================================================================
    // Query operations
    // =========================================================================

    @Override
    public StoreResponse findById(Long id) {
        log.debug("Finding store id={}", id);
        return entityMapper.toStoreResponse(findStoreOrThrow(id));
    }

    @Override
    public Page<StoreResponse> findActiveStores(Pageable pageable) {
        log.debug("Finding ACTIVE stores, pageable={}", pageable);
        return storeRepository
                .findByStatus(StoreStatus.ACTIVE, pageable)
                .map(entityMapper::toStoreResponse);
    }

    @Override
    public Page<StoreResponse> findAll(Pageable pageable) {
        log.debug("Finding all stores (admin), pageable={}", pageable);
        return storeRepository.findAll(pageable).map(entityMapper::toStoreResponse);
    }

    @Override
    public Page<StoreResponse> findByType(String type, Pageable pageable) {
        log.debug("Finding ACTIVE stores by type='{}', pageable={}", type, pageable);
        StoreType storeType = resolveStoreType(type);
        return storeRepository
                .findByStatusAndType(StoreStatus.ACTIVE, storeType, pageable)
                .map(entityMapper::toStoreResponse);
    }

    @Override
    public Page<StoreResponse> findByStatus(String status, Pageable pageable) {
        log.debug("Finding stores by status='{}', pageable={}", status, pageable);
        StoreStatus storeStatus = resolveStoreStatus(status);
        return storeRepository
                .findByStatus(storeStatus, pageable)
                .map(entityMapper::toStoreResponse);
    }

    @Override
    public Page<StoreResponse> searchStores(String keyword, Pageable pageable) {
        log.debug("Searching ACTIVE stores by keyword='{}', pageable={}", keyword, pageable);
        return storeRepository
                .searchByNameAndStatus(keyword, StoreStatus.ACTIVE, pageable)
                .map(entityMapper::toStoreResponse);
    }

    @Override
    public List<StoreResponse> findNearbyStores(Double latitude, Double longitude, Double radiusInKm) {
        log.debug("Finding nearby stores: lat={}, lon={}, radius={}km", latitude, longitude, radiusInKm);

        List<Long> storeIds = redisGeoService.findNearbyStores(longitude, latitude, radiusInKm);
        if (storeIds.isEmpty()) {
            return List.of();
        }

        List<Store> stores = storeRepository.findByIdInAndStatus(storeIds, StoreStatus.ACTIVE);

        return stores.stream()
                .map(store -> {
                    StoreResponse response = entityMapper.toStoreResponse(store);
                    response.setDistance(
                            calculateDistanceMetres(latitude, longitude,
                                    store.getLatitude(), store.getLongitude()));
                    return response;
                })
                .sorted((a, b) -> Double.compare(a.getDistance(), b.getDistance()))
                .toList();
    }

    @Override
    public List<StoreResponse> findMyStores() {
        log.debug("Finding stores for current user");
        User currentUser = authenticationService.getCurrentUser();
        return storeRepository.findByOwner(currentUser)
                .stream()
                .map(entityMapper::toStoreResponse)
                .toList();
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private Store findStoreOrThrow(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
    }

    /**
     * Throws {@link AccessDeniedException} if the current user is neither the
     * store's owner nor an admin.
     */
    private void verifyOwnerOrAdmin(Store store, String action) {
        if (!authenticationService.isStoreOwnerOrAdmin(store)) {
            throw new AccessDeniedException(
                    "You do not have permission to " + action + " this store");
        }
    }

    private StoreType resolveStoreType(String type) {
        try {
            return StoreType.fromDisplayName(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid store type: '" + type + "'");
        }
    }

    private StoreStatus resolveStoreStatus(String status) {
        try {
            return StoreStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid store status: '" + status + "'");
        }
    }

    /**
     * Haversine formula — returns distance in metres.
     */
    private double calculateDistanceMetres(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000; // Earth radius in metres

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                  * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}

