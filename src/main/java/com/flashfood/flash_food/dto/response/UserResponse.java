package com.flashfood.flash_food.dto.response;

import com.flashfood.flash_food.entity.UserRole;
import com.flashfood.flash_food.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private Boolean notificationEnabled;
    private Double notificationRadius;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
