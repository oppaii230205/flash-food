package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user response
 * Enums are serialized as strings for client
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
    
    // Enums as strings
    private String role;
    private String status;
    
    private LocalDateTime createdAt;
}
