package com.flashfood.flash_food.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for location update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {
    
    private Double latitude;
    private Double longitude;
    private Double notificationRadius; // in kilometers
}
