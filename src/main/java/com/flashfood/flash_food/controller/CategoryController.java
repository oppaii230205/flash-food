package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.CategoryRequest;
import com.flashfood.flash_food.dto.response.ApiResponse;
import com.flashfood.flash_food.dto.response.CategoryResponse;
import com.flashfood.flash_food.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category management
 */
@RestController
@RequestMapping("/api/categories")
@Slf4j
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Create a new category (Admin only)
     * POST /api/categories
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("REST request to create category: {}", request.getName());
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(category));
    }
    
    /**
     * Update an existing category (Admin only)
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("REST request to update category with id: {}", id);
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
    
    /**
     * Delete a category (Admin only)
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        log.info("REST request to delete category with id: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Category deleted successfully")
                .httpCode(HttpStatus.OK.value())
                .build());
    }
    
    /**
     * Get category by ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        log.info("REST request to get category with id: {}", id);
        CategoryResponse category = categoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
    
    /**
     * Get category by slug
     * GET /api/categories/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        log.info("REST request to get category with slug: {}", slug);
        CategoryResponse category = categoryService.findBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
    
    /**
     * Get all categories with pagination
     * GET /api/categories?page=0&size=10&sort=displayOrder,asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        log.info("REST request to get all categories - page: {}, size: {}", page, size);
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CategoryResponse> categories = categoryService.findAll(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    /**
     * Get all active categories (no pagination)
     * GET /api/categories/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {
        log.info("REST request to get all active categories");
        List<CategoryResponse> categories = categoryService.findActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    /**
     * Get root categories (categories without parent)
     * GET /api/categories/root
     */
    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        log.info("REST request to get root categories");
        List<CategoryResponse> categories = categoryService.findRootCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    /**
     * Get child categories of a parent
     * GET /api/categories/{parentId}/children
     */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getChildCategories(@PathVariable Long parentId) {
        log.info("REST request to get child categories for parent id: {}", parentId);
        List<CategoryResponse> categories = categoryService.findChildCategories(parentId);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
