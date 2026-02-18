package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order response
 * Enums are serialized as strings for client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private BigDecimal originalAmount;
    
    // Enums as strings
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    
    private LocalDateTime pickupTime;
    private String specialInstructions;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long foodItemId;
        private String foodItemName;
        private String foodItemImage;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
