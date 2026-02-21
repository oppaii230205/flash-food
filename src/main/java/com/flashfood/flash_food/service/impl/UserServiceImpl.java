package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.ChangePasswordRequest;
import com.flashfood.flash_food.dto.request.UpdateProfileRequest;
import com.flashfood.flash_food.dto.response.UserResponse;
import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.entity.UserRole;
import com.flashfood.flash_food.entity.UserStatus;
import com.flashfood.flash_food.exception.DuplicateResourceException;
import com.flashfood.flash_food.exception.InvalidOperationException;
import com.flashfood.flash_food.exception.ResourceNotFoundException;
import com.flashfood.flash_food.util.EntityMapper;
import com.flashfood.flash_food.repository.UserRepository;
import com.flashfood.flash_food.service.AuthenticationService;
import com.flashfood.flash_food.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserService
 * Handles user profile management and admin operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;
    private final EntityMapper entityMapper;

    @Override
    public UserResponse getCurrentUserProfile() {
        log.debug("Getting current user profile");
        User currentUser = authenticationService.getCurrentUser();
        return entityMapper.toUserResponse(currentUser);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        log.info("Updating profile for current user");

        User currentUser = authenticationService.getCurrentUser();

        // Update fields if provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            currentUser.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if email is already used by another user
            if (!currentUser.getEmail().equals(request.getEmail()) && 
                    userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email is already in use");
            }
            currentUser.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            // Check if phone number is already used by another user
            if (!currentUser.getPhoneNumber().equals(request.getPhoneNumber()) && 
                    userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new DuplicateResourceException("Phone number is already in use");
            }
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getAddress() != null) {
            currentUser.setAddress(request.getAddress());
        }

        User updatedUser = userRepository.save(currentUser);
        log.info("Profile updated successfully for user ID: {}", updatedUser.getId());

        return entityMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        log.info("Changing password for current user");

        User currentUser = authenticationService.getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("New password and confirm password do not match");
        }

        // Verify new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new InvalidOperationException("New password must be different from current password");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        log.info("Password changed successfully for user ID: {}", currentUser.getId());
    }

    @Override
    public UserResponse getUserById(Long userId) {
        log.debug("Getting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return entityMapper.toUserResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination");

        Page<User> users = userRepository.findAll(pageable);
        return users.map(entityMapper::toUserResponse);
    }

    @Override
    public Page<UserResponse> getUsersByRole(String role, Pageable pageable) {
        log.debug("Getting users with role: {}", role);

        // Parse role
        UserRole userRole;
        try {
            userRole = UserRole.fromDisplayName(role);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid user role: " + role);
        }

        List<User> users = userRepository.findByRole(userRole);

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());
        List<User> pageContent = users.subList(start, end);

        Page<User> page = new PageImpl<>(pageContent, pageable, users.size());
        return page.map(entityMapper::toUserResponse);
    }

    @Override
    public Page<UserResponse> getUsersByStatus(String status, Pageable pageable) {
        log.debug("Getting users with status: {}", status);

        // Parse status
        UserStatus userStatus;
        try {
            userStatus = UserStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid user status: " + status);
        }

        List<User> users = userRepository.findByStatus(userStatus);

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());
        List<User> pageContent = users.subList(start, end);

        Page<User> page = new PageImpl<>(pageContent, pageable, users.size());
        return page.map(entityMapper::toUserResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, String status) {
        log.info("Updating status for user ID: {} to: {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        User currentUser = authenticationService.getCurrentUser();

        // Parse status
        UserStatus userStatus;
        try {
            userStatus = UserStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid user status: " + status);
        }

        // Cannot change own status
        if (user.getId().equals(currentUser.getId())) {
            throw new InvalidOperationException("Cannot change your own status");
        }

        user.setStatus(userStatus);
        User updatedUser = userRepository.save(user);

        log.info("User status updated successfully");
        return entityMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, String role) {
        log.info("Updating role for user ID: {} to: {}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        User currentUser = authenticationService.getCurrentUser();

        // Parse role
        UserRole userRole;
        try {
            userRole = UserRole.fromDisplayName(role);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid user role: " + role);
        }

        // Cannot change own role
        if (user.getId().equals(currentUser.getId())) {
            throw new InvalidOperationException("Cannot change your own role");
        }

        user.getRoles().clear();
        user.getRoles().add(userRole);
        User updatedUser = userRepository.save(user);

        log.info("User role updated successfully");
        return entityMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        User currentUser = authenticationService.getCurrentUser();

        // Cannot delete own account
        if (user.getId().equals(currentUser.getId())) {
            throw new InvalidOperationException("Cannot delete your own account");
        }

        // Soft delete by setting status to DELETED
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User deleted successfully (soft delete)");
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        log.debug("Searching users with keyword: {}", keyword);

        String lowerKeyword = keyword.toLowerCase();

        List<User> allUsers = userRepository.findAll();
        List<User> matchingUsers = allUsers.stream()
                .filter(user -> 
                    user.getFullName().toLowerCase().contains(lowerKeyword) ||
                    user.getEmail().toLowerCase().contains(lowerKeyword) ||
                    (user.getPhoneNumber() != null && user.getPhoneNumber().contains(keyword))
                )
                .collect(Collectors.toList());

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matchingUsers.size());
        List<User> pageContent = matchingUsers.subList(start, end);

        Page<User> page = new PageImpl<>(pageContent, pageable, matchingUsers.size());
        return page.map(entityMapper::toUserResponse);
    }
}
