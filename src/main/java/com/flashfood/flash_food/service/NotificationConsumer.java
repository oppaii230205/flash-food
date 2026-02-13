package com.flashfood.flash_food.service;

import com.flashfood.flash_food.config.RabbitMQConfig;
import com.flashfood.flash_food.dto.message.NotificationMessage;
import com.flashfood.flash_food.entity.Notification;
import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.repository.NotificationRepository;
import com.flashfood.flash_food.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for consuming notification messages from RabbitMQ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    /**
     * Listen to notification queue and save notifications to database
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    @Transactional
    public void handleNotification(NotificationMessage message) {
        try {
            log.info("Received notification message: {}", message);
            
            List<User> users = userRepository.findAllById(message.getUserIds());
            
            for (User user : users) {
                Notification notification = Notification.builder()
                        .user(user)
                        .title(message.getTitle())
                        .message(message.getMessage())
                        .type(message.getType())
                        .referenceId(message.getReferenceId())
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("Saved {} notifications", users.size());
        } catch (Exception e) {
            log.error("Error processing notification message", e);
        }
    }
    
    /**
     * Listen to flash sale queue and send notifications to nearby users
     */
    @RabbitListener(queues = RabbitMQConfig.FLASH_SALE_QUEUE)
    @Transactional
    public void handleFlashSaleNotification(NotificationMessage message) {
        try {
            log.info("Received flash sale notification: {}", message);
            
            // TODO: Use Redis Geo to find nearby users
            // For now, use the provided user IDs
            List<User> users = userRepository.findAllById(message.getUserIds());
            
            for (User user : users) {
                Notification notification = Notification.builder()
                        .user(user)
                        .title(message.getTitle())
                        .message(message.getMessage())
                        .type(message.getType())
                        .referenceId(message.getReferenceId())
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("Sent flash sale notification to {} users", users.size());
        } catch (Exception e) {
            log.error("Error processing flash sale notification", e);
        }
    }
}
