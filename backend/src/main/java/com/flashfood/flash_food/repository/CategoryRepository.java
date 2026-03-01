package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findBySlug(String slug);
    
    Optional<Category> findByName(String name);
    
    boolean existsBySlug(String slug);
    
    boolean existsByName(String name);
    
    /**
     * Find all active categories ordered by display order
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * Find all root categories (categories without parent)
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findRootCategories();
    
    /**
     * Find all child categories of a parent
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findChildCategories(@Param("parentId") Long parentId);
    
    /**
     * Check if category has child categories
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parent.id = :parentId")
    boolean existsByParentId(@Param("parentId") Long parentId);
    
    /**
     * Search categories by name
     */
    @Query("""
        SELECT c FROM Category c 
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND c.isActive = true
        ORDER BY c.displayOrder ASC
    """)
    List<Category> searchByName(@Param("keyword") String keyword);
}
