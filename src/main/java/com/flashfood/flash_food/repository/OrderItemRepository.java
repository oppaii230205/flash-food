package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.OrderItem;
import com.flashfood.flash_food.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OrderItem entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    @Query("""
        SELECT oi FROM OrderItem oi 
        WHERE oi.foodItem.id = :foodItemId
    """)
    List<OrderItem> findByFoodItemId(@Param("foodItemId") Long foodItemId);
    
    /**
     * Get total quantity sold for a food item
     */
    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0) 
        FROM OrderItem oi 
        WHERE oi.foodItem.id = :foodItemId 
        AND oi.order.status IN (OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED)
    """)
    Integer getTotalQuantitySold(@Param("foodItemId") Long foodItemId);
}
