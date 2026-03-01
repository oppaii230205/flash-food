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
 * Repository for FoodItem entity.
 *
 * JPQL note: enum constants are always passed as named parameters so Hibernate
 * can apply the registered {@link com.flashfood.flash_food.entity.converter.FoodItemStatusConverter}
 * automatically — never hard-code enum literals inside a JPQL string (e.g.
 * {@code = FoodItemStatus.AVAILABLE}) because that form bypasses the converter
 * and compares the enum name against an integer DB column.
 *
 * Includes pessimistic locking helpers for high-concurrency stock management.
 */
@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    // -------------------------------------------------------------------------
    // Simple derived finders (Spring Data handles parameter binding correctly)
    // -------------------------------------------------------------------------

    List<FoodItem> findByStore(Store store);

    List<FoodItem> findByStoreAndStatus(Store store, FoodItemStatus status);

    Page<FoodItem> findByStoreId(Long storeId, Pageable pageable);

    Page<FoodItem> findByCategoryId(Long categoryId, Pageable pageable);

    Page<FoodItem> findByStatus(FoodItemStatus status, Pageable pageable);

    // -------------------------------------------------------------------------
    // Pessimistic-lock finder (for stock decrement operations)
    // -------------------------------------------------------------------------

    /**
     * Acquires a write lock on the row; use this inside a @Transactional method
     * when updating {@code availableQuantity} to prevent overselling.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FoodItem f WHERE f.id = :id")
    Optional<FoodItem> findByIdWithLock(@Param("id") Long id);

    // -------------------------------------------------------------------------
    // Flash-sale public queries
    // -------------------------------------------------------------------------

    /**
     * Currently active flash-sale items: status AVAILABLE, in stock, within sale window.
     * Used by the public-facing "flash sale" feed.
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.status = :status
        AND f.availableQuantity > 0
        AND f.saleStartTime <= :now
        AND f.saleEndTime > :now
        AND f.isExpired = false
        ORDER BY f.discountPercentage DESC
    """)
    Page<FoodItem> findActiveFlashSaleItems(
            @Param("status") FoodItemStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /**
     * All items with AVAILABLE status (not necessarily in their sale window right now).
     * Used by the general catalogue / "available" feed.
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.status = :status
        AND f.availableQuantity > 0
        AND f.isExpired = false
    """)
    Page<FoodItem> findAvailableItems(
            @Param("status") FoodItemStatus status,
            Pageable pageable);

    /**
     * Active items belonging to a specific store — used in store detail screens.
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.store.id = :storeId
        AND f.status = :status
        AND f.availableQuantity > 0
        AND f.isExpired = false
    """)
    List<FoodItem> findAvailableItemsByStore(
            @Param("storeId") Long storeId,
            @Param("status") FoodItemStatus status);

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    /**
     * Case-insensitive keyword search across name and description.
     */
    @Query("""
        SELECT f FROM FoodItem f
        WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<FoodItem> searchByNameOrDescription(
            @Param("keyword") String keyword,
            Pageable pageable);

    // -------------------------------------------------------------------------
    // Scheduler helpers (bulk updates avoid N+1 individual saves)
    // -------------------------------------------------------------------------

    /**
     * Bulk-expires items that have passed their sale end time.
     * Only transitions AVAILABLE and PENDING items; CANCELLED/SOLD_OUT remain unchanged.
     */
    @Modifying
    @Query("""
        UPDATE FoodItem f
        SET f.status = :expired, f.isExpired = true
        WHERE f.isExpired = false
        AND f.saleEndTime < :now
        AND f.status IN (:available, :pending)
    """)
    int bulkExpireItems(
            @Param("expired") FoodItemStatus expired,
            @Param("now") LocalDateTime now,
            @Param("available") FoodItemStatus available,
            @Param("pending") FoodItemStatus pending);

    /**
     * Bulk-activates PENDING items whose sale window has opened and still have stock.
     */
    @Modifying
    @Query("""
        UPDATE FoodItem f
        SET f.status = :available
        WHERE f.status = :pending
        AND f.saleStartTime <= :now
        AND f.saleEndTime > :now
        AND f.availableQuantity > 0
    """)
    int bulkActivatePendingItems(
            @Param("available") FoodItemStatus available,
            @Param("pending") FoodItemStatus pending,
            @Param("now") LocalDateTime now);

    // -------------------------------------------------------------------------
    // Stock management
    // -------------------------------------------------------------------------

    /**
     * Atomic quantity decrement — only succeeds when enough stock is available.
     * Returns the number of rows updated (1 on success, 0 if insufficient stock).
     */
    @Modifying
    @Query("""
        UPDATE FoodItem f
        SET f.availableQuantity = f.availableQuantity - :quantity
        WHERE f.id = :id
        AND f.availableQuantity >= :quantity
    """)
    int decrementQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * Atomic quantity increment — used when an order is cancelled and stock is restored.
     */
    @Modifying
    @Query("""
        UPDATE FoodItem f
        SET f.availableQuantity = f.availableQuantity + :quantity
        WHERE f.id = :id
    """)
    int incrementQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * Transitions an item to SOLD_OUT when its available quantity has reached zero.
     * Called after {@link #decrementQuantity} when the remaining quantity is known to be 0,
     * avoiding a flush + re-fetch cycle on the entity.
     * The conditional {@code WHERE f.availableQuantity = 0} makes this operation idempotent.
     */
    @Modifying
    @Query("""
        UPDATE FoodItem f
        SET f.status = :soldOut
        WHERE f.id = :id
        AND f.availableQuantity = 0
    """)
    int markSoldOutIfEmpty(@Param("id") Long id, @Param("soldOut") FoodItemStatus soldOut);

    /**
     * Restores an item from SOLD_OUT back to AVAILABLE after stock is returned
     * (e.g. order cancellation). Only applies when the item currently has stock.
     */
    @Modifying
    @Query("""
        UPDATE FoodItem f
        SET f.status = :available
        WHERE f.id = :id
        AND f.status = :soldOut
        AND f.availableQuantity > 0
    """)
    int restoreAvailableIfSoldOut(@Param("id") Long id,
                                  @Param("available") FoodItemStatus available,
                                  @Param("soldOut") FoodItemStatus soldOut);
}
