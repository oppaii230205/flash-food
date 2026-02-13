package com.flashfood.flash_food.dto.message;

import com.flashfood.flash_food.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Message object for RabbitMQ notification messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    
    private List<Long> userIds; // List of user IDs to notify
    private String title;
    private String message;
    private NotificationType type;
    private Long referenceId;
    
    // For geo-based notifications
    private Double latitude;
    private Double longitude;
    private Double radius; // in kilometers
}
