package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.OrderItem;
import com.flashfood.flash_food.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OrderItem entity.
 *
 * JPQL note: enum constants are always passed as named parameters so Hibernate
 * applies the registered {@link com.flashfood.flash_food.entity.converter.OrderStatusConverter}
 * automatically — never hard-code enum literals inside JPQL strings.
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
     * Returns the total quantity sold for a food item across all active/completed orders.
     * Only orders in CONFIRMED, PREPARING, READY, or COMPLETED states are counted.
     * Enum values are bound as parameters to ensure the OrderStatusConverter is applied.
     */
    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0)
        FROM OrderItem oi
        WHERE oi.foodItem.id = :foodItemId
        AND oi.order.status IN (:confirmed, :preparing, :ready, :completed)
    """)
    Integer getTotalQuantitySold(
            @Param("foodItemId") Long foodItemId,
            @Param("confirmed") OrderStatus confirmed,
            @Param("preparing") OrderStatus preparing,
            @Param("ready") OrderStatus ready,
            @Param("completed") OrderStatus completed);
}
