package com.flashfood.flash_food.dto.response;

import com.flashfood.flash_food.entity.OrderStatus;
import com.flashfood.flash_food.entity.PaymentMethod;
import com.flashfood.flash_food.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order response
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
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
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
