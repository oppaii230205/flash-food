package com.flashfood.flash_food.service;

import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.entity.UserRole;
import com.flashfood.flash_food.entity.Store;

/**
 * Service for handling authentication and authorization context
 * Centralized service to get current user and check permissions
 */
public interface AuthenticationService {
    
    /**
     * Get currently authenticated user from security context
     * @return Current user
     * @throws com.flashfood.flash_food.exception.AccessDeniedException if not authenticated
     */
    User getCurrentUser();
    
    /**
     * Check if current user has a specific role
     * @param role Role to check
     * @return true if user has the role
     */
    boolean hasRole(UserRole role);
    
    /**
     * Check if current user has any of the specified roles
     * @param roles Roles to check
     * @return true if user has at least one role
     */
    boolean hasAnyRole(UserRole... roles);
    
    /**
     * Check if current user has all of the specified roles
     * @param roles Roles to check
     * @return true if user has all roles
     */
    boolean hasAllRoles(UserRole... roles);
    
    /**
     * Check if current user is admin
     * @return true if user has ADMIN role
     */
    boolean isAdmin();
    
    /**
     * Check if current user is the owner of a store or an admin
     * @param store Store to check ownership
     * @return true if user is owner or admin
     */
    boolean isStoreOwnerOrAdmin(Store store);
}
