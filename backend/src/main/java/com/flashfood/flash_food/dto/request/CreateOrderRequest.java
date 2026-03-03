package com.flashfood.flash_food.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating order
 * Client sends enum values as strings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotNull(message = "Store ID is required")
    private Long storeId;
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // Client sends string like "cash", "card", "e-wallet"
    
    private LocalDateTime pickupTime;
    private String specialInstructions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Food item ID is required")
        private Long foodItemId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
