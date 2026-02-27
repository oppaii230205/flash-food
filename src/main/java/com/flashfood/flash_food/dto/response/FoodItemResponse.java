package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a flash-sale food item.
 *
 * Computed fields (isAvailable, timeUntilSaleStart, timeUntilSaleEnd) are
 * populated by {@link com.flashfood.flash_food.util.EntityMapper#toFoodItemResponse}
 * so controllers and services never need to duplicate that logic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private String name;
    private String description;
    private String imageUrl;

    // Pricing
    private Integer originalPrice;
    private Integer flashPrice;
    private Integer discountPercentage;

    // Stock
    private Integer totalQuantity;
    private Integer availableQuantity;

    // Sale window
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;

    // Category
    private Long categoryId;
    private String categoryName;
    private String categorySlug;

    // Status as a human-readable string for clients  (e.g. "available", "pending")
    private String status;
    private Boolean isExpired;

    // Computed convenience fields
    /** True when the item is available, in stock, and within its sale window right now. */
    private Boolean isAvailable;
    /** Seconds until the sale starts; null if already started or expired. */
    private Long timeUntilSaleStart;
    /** Seconds until the sale ends; null if not yet started or already expired. */
    private Long timeUntilSaleEnd;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
