package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating/updating food item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemRequest {
    
    @NotBlank(message = "Food name is required")
    private String name;
    
    private String description;
    private String imageUrl;
    
    @NotNull(message = "Original price is required")
    @Min(value = 1, message = "Original price must be greater than 0")
    private Integer originalPrice;
    
    @NotNull(message = "Flash price is required")
    @Min(value = 1, message = "Flash price must be greater than 0")
    private Integer flashPrice;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @NotNull(message = "Sale start time is required")
    private LocalDateTime saleStartTime;
    
    @NotNull(message = "Sale end time is required")
    private LocalDateTime saleEndTime;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
}
