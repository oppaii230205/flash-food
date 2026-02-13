package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.Order;
import com.flashfood.flash_food.entity.OrderStatus;
import com.flashfood.flash_food.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByUser(User user);
    
    Page<Order> findByUser(User user, Pageable pageable);
    
    List<Order> findByStatus(OrderStatus status);
    
    @Query("""
        SELECT o FROM Order o 
        WHERE o.user.id = :userId 
        AND o.status = :status
    """)
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);
    
    @Query("""
        SELECT o FROM Order o 
        WHERE o.store.id = :storeId 
        AND o.status = :status
    """)
    List<Order> findByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") OrderStatus status);
    
    /**
     * Find orders that need to be auto-expired
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE o.status IN ('READY', 'PREPARING') 
        AND o.pickupTime < :expiryTime
    """)
    List<Order> findOrdersToExpire(@Param("expiryTime") LocalDateTime expiryTime);
    
    /**
     * Get order statistics for a store
     */
    @Query("""
        SELECT COUNT(o) FROM Order o 
        WHERE o.store.id = :storeId 
        AND o.status = 'COMPLETED'
    """)
    Long countCompletedOrdersByStore(@Param("storeId") Long storeId);
}
