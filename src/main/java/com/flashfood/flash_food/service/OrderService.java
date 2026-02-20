package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.request.CreateOrderRequest;
import com.flashfood.flash_food.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Order operations
 * Handles order placement, payment, and order lifecycle management
 */
public interface OrderService {
    
    /**
     * Create a new order
     * @param request Order creation data
     * @return Created order
     */
    OrderResponse createOrder(CreateOrderRequest request);
    
    /**
     * Cancel an order
     * @param orderId Order ID
     * @return Cancelled order
     */
    OrderResponse cancelOrder(Long orderId);
    
    /**
     * Confirm order (store owner confirms received order)
     * @param orderId Order ID
     * @return Confirmed order
     */
    OrderResponse confirmOrder(Long orderId);
    
    /**
     * Mark order as preparing
     * @param orderId Order ID
     * @return Updated order
     */
    OrderResponse startPreparingOrder(Long orderId);
    
    /**
     * Mark order as ready for pickup
     * @param orderId Order ID
     * @return Updated order
     */
    OrderResponse markOrderReady(Long orderId);
    
    /**
     * Mark order as completed (customer picked up)
     * @param orderId Order ID
     * @return Completed order
     */
    OrderResponse completeOrder(Long orderId);
    
    /**
     * Process payment for an order
     * @param orderId Order ID
     * @return Order with payment processed
     */
    OrderResponse processPayment(Long orderId);
    
    /**
     * Find order by ID
     * @param orderId Order ID
     * @return Order details
     */
    OrderResponse findById(Long orderId);
    
    /**
     * Find order by order number
     * @param orderNumber Order number
     * @return Order details
     */
    OrderResponse findByOrderNumber(String orderNumber);
    
    /**
     * Find all orders for current user
     * @param pageable Pagination parameters
     * @return Page of user's orders
     */
    Page<OrderResponse> findMyOrders(Pageable pageable);
    
    /**
     * Find orders by status for current user
     * @param status Order status as string
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    Page<OrderResponse> findMyOrdersByStatus(String status, Pageable pageable);
    
    /**
     * Find all orders for a store (store owner only)
     * @param storeId Store ID
     * @param pageable Pagination parameters
     * @return Page of store's orders
     */
    Page<OrderResponse> findStoreOrders(Long storeId, Pageable pageable);
    
    /**
     * Find orders by status for a store
     * @param storeId Store ID
     * @param status Order status as string
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    Page<OrderResponse> findStoreOrdersByStatus(Long storeId, String status, Pageable pageable);
    
    /**
     * Find all orders (admin only)
     * @param pageable Pagination parameters
     * @return Page of all orders
     */
    Page<OrderResponse> findAllOrders(Pageable pageable);
}
