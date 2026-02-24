package com.flashfood.flash_food.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned after a successful authentication (register / login / token refresh).
 *
 * The {@code refreshToken} field is intentionally excluded from JSON serialization.
 * It is delivered to the client exclusively via an HTTP-only {@code Set-Cookie} header
 * set by the {@code AuthController}, so that JavaScript running in the browser can
 * never read it (protects against XSS token theft).
 *
 * The client should:
 *  - Include {@code accessToken} in the {@code Authorization: Bearer <token>} header.
 *  - Let the browser automatically attach the refresh-token cookie on subsequent
 *    calls to {@code /api/v1/auth/refresh-token} and {@code /api/v1/auth/logout}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** Short-lived JWT used to authenticate API requests. */
    private String accessToken;

    /**
     * Long-lived opaque refresh token.
     * Never serialized into the JSON response body — communicated via HTTP-only cookie only.
     */
    @JsonIgnore
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    /** Access token validity in seconds (convenience field for the client). */
    private Long expiresIn;

    private UserResponse user;
}
