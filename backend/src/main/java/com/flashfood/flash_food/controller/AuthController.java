package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.LoginRequest;
import com.flashfood.flash_food.dto.request.LogoutRequest;
import com.flashfood.flash_food.dto.request.RefreshTokenRequest;
import com.flashfood.flash_food.dto.request.RegisterRequest;
import com.flashfood.flash_food.dto.response.ApiResponse;
import com.flashfood.flash_food.dto.response.AuthResponse;
import com.flashfood.flash_food.exception.TokenException;
import com.flashfood.flash_food.service.JwtAuthService;
import com.flashfood.flash_food.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * REST controller for authentication and token management.
 *
 * Refresh token strategy:
 *   The refresh token is NEVER returned in the JSON response body. Instead, it is
 *   delivered and received exclusively through an HTTP-only cookie named
 *   {@value #REFRESH_TOKEN_COOKIE}. This prevents JavaScript running in the browser
 *   from ever reading the value, effectively eliminating XSS-based token theft.
 *
 * Public endpoints (no token required):
 *   POST /api/v1/auth/register        – create a new account
 *   POST /api/v1/auth/login           – obtain access token; refresh token set as cookie
 *   POST /api/v1/auth/refresh-token   – rotate tokens; no request body needed
 *
 * Authenticated endpoint:
 *   POST /api/v1/auth/logout          – revoke the refresh token cookie
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh and logout")
public class AuthController {

    static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final JwtAuthService jwtAuthService;
    private final JwtService jwtService;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    @Operation(summary = "Register a new user account")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse httpResponse) {

        log.info("POST /api/v1/auth/register - email={}", request.getEmail());
        AuthResponse response = jwtAuthService.register(request);
        setRefreshTokenCookie(httpResponse, response.getRefreshToken());
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
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {

        log.info("POST /api/v1/auth/login - email={}", request.getEmail());
        AuthResponse response = jwtAuthService.login(request);
        setRefreshTokenCookie(httpResponse, response.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // -------------------------------------------------------------------------
    // Refresh token  (no request body — token comes from the HTTP-only cookie)
    // -------------------------------------------------------------------------

    @Operation(summary = "Rotate the access/refresh token pair. Refresh token is read from the HTTP-only cookie automatically.")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        log.info("POST /api/v1/auth/refresh-token");
        String refreshTokenValue = extractRefreshTokenCookie(httpRequest);
        AuthResponse response = jwtAuthService.refreshToken(
                RefreshTokenRequest.builder().refreshToken(refreshTokenValue).build());
        setRefreshTokenCookie(httpResponse, response.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    // -------------------------------------------------------------------------
    // Logout  (no request body — token comes from the HTTP-only cookie)
    // -------------------------------------------------------------------------

    @Operation(summary = "Revoke the refresh token and clear the cookie. Requires a valid access token.")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        log.info("POST /api/v1/auth/logout");
        String refreshTokenValue = extractRefreshTokenCookie(httpRequest);
        jwtAuthService.logout(LogoutRequest.builder().refreshToken(refreshTokenValue).build());
        clearRefreshTokenCookie(httpResponse);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Logged out successfully")
                .httpCode(HttpStatus.OK.value())
                .build());
    }

    // -------------------------------------------------------------------------
    // Cookie helpers
    // -------------------------------------------------------------------------

    private void setRefreshTokenCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/v1/auth"); // sent only to auth endpoints
        cookie.setMaxAge((int) (jwtService.getRefreshTokenExpirationMs() / 1000L));
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0); // instruct browser to delete immediately
        response.addCookie(cookie);
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(() -> new TokenException("Refresh token cookie is missing"));
        }
        throw new TokenException("Refresh token cookie is missing");
    }
}
