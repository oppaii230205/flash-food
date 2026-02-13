package com.flashfood.flash_food.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FoodItem entity - Represents flash sale food items
 */
@Entity
@Table(name = "food_items")
@Data
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
    private BigDecimal originalPrice;
    
    @Column(nullable = false)
    private BigDecimal flashPrice;
    
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
    
    @Enumerated(EnumType.STRING)
    private FoodCategory category;
    
    @Enumerated(EnumType.STRING)
    private FoodItemStatus status = FoodItemStatus.PENDING;
    
    // For scheduled auto-expiry
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
