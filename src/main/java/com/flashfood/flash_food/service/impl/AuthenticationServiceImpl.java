package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.entity.Store;
import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.entity.UserRole;
import com.flashfood.flash_food.exception.AccessDeniedException;
import com.flashfood.flash_food.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuthenticationService
 * Provides centralized authentication and authorization utilities
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found in security context");
            throw new AccessDeniedException("User is not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            log.error("Principal is not a User instance: {}", principal.getClass().getName());
            throw new AccessDeniedException("Invalid authentication principal");
        }
        
        return (User) principal;
    }

    @Override
    public boolean hasRole(UserRole role) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRoles().contains(role);
        } catch (AccessDeniedException e) {
            log.debug("Failed to get current user for role check", e);
            return false;
        }
    }

    @Override
    public boolean hasAnyRole(UserRole... roles) {
        try {
            User currentUser = getCurrentUser();
            for (UserRole role : roles) {
                if (currentUser.getRoles().contains(role)) {
                    return true;
                }
            }
            return false;
        } catch (AccessDeniedException e) {
            log.debug("Failed to get current user for roles check", e);
            return false;
        }
    }

    @Override
    public boolean hasAllRoles(UserRole... roles) {
        try {
            User currentUser = getCurrentUser();
            for (UserRole role : roles) {
                if (!currentUser.getRoles().contains(role)) {
                    return false;
                }
            }
            return true;
        } catch (AccessDeniedException e) {
            log.debug("Failed to get current user for roles check", e);
            return false;
        }
    }

    @Override
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    @Override
    public boolean isStoreOwnerOrAdmin(Store store) {
        try {
            User currentUser = getCurrentUser();
            
            // Admin can access any store
            if (currentUser.getRoles().contains(UserRole.ADMIN)) {
                return true;
            }
            
            // Check if user is store owner
            if (currentUser.getRoles().contains(UserRole.STORE_OWNER)) {
                return store.getOwner() != null && store.getOwner().getId().equals(currentUser.getId());
            }
            
            return false;
        } catch (AccessDeniedException e) {
            log.debug("Failed to get current user for ownership check", e);
            return false;
        }
    }
}
