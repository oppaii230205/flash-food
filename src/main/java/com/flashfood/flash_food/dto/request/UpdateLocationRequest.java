package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating the current user's geo-location and notification radius.
 * All fields are optional – only non-null values are applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLocationRequest {

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0",  message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0",  message = "Longitude must be between -180 and 180")
    private Double longitude;

    /** Radius in kilometres within which the user wants to receive flash-sale alerts. */
    @DecimalMin(value = "0.1", message = "Notification radius must be at least 0.1 km")
    @DecimalMax(value = "50.0", message = "Notification radius must not exceed 50 km")
    private Double notificationRadius;
}
