package com.flashfood.flash_food.service;

import com.flashfood.flash_food.config.RabbitMQConfig;
import com.flashfood.flash_food.dto.message.FlashSaleMessage;
import com.flashfood.flash_food.dto.message.NotificationMessage;
import com.flashfood.flash_food.entity.Notification;
import com.flashfood.flash_food.entity.NotificationType;
import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.repository.NotificationRepository;
import com.flashfood.flash_food.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Persists inbound notification messages to the database.
 *
 * <h3>Queues consumed</h3>
 * <ul>
 *   <li>{@link RabbitMQConfig#NOTIFICATION_QUEUE} — direct notifications targeting
 *       specific user IDs.  Published by {@link OrderEventConsumer} (and any
 *       future producer).</li>
 *   <li>{@link RabbitMQConfig#FLASH_SALE_QUEUE} — geo-targeted flash-sale activation
 *       events.  The consumer performs a Redis Geo radius search to resolve the
 *       set of nearby users before persisting their notifications.</li>
 * </ul>
 *
 * <h3>Error handling</h3>
 * <p>Exceptions are intentionally <strong>not caught</strong> at the listener
 * boundary.  Any failure propagates to the AMQP container, which retries the
 * delivery up to 3 times (exponential back-off).  After all attempts fail the
 * message is forwarded to the dead-letter exchange / DLQ configured in
 * {@link RabbitMQConfig}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;
    private final RedisGeoService        redisGeoService;

    // =========================================================================
    // Direct notifications  (notification.queue)
    // =========================================================================

    /**
     * Persists one {@link Notification} row per user in
     * {@link NotificationMessage#getUserIds()}.
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    @Transactional
    public void handleNotification(NotificationMessage message) {
        log.info("Notification received: type={}, userIds={}",
                message.getType(), message.getUserIds());

        if (message.getUserIds() == null || message.getUserIds().isEmpty()) {
            log.warn("Notification message has no target users — skipping");
            return;
        }

        List<User> users = userRepository.findAllById(message.getUserIds());
        if (users.isEmpty()) {
            log.warn("None of the target userIds resolved to existing users: {}",
                    message.getUserIds());
            return;
        }

        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(message.getTitle())
                        .message(message.getMessage())
                        .type(message.getType())
                        .referenceId(message.getReferenceId())
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
        log.info("Persisted {} notification(s): type={}", notifications.size(), message.getType());
    }

    // =========================================================================
    // Flash-sale notifications  (flash-sale.queue)
    // =========================================================================

    /**
     * Handles a geo-targeted flash-sale activation event.
     *
     * <ol>
     *   <li>Queries Redis Geo for users within {@link FlashSaleMessage#getRadiusKm()} km
     *       of the store.</li>
     *   <li>Persists a {@link NotificationType#NEW_FLASH_SALE} notification for every
     *       matching user.</li>
     * </ol>
     */
    @RabbitListener(queues = RabbitMQConfig.FLASH_SALE_QUEUE)
    @Transactional
    public void handleFlashSale(FlashSaleMessage event) {
        log.info("Flash-sale event received: foodItemId={}, store='{}', radius={}km",
                event.getFoodItemId(), event.getStoreName(), event.getRadiusKm());

        List<Long> nearbyUserIds = resolveNearbyUsers(event);
        if (nearbyUserIds.isEmpty()) {
            log.info("No nearby users found for flash-sale event (foodItemId={})",
                    event.getFoodItemId());
            return;
        }

        String title   = "Flash Sale at " + event.getStoreName() + "!";
        String body    = buildFlashSaleBody(event);

        List<User> users = userRepository.findAllById(nearbyUserIds);
        if (users.isEmpty()) {
            log.warn("None of the nearby userIds resolved to existing users (foodItemId={})",
                    event.getFoodItemId());
            return;
        }

        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(title)
                        .message(body)
                        .type(NotificationType.NEW_FLASH_SALE)
                        .referenceId(event.getFoodItemId())
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
        log.info("Persisted {} flash-sale notification(s) for foodItemId={}",
                notifications.size(), event.getFoodItemId());
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private List<Long> resolveNearbyUsers(FlashSaleMessage event) {
        if (event.getStoreLongitude() == null || event.getStoreLatitude() == null
                || event.getRadiusKm() == null) {
            log.warn("Flash-sale event missing geo data (foodItemId={}) — skipping geo lookup",
                    event.getFoodItemId());
            return Collections.emptyList();
        }
        return redisGeoService.findNearbyUsers(
                event.getStoreLongitude(), event.getStoreLatitude(), event.getRadiusKm());
    }

    private String buildFlashSaleBody(FlashSaleMessage event) {
        return String.format(
                "%s is now available at %s for %,d VND (originally %,d VND — %d%% off!). Hurry, limited stock!",
                event.getItemName(),
                event.getStoreName(),
                event.getFlashPrice(),
                event.getOriginalPrice(),
                event.getDiscountPercentage());
    }
}
