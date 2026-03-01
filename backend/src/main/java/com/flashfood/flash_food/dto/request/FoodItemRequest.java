package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating/updating a flash-sale food item.
 *
 * Cross-field validation (flashPrice &lt; originalPrice, saleEndTime after saleStartTime)
 * is enforced inside {@link com.flashfood.flash_food.service.impl.FoodItemServiceImpl}
 * to keep annotation validation focused on single-field constraints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemRequest {

    @NotBlank(message = "Food name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 512, message = "Image URL must not exceed 512 characters")
    private String imageUrl;

    @NotNull(message = "Original price is required")
    @Min(value = 1000, message = "Original price must be at least 1,000 VND")
    private Integer originalPrice;

    @NotNull(message = "Flash price is required")
    @Min(value = 1000, message = "Flash price must be at least 1,000 VND")
    private Integer flashPrice;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity must not exceed 10,000")
    private Integer quantity;

    @NotNull(message = "Sale start time is required")
    private LocalDateTime saleStartTime;

    @NotNull(message = "Sale end time is required")
    private LocalDateTime saleEndTime;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
