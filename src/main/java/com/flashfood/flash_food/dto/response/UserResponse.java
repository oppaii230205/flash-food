package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Flat DTO that combines fields from both {@code User} (auth) and {@code Profile}
 * (personal data) for a clean, unified client-facing representation.
 *
 * Enums are serialized as human-readable strings via their {@code displayName}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    // --- from User ---
    private Long id;
    private String email;

    // --- from Profile ---
    private String fullName;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Double latitude;
    private Double longitude;
    private Boolean notificationEnabled;
    private Double notificationRadius;

    // --- Enums as strings ---
    private String role;
    private String status;

    private LocalDateTime createdAt;
}
