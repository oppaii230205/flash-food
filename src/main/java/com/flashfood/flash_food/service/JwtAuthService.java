package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.request.LoginRequest;
import com.flashfood.flash_food.dto.request.LogoutRequest;
import com.flashfood.flash_food.dto.request.RefreshTokenRequest;
import com.flashfood.flash_food.dto.request.RegisterRequest;
import com.flashfood.flash_food.dto.response.AuthResponse;

/**
 * High-level authentication service.
 *
 * Responsibilities:
 *  - User registration (creates both {@code User} and {@code Profile} records).
 *  - Credential-based login (issues access + refresh token pair).
 *  - Refresh token rotation (invalidates old token, issues new pair).
 *  - Logout (explicitly revokes the supplied refresh token).
 */
public interface JwtAuthService {

    /**
     * Register a new user account and return a ready-to-use token pair.
     *
     * @param request registration details (email, password, name, phone)
     * @return token pair + user summary
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate an existing user with email + password.
     *
     * @param request login credentials
     * @return token pair + user summary
     */
    AuthResponse login(LoginRequest request);

    /**
     * Exchange a valid refresh token for a fresh access + refresh token pair.
     * The supplied refresh token is revoked immediately (token rotation).
     *
     * @param request wrapper containing the refresh token value
     * @return new token pair + user summary
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Revoke the supplied refresh token, effectively ending the session.
     *
     * @param request wrapper containing the refresh token value
     */
    void logout(LogoutRequest request);
}
