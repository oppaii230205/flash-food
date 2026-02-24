package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.LoginRequest;
import com.flashfood.flash_food.dto.request.LogoutRequest;
import com.flashfood.flash_food.dto.request.RefreshTokenRequest;
import com.flashfood.flash_food.dto.request.RegisterRequest;
import com.flashfood.flash_food.dto.response.AuthResponse;
import com.flashfood.flash_food.entity.Profile;
import com.flashfood.flash_food.entity.RefreshToken;
import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.entity.UserRole;
import com.flashfood.flash_food.entity.UserStatus;
import com.flashfood.flash_food.exception.ResourceAlreadyExistsException;
import com.flashfood.flash_food.exception.ResourceNotFoundException;
import com.flashfood.flash_food.exception.TokenException;
import com.flashfood.flash_food.repository.ProfileRepository;
import com.flashfood.flash_food.repository.RefreshTokenRepository;
import com.flashfood.flash_food.repository.UserRepository;
import com.flashfood.flash_food.service.JwtAuthService;
import com.flashfood.flash_food.service.JwtService;
import com.flashfood.flash_food.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link JwtAuthService}.
 *
 * Handles the full authentication lifecycle:
 * register → login → refresh → logout.
 *
 * Token strategy:
 *  - Access token  : short-lived JWT (default 15 min), stateless.
 *  - Refresh token : long-lived opaque UUID (default 7 days), stored in DB.
 *    Rotation is applied on every refresh call — old token is immediately
 *    revoked and a brand-new one is issued.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtAuthServiceImpl implements JwtAuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EntityMapper entityMapper;
    private final AuthenticationManager authenticationManager;

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Uniqueness checks
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Email is already registered: " + request.getEmail());
        }
        if (profileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException(
                    "Phone number is already registered: " + request.getPhoneNumber());
        }

        // Persist User (auth credentials only)
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(new java.util.HashSet<>(Set.of(UserRole.CUSTOMER)))
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        // Persist Profile (personal data)
        Profile profile = Profile.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        profileRepository.save(profile);

        // Wire profile into the in-memory user so entityMapper can read it
        savedUser.setProfile(profile);

        // Issue tokens
        String accessToken = jwtService.generateAccessToken(savedUser);
        RefreshToken refreshToken = createAndPersistRefreshToken(savedUser);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return buildAuthResponse(accessToken, refreshToken.getToken(), savedUser);
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException (→ 401) on failure — handled globally
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.getEmail()));

        // Revoke all previous refresh tokens (single-session policy)
        revokeAllActiveRefreshTokens(user);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createAndPersistRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    // -------------------------------------------------------------------------
    // Refresh token
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (!storedToken.isValid()) {
            String reason = storedToken.getRevoked() ? "revoked" : "expired";
            throw new TokenException("Refresh token has been " + reason);
        }

        // Rotate: revoke old token immediately
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();

        // Issue fresh pair
        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = createAndPersistRefreshToken(user);

        log.info("Tokens rotated for user: {}", user.getEmail());

        return buildAuthResponse(newAccessToken, newRefreshToken.getToken(), user);
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        log.info("User logged out: {}", storedToken.getUser().getEmail());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private RefreshToken createAndPersistRefreshToken(User user) {
        long expirationMs = jwtService.getRefreshTokenExpirationMs();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(jwtService.generateRefreshTokenValue())
                .user(user)
                .expiresAt(LocalDateTime.now().plusNanos(expirationMs * 1_000_000L))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private void revokeAllActiveRefreshTokens(User user) {
        List<RefreshToken> activeTokens =
                refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        if (!activeTokens.isEmpty()) {
            activeTokens.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(activeTokens);
        }
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshTokenValue, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000L)
                .user(entityMapper.toUserResponse(user))
                .build();
    }
}
