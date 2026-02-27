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
 * Store entity - Represents food stores/restaurants.
 *
 * Enum fields (type, status) are stored as INTEGER via auto-applied JPA converters
 * ({@link com.flashfood.flash_food.entity.converter.StoreTypeConverter} /
 *  {@link com.flashfood.flash_food.entity.converter.StoreStatusConverter}).
 * Clients always send/receive those fields as human-readable strings.
 */
@Entity
@Table(name = "stores", indexes = {
    @Index(name = "idx_store_status",   columnList = "status"),
    @Index(name = "idx_store_type",     columnList = "type"),
    @Index(name = "idx_store_owner_id", columnList = "owner_id"),
    @Index(name = "idx_store_name",     columnList = "name")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    // Geo-spatial coordinates for Redis Geo
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** Store category — persisted as INTEGER via {@link com.flashfood.flash_food.entity.converter.StoreTypeConverter}. */
    @Column(nullable = false)
    private StoreType type;

    @Column(length = 1000)
    private String description;

    @Column(length = 512)
    private String imageUrl;

    // Business hours
    private LocalTime openTime;
    private LocalTime closeTime;

    // Flash sale time (e.g., 9:00 PM)
    private LocalTime flashSaleTime;

    /** Store lifecycle status — persisted as INTEGER via {@link com.flashfood.flash_food.entity.converter.StoreStatusConverter}. */
    @Builder.Default
    @Column(nullable = false)
    private StoreStatus status = StoreStatus.PENDING_APPROVAL;

    // Owner relationship (FK to users table)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Aggregate rating
    @Builder.Default
    @Column(nullable = false)
    private Double rating = 0.0;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalRatings = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when the store is currently within its declared
     * operating hours.  Safe to call even when {@code openTime}/{@code closeTime}
     * are not set (treats the store as open in that case).
     */
    public boolean isOpen() {
        if (openTime == null || closeTime == null) {
            return true;
        }
        LocalTime now = LocalTime.now();
        return !now.isBefore(openTime) && !now.isAfter(closeTime);
    }
}
