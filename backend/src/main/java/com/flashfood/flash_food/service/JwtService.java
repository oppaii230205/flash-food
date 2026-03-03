package com.flashfood.flash_food.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service for low-level JWT operations: generation, parsing and validation.
 *
 * Intentionally kept narrow — it knows nothing about the database or business
 * logic.  Higher-level concerns (issuing tokens, refresh-token persistence, etc.)
 * belong to {@link JwtAuthService}.
 */
public interface JwtService {

    /**
     * Generate a signed, short-lived access token for the given principal.
     *
     * @param userDetails the authenticated user
     * @return compact JWT string
     */
    String generateAccessToken(UserDetails userDetails);

    /**
     * Generate a random, opaque value suitable for use as a refresh token.
     * The returned value is NOT a JWT — it is an opaque UUID stored server-side.
     *
     * @return UUID string
     */
    String generateRefreshTokenValue();

    /**
     * Extract the {@code sub} (email) claim from an access token.
     *
     * @param token JWT access token
     * @return email stored in the subject claim
     */
    String extractEmail(String token);

    /**
     * Verify that the token is signed correctly, has not expired, and belongs
     * to the given principal.
     *
     * @param token       JWT access token
     * @param userDetails user to validate against
     * @return {@code true} if the token is valid
     */
    boolean isAccessTokenValid(String token, UserDetails userDetails);

    /** Configured access-token TTL in milliseconds. */
    long getAccessTokenExpirationMs();

    /** Configured refresh-token TTL in milliseconds. */
    long getRefreshTokenExpirationMs();
}
