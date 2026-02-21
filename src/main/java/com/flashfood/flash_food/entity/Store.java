package com.flashfood.flash_food.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Store entity - Represents food stores/restaurants
 */
@Entity
@Table(name = "stores")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String address;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    // Geo-spatial coordinates for Redis Geo
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    private StoreType type; // BAKERY, RESTAURANT, CONVENIENCE_STORE, etc.
    
    @Column(length = 1000)
    private String description;
    
    private String imageUrl;
    
    // Business hours
    private LocalTime openTime;
    private LocalTime closeTime;
    
    // Flash sale time (e.g., 9:00 PM)
    private LocalTime flashSaleTime;
    
    @Builder.Default
    private StoreStatus status = StoreStatus.ACTIVE;
    
    // Owner relationship (FK to users table)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    // Rating
    @Builder.Default
    private Double rating = 0.0;
    @Builder.Default
    private Integer totalRatings = 0;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
