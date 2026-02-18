package com.flashfood.flash_food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for notification response
 * Enums are serialized as strings for client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    private String title;
    private String message;
    
    // Enum as string
    private String type;
    
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
