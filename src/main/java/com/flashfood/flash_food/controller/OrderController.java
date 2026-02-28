package com.flashfood.flash_food.controller;

import com.flashfood.flash_food.dto.request.CancelOrderRequest;
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
 * REST Controller for Order management.
 *
 * <p>Authorization is enforced at two levels:
 * <ol>
 *   <li>{@code @PreAuthorize} — a role-based gate at the HTTP layer.</li>
 *   <li>The service layer — ownership and fine-grained access control.</li>
 * </ol>
 *
 * <h3>Order lifecycle endpoints (HTTP verbs / paths)</h3>
 * <pre>
 *  POST   /api/v1/orders                         → place order (CUSTOMER)
 *  PATCH  /api/v1/orders/{id}/cancel             → cancel order (CUSTOMER | STORE_OWNER | ADMIN)
 *  PATCH  /api/v1/orders/{id}/confirm            → store confirms (STORE_OWNER | ADMIN)
 *  PATCH  /api/v1/orders/{id}/preparing          → mark preparing (STORE_OWNER | ADMIN)
 *  PATCH  /api/v1/orders/{id}/ready              → mark ready for pickup (STORE_OWNER | ADMIN)
 *  PATCH  /api/v1/orders/{id}/complete           → mark completed (STORE_OWNER | ADMIN)
 *  POST   /api/v1/orders/{id}/payment            → process payment (CUSTOMER)
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // =========================================================================
    // Place order
    // =========================================================================

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("POST /api/v1/orders - Creating new order");
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    // =========================================================================
    // Cancellation
    // =========================================================================

    /**
     * Cancel an order.
     *
     * <ul>
     *   <li>Customers may cancel their own <b>PENDING</b> orders.</li>
     *   <li>Store owners / admins may cancel <b>PENDING</b> or <b>CONFIRMED</b> orders
     *       belonging to their store.</li>
     * </ul>
     *
     * The request body is optional; when provided, {@code reason} is stored on the order.
     */
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request) {

        log.info("PATCH /api/v1/orders/{}/cancel - Cancelling order", orderId);
        String reason = request != null ? request.getReason() : null;
        OrderResponse response = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }

    // =========================================================================
    // Store-side status transitions
    // =========================================================================

    @PatchMapping("/{orderId}/confirm")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(@PathVariable Long orderId) {
        log.info("PATCH /api/v1/orders/{}/confirm - Confirming order", orderId);
        OrderResponse response = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order confirmed successfully", response));
    }

    @PatchMapping("/{orderId}/preparing")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> startPreparingOrder(@PathVariable Long orderId) {
        log.info("PATCH /api/v1/orders/{}/preparing - Starting order preparation", orderId);
        OrderResponse response = orderService.startPreparingOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order preparation started", response));
    }

    @PatchMapping("/{orderId}/ready")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> markOrderReady(@PathVariable Long orderId) {
        log.info("PATCH /api/v1/orders/{}/ready - Marking order as ready", orderId);
        OrderResponse response = orderService.markOrderReady(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order is ready for pickup", response));
    }

    @PatchMapping("/{orderId}/complete")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(@PathVariable Long orderId) {
        log.info("PATCH /api/v1/orders/{}/complete - Completing order", orderId);
        OrderResponse response = orderService.completeOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order completed successfully", response));
    }

    // =========================================================================
    // Payment
    // =========================================================================

    @PostMapping("/{orderId}/payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> processPayment(@PathVariable Long orderId) {
        log.info("POST /api/v1/orders/{}/payment - Processing payment", orderId);
        OrderResponse response = orderService.processPayment(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
    }

    // =========================================================================
    // Query endpoints
    // =========================================================================

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {
        log.info("GET /api/v1/orders/{} - Getting order", orderId);
        OrderResponse response = orderService.findById(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber) {
        log.info("GET /api/v1/orders/number/{} - Getting order by number", orderNumber);
        OrderResponse response = orderService.findByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("GET /api/v1/orders/my-orders - Getting current user's orders");
        Page<OrderResponse> response = orderService.findMyOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-orders/status/{status}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrdersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("GET /api/v1/orders/my-orders/status/{} - Getting user's orders by status", status);
        Page<OrderResponse> response = orderService.findMyOrdersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getStoreOrders(
            @PathVariable Long storeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("GET /api/v1/orders/store/{} - Getting store's orders", storeId);
        Page<OrderResponse> response = orderService.findStoreOrders(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}/status/{status}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getStoreOrdersByStatus(
            @PathVariable Long storeId,
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("GET /api/v1/orders/store/{}/status/{} - Getting store's orders by status",
                storeId, status);
        Page<OrderResponse> response = orderService.findStoreOrdersByStatus(storeId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("GET /api/v1/orders/all - Getting all orders (admin)");
        Page<OrderResponse> response = orderService.findAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
