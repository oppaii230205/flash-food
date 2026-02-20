package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.ChangePasswordRequest;
import com.flashfood.flash_food.dto.request.UpdateProfileRequest;
import com.flashfood.flash_food.dto.response.ApiResponse;
import com.flashfood.flash_food.dto.response.UserResponse;
import com.flashfood.flash_food.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User management
 * Handles user profiles and admin user operations
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     * @return Current user details
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {
        log.info("GET /api/users/me - Getting current user profile");
        UserResponse response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update current user profile
     * @param request Profile update data
     * @return Updated user
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        
        log.info("PUT /api/users/me - Updating current user profile");
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    /**
     * Change current user password
     * @param request Password change data
     * @return Success message
     */
    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        
        log.info("POST /api/users/me/change-password - Changing password");
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    /**
     * Get user by ID (admin only)
     * @param userId User ID
     * @return User details
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        log.info("GET /api/users/{} - Getting user by ID", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all users (admin only)
     * @param pageable Pagination parameters
     * @return Page of users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/users - Getting all users");
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get users by role (admin only)
     * @param role User role
     * @param pageable Pagination parameters
     * @return Page of users
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @PathVariable String role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/users/role/{} - Getting users by role", role);
        Page<UserResponse> response = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get users by status (admin only)
     * @param status User status
     * @param pageable Pagination parameters
     * @return Page of users
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/users/status/{} - Getting users by status", status);
        Page<UserResponse> response = userService.getUsersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search users by keyword (admin only)
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Page of matching users
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/users/search?keyword={} - Searching users", keyword);
        Page<UserResponse> response = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update user status (admin only)
     * @param userId User ID
     * @param status New status
     * @return Updated user
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        
        log.info("PATCH /api/users/{}/status?status={} - Updating user status", userId, status);
        UserResponse response = userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success(response, "User status updated successfully"));
    }

    /**
     * Update user role (admin only)
     * @param userId User ID
     * @param role New role
     * @return Updated user
     */
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        
        log.info("PATCH /api/users/{}/role?role={} - Updating user role", userId, role);
        UserResponse response = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success(response, "User role updated successfully"));
    }

    /**
     * Delete user (admin only)
     * @param userId User ID
     * @return Success message
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /api/users/{} - Deleting user", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
