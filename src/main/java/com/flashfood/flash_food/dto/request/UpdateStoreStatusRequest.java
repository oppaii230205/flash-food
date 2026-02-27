package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a store's status.
 * Using a dedicated DTO (rather than a raw {@code Map<String, String>}) gives us
 * Bean Validation out-of-the-box and a self-documenting API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreStatusRequest {

    /**
     * The target status as its display name, e.g. {@code "active"}, {@code "inactive"}.
     * Valid values are the display names of {@link com.flashfood.flash_food.entity.StoreStatus}.
     */
    @NotBlank(message = "Status is required")
    private String status;
}
