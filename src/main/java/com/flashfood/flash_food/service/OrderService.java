package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.request.CreateOrderRequest;
import com.flashfood.flash_food.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Order operations.
 * Handles the full order lifecycle: placement, payment, status progression, and cancellation.
 */
public interface OrderService {

    /**
     * Place a new order for the currently authenticated customer.
     *
     * @param request order data including store, items, and payment method
     * @return the created order
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Cancel an order.
     * Customers may cancel their own PENDING orders.
     * Store owners and admins may cancel PENDING or CONFIRMED orders for their store.
     *
     * @param orderId order identifier
     * @param reason  optional human-readable cancellation reason
     * @return the cancelled order
     */
    OrderResponse cancelOrder(Long orderId, String reason);

    /**
     * Confirm a pending order (store owner / admin).
     * Transitions: PENDING → CONFIRMED.
     *
     * @param orderId order identifier
     * @return the confirmed order
     */
    OrderResponse confirmOrder(Long orderId);

    /**
     * Mark an order as being actively prepared (store owner / admin).
     * Transitions: CONFIRMED → PREPARING.
     *
     * @param orderId order identifier
     * @return the updated order
     */
    OrderResponse startPreparingOrder(Long orderId);

    /**
     * Mark an order as ready for customer pickup (store owner / admin).
     * Transitions: PREPARING → READY.
     *
     * @param orderId order identifier
     * @return the updated order
     */
    OrderResponse markOrderReady(Long orderId);

    /**
     * Mark an order as completed after the customer has picked it up (store owner / admin).
     * Transitions: READY → COMPLETED.
     *
     * @param orderId order identifier
     * @return the completed order
     */
    OrderResponse completeOrder(Long orderId);

    /**
     * Simulate payment processing for a PENDING order.
     * Updates both the {@link com.flashfood.flash_food.entity.Payment} record and the
     * {@code paymentStatus} field on the order itself.
     *
     * @param orderId order identifier
     * @return order with updated payment status
     */
    OrderResponse processPayment(Long orderId);

    /**
     * Retrieve an order by its database ID.
     * Access is restricted to the order's customer, the owning store's staff, or admins.
     *
     * @param orderId order identifier
     * @return order details
     */
    OrderResponse findById(Long orderId);

    /**
     * Retrieve an order by its human-readable order number (e.g. {@code ORD-20260228-AB12CD}).
     *
     * @param orderNumber order number string
     * @return order details
     */
    OrderResponse findByOrderNumber(String orderNumber);

    /**
     * List all orders belonging to the currently authenticated customer.
     *
     * @param pageable pagination parameters
     * @return paged list of orders
     */
    Page<OrderResponse> findMyOrders(Pageable pageable);

    /**
     * List the current customer's orders filtered by status.
     *
     * @param status   order status as a display-name string (e.g. {@code "pending"})
     * @param pageable pagination parameters
     * @return paged list of matching orders
     */
    Page<OrderResponse> findMyOrdersByStatus(String status, Pageable pageable);

    /**
     * List all orders for a specific store (store owner / admin only).
     * The caller must own the store or be an admin.
     *
     * @param storeId  store identifier
     * @param pageable pagination parameters
     * @return paged list of store orders
     */
    Page<OrderResponse> findStoreOrders(Long storeId, Pageable pageable);

    /**
     * List a store's orders filtered by status (store owner / admin only).
     *
     * @param storeId  store identifier
     * @param status   order status as a display-name string
     * @param pageable pagination parameters
     * @return paged list of matching orders
     */
    Page<OrderResponse> findStoreOrdersByStatus(Long storeId, String status, Pageable pageable);

    /**
     * List all orders in the system (admin only).
     *
     * @param pageable pagination parameters
     * @return paged list of all orders
     */
    Page<OrderResponse> findAllOrders(Pageable pageable);
}

