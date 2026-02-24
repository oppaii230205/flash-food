package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * DTO for updating user profile.
 * All fields are optional – only non-null values are applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String fullName;

    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phoneNumber;

    private String address;

    /** Full URL of the user's avatar image. */
    @URL(message = "Avatar URL must be a valid URL")
    private String avatarUrl;

    /** Enable or disable push notifications for this account. */
    private Boolean notificationEnabled;
}
