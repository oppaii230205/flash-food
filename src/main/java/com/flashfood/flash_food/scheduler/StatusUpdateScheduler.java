package com.flashfood.flash_food.scheduler;

import com.flashfood.flash_food.entity.FoodItem;
import com.flashfood.flash_food.entity.FoodItemStatus;
import com.flashfood.flash_food.entity.Order;
import com.flashfood.flash_food.entity.OrderStatus;
import com.flashfood.flash_food.repository.FoodItemRepository;
import com.flashfood.flash_food.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks for automatic status updates
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatusUpdateScheduler {
    
    private final FoodItemRepository foodItemRepository;
    private final OrderRepository orderRepository;
    
    /**
     * Run every 5 minutes to mark expired food items
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void markExpiredFoodItems() {
        log.info("Running scheduled task: markExpiredFoodItems");
        
        LocalDateTime now = LocalDateTime.now();
        List<FoodItem> expiredItems = foodItemRepository.findExpiredItems(now);
        
        for (FoodItem item : expiredItems) {
            item.setIsExpired(true);
            item.setStatus(FoodItemStatus.EXPIRED);
            foodItemRepository.save(item);
            log.info("Marked food item {} as expired", item.getId());
        }
        
        if (!expiredItems.isEmpty()) {
            log.info("Marked {} food items as expired", expiredItems.size());
        }
    }
    
    /**
     * Run every 10 minutes to expire unclaimed orders
     * Orders that are ready but not picked up within 2 hours will be expired
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void expireUnclaimedOrders() {
        log.info("Running scheduled task: expireUnclaimedOrders");
        
        LocalDateTime expiryTime = LocalDateTime.now().minusHours(2);
        List<Order> ordersToExpire = orderRepository.findOrdersToExpire(expiryTime);
        
        for (Order order : ordersToExpire) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
            log.info("Expired order {} that was not picked up", order.getOrderNumber());
        }
        
        if (!ordersToExpire.isEmpty()) {
            log.info("Expired {} unclaimed orders", ordersToExpire.size());
        }
    }
    
    /**
     * Run daily at 2 AM to clean up old notifications (older than 30 days)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldNotifications() {
        log.info("Running scheduled task: cleanupOldNotifications");
        
        // This will be implemented in NotificationRepository
        // For now, just log
        log.info("Notification cleanup completed");
    }
}
