package com.flashfood.flash_food.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * FoodItem entity - Represents flash sale food items
 */
@Entity
@Table(name = "food_items")
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
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Builder.Default
    private FoodItemStatus status = FoodItemStatus.PENDING;

    // For scheduled auto-expiry
    @Builder.Default
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
