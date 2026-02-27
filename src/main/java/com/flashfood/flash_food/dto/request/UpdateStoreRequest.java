package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for updating an existing store.
 * All fields are optional — only non-null values are applied (PATCH semantics).
 * Constraints still apply when a field is provided.
 *
 * @see CreateStoreRequest for full-create use case
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreRequest {

    @Size(max = 150, message = "Store name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @DecimalMin(value = "-90.0",  message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0",   message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0",  message = "Longitude must be between -180 and 180")
    private Double longitude;

    /** One of the display names from {@link com.flashfood.flash_food.entity.StoreType}, or {@code null} to leave unchanged. */
    private String type;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 512, message = "Image URL must not exceed 512 characters")
    private String imageUrl;

    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime flashSaleTime;
}
