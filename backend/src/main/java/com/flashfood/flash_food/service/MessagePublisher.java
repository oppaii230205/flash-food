package com.flashfood.flash_food.service;

import com.flashfood.flash_food.config.RabbitMQConfig;
import com.flashfood.flash_food.dto.message.FlashSaleMessage;
import com.flashfood.flash_food.dto.message.NotificationMessage;
import com.flashfood.flash_food.dto.message.OrderEventMessage;
import com.flashfood.flash_food.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around {@link RabbitTemplate} that provides type-safe publish
 * methods for each domain event in the Flash Food platform.
 *
 * <h3>Channel overview</h3>
 * <ul>
 *   <li>{@link #publishNotification} → {@value RabbitMQConfig#NOTIFICATION_EXCHANGE}
 *       with key {@value RabbitMQConfig#NOTIFICATION_ROUTING_KEY}</li>
 *   <li>{@link #publishOrderEvent} → {@value RabbitMQConfig#ORDER_EXCHANGE}
 *       with key derived from the event's {@link OrderEventMessage#getStatus()}</li>
 *   <li>{@link #publishFlashSaleEvent} → {@value RabbitMQConfig#FLASH_SALE_EXCHANGE}
 *       with key {@value RabbitMQConfig#FLASH_SALE_ROUTING_KEY}</li>
 * </ul>
 *
 * <p>All methods catch and log broker errors without re-throwing so that a
 * temporary outage never rolls back the calling transaction.  The caller
 * remains responsible for ensuring the DB commit happens before this method
 * is invoked (i.e. call from inside {@code @Transactional} methods — Spring
 * AMQP will use a separate connection, not the same JDBC transaction).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    // =========================================================================
    // Notification
    // =========================================================================

    /**
     * Routes a pre-built notification to specific user(s) via the notification
     * exchange.  Typically called by {@link OrderEventConsumer} after it has
     * determined which party to notify.
     */
    public void publishNotification(NotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    message);
            log.debug("Published notification: type={}, userIds={}",
                    message.getType(), message.getUserIds());
        } catch (Exception e) {
            log.error("Failed to publish notification (type={}, userIds={}): {}",
                    message.getType(), message.getUserIds(), e.getMessage(), e);
        }
    }

    // =========================================================================
    // Order events
    // =========================================================================

    /**
     * Emits an order lifecycle event to the order exchange.
     *
     * <p>The routing key is derived from the event's status so that interested
     * consumers can subscribe to specific transitions (e.g. {@code order.created}).
     */
    public void publishOrderEvent(OrderEventMessage event) {
        String routingKey = resolveOrderRoutingKey(event.getStatus());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    routingKey,
                    event);
            log.debug("Published order event: orderNumber={}, status={}, routingKey={}",
                    event.getOrderNumber(), event.getStatus(), routingKey);
        } catch (Exception e) {
            log.error("Failed to publish order event (orderNumber={}, status={}): {}",
                    event.getOrderNumber(), event.getStatus(), e.getMessage(), e);
        }
    }

    // =========================================================================
    // Flash-sale events
    // =========================================================================

    /**
     * Publishes a geo-targeted flash-sale activation event.  The consumer
     * performs a Redis Geo radius search to determine which nearby users to
     * notify.
     */
    public void publishFlashSaleEvent(FlashSaleMessage event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.FLASH_SALE_EXCHANGE,
                    RabbitMQConfig.FLASH_SALE_ROUTING_KEY,
                    event);
            log.debug("Published flash-sale event: foodItemId={}, store={}",
                    event.getFoodItemId(), event.getStoreName());
        } catch (Exception e) {
            log.error("Failed to publish flash-sale event (foodItemId={}, storeId={}): {}",
                    event.getFoodItemId(), event.getStoreId(), e.getMessage(), e);
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private String resolveOrderRoutingKey(OrderStatus status) {
        if (status == null) return "order.unknown";
        return switch (status) {
            case PENDING   -> RabbitMQConfig.ORDER_CREATED_ROUTING_KEY;
            case CONFIRMED -> RabbitMQConfig.ORDER_CONFIRMED_ROUTING_KEY;
            case PREPARING -> RabbitMQConfig.ORDER_PREPARING_ROUTING_KEY;
            case READY     -> RabbitMQConfig.ORDER_READY_ROUTING_KEY;
            case COMPLETED -> RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY;
            case CANCELLED -> RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY;
            default        -> "order." + status.getDisplayName();
        };
    }
}
