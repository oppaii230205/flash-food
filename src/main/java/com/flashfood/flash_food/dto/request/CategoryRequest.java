package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating category
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    private String slug;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private String iconUrl;
    
    @Min(value = 0, message = "Display order must be non-negative")
    private Integer displayOrder = 0;
    
    private Boolean isActive = true;
    
    private Long parentId;
}
