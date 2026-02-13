package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.Notification;
import com.flashfood.flash_food.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    Long countByUserIdAndIsReadFalse(Long userId);
    
    List<Notification> findByType(NotificationType type);
    
    /**
     * Mark notification as read
     */
    @Modifying
    @Query("""
        UPDATE Notification n 
        SET n.isRead = true, n.readAt = :readAt 
        WHERE n.id = :id
    """)
    void markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("""
        UPDATE Notification n 
        SET n.isRead = true, n.readAt = :readAt 
        WHERE n.user.id = :userId 
        AND n.isRead = false
    """)
    void markAllAsReadForUser(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Delete old notifications
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :beforeDate")
    void deleteOldNotifications(@Param("beforeDate") LocalDateTime beforeDate);
}
