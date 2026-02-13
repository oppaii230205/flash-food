package com.flashfood.flash_food.dto.response;

import com.flashfood.flash_food.entity.StoreStatus;
import com.flashfood.flash_food.entity.StoreType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for store response
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
    private StoreType type;
    private String description;
    private String imageUrl;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime flashSaleTime;
    private StoreStatus status;
    private Double rating;
    private Integer totalRatings;
    
    // Distance in meters (calculated from user location)
    private Double distance;
}
