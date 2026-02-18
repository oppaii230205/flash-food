package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for category response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Long parentId;
    private String parentName;
    
    // For hierarchical display
    private Integer level; // 0 for root, 1 for child, etc.
}
