package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for food item response
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
    private Integer originalPrice;
    private Integer flashPrice;
    private Integer discountPercentage;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    
    // Category information
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    
    // Status as string for client
    private String status;
    
    // Calculated fields
    private Boolean isAvailable;
    private Long timeUntilSaleStart; // in seconds
    private Long timeUntilSaleEnd; // in seconds
}
