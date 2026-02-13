package com.flashfood.flash_food.service;

import com.flashfood.flash_food.config.RabbitMQConfig;
import com.flashfood.flash_food.dto.message.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing messages to RabbitMQ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Publish notification message to notification queue
     */
    public void publishNotification(NotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.send",
                message
            );
            log.info("Published notification message: {}", message);
        } catch (Exception e) {
            log.error("Error publishing notification message", e);
        }
    }
    
    /**
     * Publish flash sale notification to multiple users
     */
    public void publishFlashSaleNotification(NotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.FLASH_SALE_EXCHANGE,
                "flash-sale.notify",
                message
            );
            log.info("Published flash sale notification: {}", message);
        } catch (Exception e) {
            log.error("Error publishing flash sale notification", e);
        }
    }
    
    /**
     * Publish order event
     */
    public void publishOrderEvent(String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                routingKey,
                message
            );
            log.info("Published order event with routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Error publishing order event", e);
        }
    }
}
