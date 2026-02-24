package com.flashfood.flash_food.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * RefreshToken entity - persists opaque refresh tokens (UUIDs) for each user session.
 *
 * Design notes:
 * - Access tokens are short-lived JWTs (stateless).
 * - Refresh tokens are long-lived UUIDs stored here so they can be explicitly revoked
 *   (logout, password change, suspicious activity).
 * - Token rotation is applied on every refresh: old token is marked revoked and a new
 *   one is issued, limiting the replay-attack window.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_token", columnList = "token"),
        @Index(name = "idx_refresh_token_user_id", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Opaque UUID value sent to / received from the client. */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Explicitly revoked (logout / token rotation / security event). */
    @Builder.Default
    private Boolean revoked = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // -------------------------------------------------------------------------
    // Domain helpers
    // -------------------------------------------------------------------------

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /** A token is valid only when it has not been revoked AND has not expired. */
    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
