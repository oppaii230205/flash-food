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
 * Category entity - Represents food categories for better management and search
 * Example: Bánh ngọt, Cơm, Đồ ăn vặt, Đồ uống, etc.
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_slug", columnList = "slug"),
    @Index(name = "idx_category_active", columnList = "isActive")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    /**
     * URL-friendly slug for the category
     * Example: "banh-ngot", "do-uong"
     */
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    
    @Column(length = 500)
    private String description;
    
    /**
     * Icon or image URL for the category
     */
    private String iconUrl;
    
    /**
     * Display order for sorting categories
     */
    @Column(nullable = false)
    private Integer displayOrder = 0;
    
    /**
     * Active status
     */
    @Column(nullable = false)
    private Boolean isActive = true;
    
    /**
     * Parent category for hierarchical structure (optional)
     * Example: "Đồ ăn" -> "Bánh ngọt", "Cơm"
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
