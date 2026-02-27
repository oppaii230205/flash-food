package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for store responses.
 * Enum fields (type, status) are serialised as their human-readable display names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private Long   id;
    private String name;
    private String address;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;

    // Enum display names
    private String type;
    private String status;

    private String    description;
    private String    imageUrl;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime flashSaleTime;

    private Double  rating;
    private Integer totalRatings;

    /** Store owner info */
    private Long   ownerId;
    private String ownerName;      // Profile full name

    /**
     * Whether the store is currently within its declared operating hours.
     * {@code null} when the store has not set business hours.
     */
    private Boolean isOpen;

    /** Distance from the requester in metres (populated only for nearby queries). */
    private Double distance;

    private LocalDateTime createdAt;
}
