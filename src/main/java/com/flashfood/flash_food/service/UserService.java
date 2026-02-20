package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.request.ChangePasswordRequest;
import com.flashfood.flash_food.dto.request.UpdateProfileRequest;
import com.flashfood.flash_food.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for User management
 */
public interface UserService {
    
    /**
     * Get current user profile
     * @return Current user details
     */
    UserResponse getCurrentUserProfile();
    
    /**
     * Update current user profile
     * @param request Profile update data
     * @return Updated user
     */
    UserResponse updateProfile(UpdateProfileRequest request);
    
    /**
     * Change current user password
     * @param request Password change data
     */
    void changePassword(ChangePasswordRequest request);
    
    /**
     * Get user by ID (admin only)
     * @param userId User ID
     * @return User details
     */
    UserResponse getUserById(Long userId);
    
    /**
     * Get all users with pagination (admin only)
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<UserResponse> getAllUsers(Pageable pageable);
    
    /**
     * Get users by role (admin only)
     * @param role User role as string
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<UserResponse> getUsersByRole(String role, Pageable pageable);
    
    /**
     * Get users by status (admin only)
     * @param status User status as string
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<UserResponse> getUsersByStatus(String status, Pageable pageable);
    
    /**
     * Update user status (admin only)
     * @param userId User ID
     * @param status New status as string
     * @return Updated user
     */
    UserResponse updateUserStatus(Long userId, String status);
    
    /**
     * Update user role (admin only)
     * @param userId User ID
     * @param role New role as string
     * @return Updated user
     */
    UserResponse updateUserRole(Long userId, String role);
    
    /**
     * Delete user (admin only)
     * @param userId User ID
     */
    void deleteUser(Long userId);
    
    /**
     * Search users by keyword (admin only)
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Page of matching users
     */
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);
}
