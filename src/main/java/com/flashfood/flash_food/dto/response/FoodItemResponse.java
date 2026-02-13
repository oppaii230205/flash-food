package com.flashfood.flash_food.dto.response;

import com.flashfood.flash_food.entity.FoodCategory;
import com.flashfood.flash_food.entity.FoodItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private BigDecimal originalPrice;
    private BigDecimal flashPrice;
    private Integer discountPercentage;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private FoodCategory category;
    private FoodItemStatus status;
    
    // Calculated fields
    private Boolean isAvailable;
    private Long timeUntilSaleStart; // in seconds
    private Long timeUntilSaleEnd; // in seconds
}
