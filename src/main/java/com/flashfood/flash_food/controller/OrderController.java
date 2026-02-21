package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.CreateOrderRequest;
import com.flashfood.flash_food.dto.response.ApiResponse;
import com.flashfood.flash_food.dto.response.OrderResponse;
import com.flashfood.flash_food.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Order management
 * Handles order placement, tracking, and lifecycle management
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order
     * @param request Order creation data
     * @return Created order
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        
        log.info("POST /api/orders - Creating new order");
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    /**
     * Cancel an order
     * @param orderId Order ID
     * @return Cancelled order
     */
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId) {
        log.info("PATCH /api/orders/{}/cancel - Cancelling order", orderId);
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }

    /**
     * Confirm order (store owner)
     * @param orderId Order ID
     * @return Confirmed order
     */
    @PatchMapping("/{orderId}/confirm")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(@PathVariable Long orderId) {
        log.info("PATCH /api/orders/{}/confirm - Confirming order", orderId);
        OrderResponse response = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order confirmed successfully", response));
    }

    /**
     * Start preparing order
     * @param orderId Order ID
     * @return Updated order
     */
    @PatchMapping("/{orderId}/preparing")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> startPreparingOrder(@PathVariable Long orderId) {
        log.info("PATCH /api/orders/{}/preparing - Starting order preparation", orderId);
        OrderResponse response = orderService.startPreparingOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order preparation started", response));
    }

    /**
     * Mark order as ready
     * @param orderId Order ID
     * @return Updated order
     */
    @PatchMapping("/{orderId}/ready")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> markOrderReady(@PathVariable Long orderId) {
        log.info("PATCH /api/orders/{}/ready - Marking order as ready", orderId);
        OrderResponse response = orderService.markOrderReady(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order is ready for pickup", response));
    }

    /**
     * Complete order
     * @param orderId Order ID
     * @return Completed order
     */
    @PatchMapping("/{orderId}/complete")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(@PathVariable Long orderId) {
        log.info("PATCH /api/orders/{}/complete - Completing order", orderId);
        OrderResponse response = orderService.completeOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order completed successfully", response));
    }

    /**
     * Process payment for order
     * @param orderId Order ID
     * @return Order with payment processed
     */
    @PostMapping("/{orderId}/payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> processPayment(@PathVariable Long orderId) {
        log.info("POST /api/orders/{}/payment - Processing payment", orderId);
        OrderResponse response = orderService.processPayment(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
    }

    /**
     * Get order by ID
     * @param orderId Order ID
     * @return Order details
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {
        log.info("GET /api/orders/{} - Getting order", orderId);
        OrderResponse response = orderService.findById(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get order by order number
     * @param orderNumber Order number
     * @return Order details
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(@PathVariable String orderNumber) {
        log.info("GET /api/orders/number/{} - Getting order by number", orderNumber);
        OrderResponse response = orderService.findByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get current user's orders
     * @param pageable Pagination parameters
     * @return Page of user's orders
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/orders/my-orders - Getting current user's orders");
        Page<OrderResponse> response = orderService.findMyOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get current user's orders by status
     * @param status Order status
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    @GetMapping("/my-orders/status/{status}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrdersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/orders/my-orders/status/{} - Getting user's orders by status", status);
        Page<OrderResponse> response = orderService.findMyOrdersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get store's orders
     * @param storeId Store ID
     * @param pageable Pagination parameters
     * @return Page of store's orders
     */
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getStoreOrders(
            @PathVariable Long storeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/orders/store/{} - Getting store's orders", storeId);
        Page<OrderResponse> response = orderService.findStoreOrders(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get store's orders by status
     * @param storeId Store ID
     * @param status Order status
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    @GetMapping("/store/{storeId}/status/{status}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getStoreOrdersByStatus(
            @PathVariable Long storeId,
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/orders/store/{}/status/{} - Getting store's orders by status", storeId, status);
        Page<OrderResponse> response = orderService.findStoreOrdersByStatus(storeId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all orders (admin only)
     * @param pageable Pagination parameters
     * @return Page of all orders
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/orders/all - Getting all orders (admin)");
        Page<OrderResponse> response = orderService.findAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
