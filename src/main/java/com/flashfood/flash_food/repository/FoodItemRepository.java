package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.FoodItem;
import com.flashfood.flash_food.entity.FoodItemStatus;
import com.flashfood.flash_food.entity.Store;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FoodItem entity
 * Includes pessimistic locking for high concurrency scenarios
 */
@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByStore(Store store);

    List<FoodItem> findByStoreAndStatus(Store store, FoodItemStatus status);

    List<FoodItem> findByStatus(FoodItemStatus status);

    Page<FoodItem> findByStoreId(Long storeId, Pageable pageable);

    Page<FoodItem> findByCategoryId(Long categoryId, Pageable pageable);

    Page<FoodItem> findByStatus(FoodItemStatus status, Pageable pageable);

    /**
     * Find food item with pessimistic write lock for handling concurrency
     * Use this when updating quantity to prevent overselling
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FoodItem f WHERE f.id = :id")
    Optional<FoodItem> findByIdWithLock(@Param("id") Long id);

    /**
     * Find available flash sale food items (paginated)
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.status = FoodItemStatus.AVAILABLE
        AND f.availableQuantity > 0
        AND f.saleStartTime <= :now
        AND f.saleEndTime > :now
        AND f.isExpired = false
    """)
    Page<FoodItem> findAvailableFlashSaleItems(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Find available flash sale food items (list, for scheduled tasks)
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.status = FoodItemStatus.AVAILABLE
        AND f.availableQuantity > 0
        AND f.saleStartTime <= :now
        AND f.saleEndTime > :now
        AND f.isExpired = false
    """)
    List<FoodItem> findAvailableFlashSaleItems(@Param("now") LocalDateTime now);

    /**
     * Find food items by store that are currently on sale
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.store.id = :storeId
        AND f.status = FoodItemStatus.AVAILABLE
        AND f.availableQuantity > 0
        AND f.isExpired = false
    """)
    List<FoodItem> findAvailableItemsByStore(@Param("storeId") Long storeId);

    /**
     * Search food items by name or description (paginated)
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<FoodItem> searchByNameOrDescription(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find expired items that need status update (for scheduler)
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.isExpired = false
        AND f.saleEndTime < :now
    """)
    List<FoodItem> findExpiredItems(@Param("now") LocalDateTime now);

    /**
     * Update quantity atomically (for high concurrency)
     */
    @Modifying
    @Query("""
        UPDATE FoodItem f
        SET f.availableQuantity = f.availableQuantity - :quantity
        WHERE f.id = :id
        AND f.availableQuantity >= :quantity
    """)
    int decrementQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
}
