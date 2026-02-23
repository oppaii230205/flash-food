package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.request.CategoryRequest;
import com.flashfood.flash_food.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Category operations
 */
public interface CategoryService {
    
    /**
     * Create a new category
     * @param request Category data
     * @return Created category
     */
    CategoryResponse createCategory(CategoryRequest request);
    
    /**
     * Update an existing category
     * @param id Category ID
     * @param request Updated category data
     * @return Updated category
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    
    /**
     * Delete a category
     * @param id Category ID
     */
    void deleteCategory(Long id);
    
    /**
     * Find category by ID
     * @param id Category ID
     * @return Category details
     */
    CategoryResponse findById(Long id);
    
    /**
     * Find category by slug
     * @param slug Category slug
     * @return Category details
     */
    CategoryResponse findBySlug(String slug);
    
    /**
     * Find all categories with pagination
     * @param pageable Pagination parameters
     * @return Page of categories
     */
    Page<CategoryResponse> findAll(Pageable pageable);
    
    /**
     * Find all active categories
     * @return List of active categories
     */
    List<CategoryResponse> findActiveCategories();
    
    /**
     * Find root categories (no parent)
     * @return List of root categories
     */
    List<CategoryResponse> findRootCategories();
    
    /**
     * Find child categories of a parent
     * @param parentId Parent category ID
     * @return List of child categories
     */
    List<CategoryResponse> findChildCategories(Long parentId);

    /**
     * Search categories by keyword (name)
     * @param keyword Search keyword
     * @return List of matching active categories
     */
    List<CategoryResponse> searchCategories(String keyword);
}
