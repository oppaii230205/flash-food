package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for creating/updating store
 * Client sends enum values as strings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreRequest {
    
    @NotBlank(message = "Store name is required")
    private String name;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    @NotBlank(message = "Store type is required")
    private String type; // Client sends string like "restaurant", "supermarket", etc.
    
    private String description;
    private String imageUrl;
    
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime flashSaleTime;
}
