package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.LoginRequest;
import com.flashfood.flash_food.dto.request.LogoutRequest;
import com.flashfood.flash_food.dto.request.RefreshTokenRequest;
import com.flashfood.flash_food.dto.request.RegisterRequest;
import com.flashfood.flash_food.dto.response.ApiResponse;
import com.flashfood.flash_food.dto.response.AuthResponse;
import com.flashfood.flash_food.service.JwtAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication and token management.
 *
 * Public endpoints (no token required):
 *   POST /api/v1/auth/register        – create a new account
 *   POST /api/v1/auth/login           – obtain access + refresh tokens
 *   POST /api/v1/auth/refresh-token   – rotate tokens using a valid refresh token
 *
 * Authenticated endpoint:
 *   POST /api/v1/auth/logout          – revoke the current refresh token
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh and logout")
public class AuthController {

    private final JwtAuthService jwtAuthService;

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    @Operation(summary = "Register a new user account")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/v1/auth/register - email={}", request.getEmail());
        AuthResponse response = jwtAuthService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "User registered successfully", response));
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    @Operation(summary = "Authenticate with email and password")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /api/v1/auth/login - email={}", request.getEmail());
        AuthResponse response = jwtAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // -------------------------------------------------------------------------
    // Refresh token
    // -------------------------------------------------------------------------

    @Operation(summary = "Obtain a new access/refresh token pair using a valid refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("POST /api/v1/auth/refresh-token");
        AuthResponse response = jwtAuthService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------

    @Operation(summary = "Revoke the supplied refresh token (logout)")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {

        log.info("POST /api/v1/auth/logout");
        jwtAuthService.logout(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Logged out successfully")
                .httpCode(HttpStatus.OK.value())
                .build());
    }
}
