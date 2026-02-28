package com.flashfood.flash_food.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * FoodItem entity - Represents flash sale food items offered by a store.
 *
 * Enum field {@code status} is stored as INTEGER via the auto-applied
 * {@link com.flashfood.flash_food.entity.converter.FoodItemStatusConverter}.
 * Clients always send/receive the status as a human-readable string.
 *
 * Optimistic locking ({@code @Version}) guards concurrent updates; for
 * quantity decrements during order placement, use
 * {@link com.flashfood.flash_food.repository.FoodItemRepository#findByIdWithLock}
 * (pessimistic write lock) instead.
 *
 * Soft-delete: setting {@code status = DELETED} hides the row from all queries
 * via the {@code @SQLRestriction} below.
 */
@SQLRestriction("status <> 6")
@Entity
@Table(name = "food_items", indexes = {
    @Index(name = "idx_food_item_store_id",       columnList = "store_id"),
    @Index(name = "idx_food_item_category_id",    columnList = "category_id"),
    @Index(name = "idx_food_item_status",         columnList = "status"),
    @Index(name = "idx_food_item_sale_window",    columnList = "sale_start_time,sale_end_time"),
    @Index(name = "idx_food_item_is_expired",     columnList = "is_expired")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private String imageUrl;
    
    @Column(nullable = false)
    private Integer originalPrice;
    
    @Column(nullable = false)
    private Integer flashPrice;
    
    // Discount percentage (e.g., 70%)
    private Integer discountPercentage;
    
    // Stock management for high concurrency
    @Column(nullable = false)
    private Integer totalQuantity;
    
    @Column(nullable = false)
    private Integer availableQuantity;
    
    // Flash sale period
    @Column(name = "sale_start_time")
    private LocalDateTime saleStartTime;

    @Column(name = "sale_end_time")
    private LocalDateTime saleEndTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Builder.Default
    private FoodItemStatus status = FoodItemStatus.PENDING;

    // For scheduled auto-expiry
    @Builder.Default
    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired = false;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Version for optimistic locking to handle concurrency
    @Version
    private Long version;
}
