package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.CategoryRequest;
import com.flashfood.flash_food.dto.response.CategoryResponse;
import com.flashfood.flash_food.entity.Category;
import com.flashfood.flash_food.exception.DuplicateResourceException;
import com.flashfood.flash_food.exception.ResourceNotFoundException;
import com.flashfood.flash_food.repository.CategoryRepository;
import com.flashfood.flash_food.service.CategoryService;
import com.flashfood.flash_food.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final EntityMapper entityMapper;
    
    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        
        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(request.getName());
        } else {
            slug = generateSlug(slug);
        }
        
        // Check if slug already exists
        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Category with slug '" + slug + "' already exists");
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        }
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());
        
        return entityMapper.toCategoryResponse(savedCategory);
    }
    
    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Update slug if name changed
        String newSlug = request.getSlug();
        if (newSlug == null || newSlug.isEmpty()) {
            newSlug = generateSlug(request.getName());
        } else {
            newSlug = generateSlug(newSlug);
        }
        
        // Check slug uniqueness if changed
        if (!category.getSlug().equals(newSlug) && categoryRepository.existsBySlug(newSlug)) {
            throw new DuplicateResourceException("Category with slug '" + newSlug + "' already exists");
        }
        
        // Update fields
        category.setName(request.getName());
        category.setSlug(newSlug);
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : category.getDisplayOrder());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : category.getIsActive());
        
        // Update parent if changed
        if (request.getParentId() != null) {
            if (!request.getParentId().equals(id)) { // Prevent self-reference
                Category parent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
                category.setParent(parent);
            }
        } else {
            category.setParent(null);
        }
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", updatedCategory.getId());
        
        return entityMapper.toCategoryResponse(updatedCategory);
    }
    
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if category has children
        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalStateException("Cannot delete category with existing child categories");
        }
        
        // Check if category is used by food items (if FoodItemRepository is available)
        // This would require injecting FoodItemRepository
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {}", id);
    }
    
    @Override
    public CategoryResponse findById(Long id) {
        log.debug("Finding category by id: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        return entityMapper.toCategoryResponse(category);
    }
    
    @Override
    public CategoryResponse findBySlug(String slug) {
        log.debug("Finding category by slug: {}", slug);
        
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        
        return entityMapper.toCategoryResponse(category);
    }
    
    @Override
    public Page<CategoryResponse> findAll(Pageable pageable) {
        log.debug("Finding all categories with pagination: {}", pageable);
        
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(entityMapper::toCategoryResponse);
    }
    
    @Override
    public List<CategoryResponse> findActiveCategories() {
        log.debug("Finding all active categories");
        
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        return categories.stream()
                .map(entityMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryResponse> findRootCategories() {
        log.debug("Finding root categories");
        
        List<Category> categories = categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        return categories.stream()
                .map(entityMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryResponse> findChildCategories(Long parentId) {
        log.debug("Finding child categories for parent id: {}", parentId);
        
        // Verify parent exists
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent category not found with id: " + parentId);
        }
        
        List<Category> categories = categoryRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(parentId);
        return categories.stream()
                .map(entityMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate URL-friendly slug from text
     * Handles Vietnamese characters
     */
    private String generateSlug(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Normalize Vietnamese characters
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        
        // Remove diacritics
        String noDiacritics = normalized.replaceAll("\\p{M}", "");
        
        // Convert to lowercase and replace spaces/special chars with hyphen
        String slug = noDiacritics.toLowerCase()
                .replaceAll("[đĐ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        
        return slug;
    }
}
