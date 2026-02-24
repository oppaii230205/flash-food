package com.flashfood.flash_food.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Profile entity - Holds personal/display information for a user.
 * Decoupled from the User entity so that auth credentials are kept separate
 * from profile data (single-responsibility principle).
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Back-reference to the owning User (FK: profiles.user_id → users.id). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private String address;

    private String avatarUrl;

    /** User's last-known latitude for geo-spatial notifications. */
    private Double latitude;

    /** User's last-known longitude for geo-spatial notifications. */
    private Double longitude;

    @Builder.Default
    private Boolean notificationEnabled = true;

    /** Radius in kilometres within which the user wants to receive flash-sale alerts. */
    @Builder.Default
    private Double notificationRadius = 1.0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
