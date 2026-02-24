package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned after a successful authentication (register / login / token refresh).
 *
 * The client should:
 *  - Include {@code accessToken} in the {@code Authorization: Bearer <token>} header
 *    for all authenticated requests.
 *  - Store {@code refreshToken} securely (HTTP-only cookie or secure storage) and
 *    use it only at the refresh endpoint once the access token expires.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** Short-lived JWT used to authenticate API requests. */
    private String accessToken;

    /** Long-lived opaque token used to obtain a new access token. */
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    /** Access token validity in seconds (convenience field for the client). */
    private Long expiresIn;

    private UserResponse user;
}
