package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for cancelling an order.
 * The {@code reason} field is optional — clients may omit it entirely.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {

    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String reason;
}
