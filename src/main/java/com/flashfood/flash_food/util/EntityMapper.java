package com.flashfood.flash_food.util;

import com.flashfood.flash_food.dto.response.*;
import com.flashfood.flash_food.entity.*;
import lombok.experimental.UtilityClass;

/**
 * Utility class for mapping entities to DTOs
 * Handles conversion of enums to strings for client API
 */
@UtilityClass
public class EntityMapper {
    
    /**
     * Map User entity to UserResponse DTO
     */
    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .notificationEnabled(user.getNotificationEnabled())
                .notificationRadius(user.getNotificationRadius())
                .role(user.getRole() != null ? user.getRole().getDisplayName() : null)
                .status(user.getStatus() != null ? user.getStatus().getDisplayName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    /**
     * Map Store entity to StoreResponse DTO
     */
    public StoreResponse toStoreResponse(Store store) {
        if (store == null) return null;
        
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .type(store.getType() != null ? store.getType().getDisplayName() : null)
                .status(store.getStatus() != null ? store.getStatus().getDisplayName() : null)
                .description(store.getDescription())
                .imageUrl(store.getImageUrl())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .flashSaleTime(store.getFlashSaleTime())
                .rating(store.getRating())
                .totalRatings(store.getTotalRatings())
                .build();
    }
    
    /**
     * Map FoodItem entity to FoodItemResponse DTO
     */
    public FoodItemResponse toFoodItemResponse(FoodItem foodItem) {
        if (foodItem == null) return null;
        
        Category category = foodItem.getCategory();
        
        return FoodItemResponse.builder()
                .id(foodItem.getId())
                .storeId(foodItem.getStore() != null ? foodItem.getStore().getId() : null)
                .storeName(foodItem.getStore() != null ? foodItem.getStore().getName() : null)
                .name(foodItem.getName())
                .description(foodItem.getDescription())
                .imageUrl(foodItem.getImageUrl())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categorySlug(category != null ? category.getSlug() : null)
                .originalPrice(foodItem.getOriginalPrice())
                .flashPrice(foodItem.getFlashPrice())
                .discountPercent(foodItem.getDiscountPercent())
                .quantity(foodItem.getQuantity())
                .soldQuantity(foodItem.getSoldQuantity())
                .expiryTime(foodItem.getExpiryTime())
                .status(foodItem.getStatus() != null ? foodItem.getStatus().getDisplayName() : null)
                .createdAt(foodItem.getCreatedAt())
                .build();
    }
    
    /**
     * Map Order entity to OrderResponse DTO
     */
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .storeId(order.getStore() != null ? order.getStore().getId() : null)
                .storeName(order.getStore() != null ? order.getStore().getName() : null)
                .storeAddress(order.getStore() != null ? order.getStore().getAddress() : null)
                .items(order.getOrderItems() != null ? 
                        order.getOrderItems().stream()
                                .map(EntityMapper::toOrderItemResponse)
                                .toList() : null)
                .totalAmount(order.getTotalAmount())
                .originalAmount(order.getOriginalAmount())
                .status(order.getStatus() != null ? order.getStatus().getDisplayName() : null)
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().getDisplayName() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().getDisplayName() : null)
                .pickupTime(order.getPickupTime())
                .specialInstructions(order.getSpecialInstructions())
                .createdAt(order.getCreatedAt())
                .build();
    }
    
    /**
     * Map OrderItem entity to OrderItemResponse DTO
     */
    private OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) return null;
        
        FoodItem foodItem = orderItem.getFoodItem();
        
        return OrderResponse.OrderItemResponse.builder()
                .id(orderItem.getId())
                .foodItemId(foodItem != null ? foodItem.getId() : null)
                .foodItemName(foodItem != null ? foodItem.getName() : null)
                .foodItemImage(foodItem != null ? foodItem.getImageUrl() : null)
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
    
    /**
     * Map Notification entity to NotificationResponse DTO
     */
    public NotificationResponse toNotificationResponse(Notification notification) {
        if (notification == null) return null;
        
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType() != null ? notification.getType().getDisplayName() : null)
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
    
    /**
     * Map Category entity to CategoryResponse DTO
     */
    public CategoryResponse toCategoryResponse(Category category) {
        if (category == null) return null;
        
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .build();
    }
}
