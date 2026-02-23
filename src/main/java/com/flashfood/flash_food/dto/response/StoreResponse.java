package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for store response
 * Enums are serialized as strings for client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {
    
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    
    // Enums as strings
    private String type;
    private String status;
    
    private String description;
    private String imageUrl;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime flashSaleTime;
    private Double rating;
    private Integer totalRatings;
    private Long ownerId;
    
    // Distance in meters (calculated from user location)
    private Double distance;
}
