package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.CreateOrderRequest;
import com.flashfood.flash_food.dto.message.NotificationMessage;
import com.flashfood.flash_food.dto.response.OrderResponse;
import com.flashfood.flash_food.entity.*;
import com.flashfood.flash_food.exception.AccessDeniedException;
import com.flashfood.flash_food.exception.InsufficientStockException;
import com.flashfood.flash_food.exception.InvalidOperationException;
import com.flashfood.flash_food.exception.ResourceNotFoundException;
import com.flashfood.flash_food.repository.*;
import com.flashfood.flash_food.service.AuthenticationService;
import com.flashfood.flash_food.service.MessagePublisher;
import com.flashfood.flash_food.service.OrderService;
import com.flashfood.flash_food.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link OrderService}.
 *
 * <h3>Key design decisions</h3>
 * <ul>
 *   <li><b>Inventory safety</b>: stock is decremented via an atomic
 *       {@code UPDATE … WHERE availableQuantity >= :qty} guarded by a pessimistic write-lock
 *       on the food-item row to prevent overselling under high concurrency.</li>
 *   <li><b>Status transitions</b>: driven by a strict state machine ({@link OrderStatus});
 *       any out-of-order transition raises {@link InvalidOperationException}.</li>
 *   <li><b>Authorization</b>: every mutating method re-validates ownership/role at the service
 *       layer — the {@code @PreAuthorize} annotations on the controller are a first defence only.</li>
 *   <li><b>Notification</b>: order events are published asynchronously to RabbitMQ via
 *       {@link MessagePublisher} so the HTTP response is never delayed by downstream consumers.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository       orderRepository;
    private final FoodItemRepository    foodItemRepository;
    private final StoreRepository       storeRepository;
    private final PaymentRepository     paymentRepository;
    private final AuthenticationService authenticationService;
    private final MessagePublisher      messagePublisher;
    private final EntityMapper          entityMapper;

    // =========================================================================
    // Place order
    // =========================================================================

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for store id={}", request.getStoreId());

        User currentUser = authenticationService.getCurrentUser();

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found with ID: " + request.getStoreId()));

        if (store.getStatus() != StoreStatus.ACTIVE) {
            throw new InvalidOperationException("Store is not currently accepting orders");
        }

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.fromDisplayName(request.getPaymentMethod());
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid payment method: " + request.getPaymentMethod());
        }

        // Build the order shell — items are appended to the managed collection below
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(currentUser)
                .store(store)
                .status(OrderStatus.PENDING)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PENDING)
                .pickupTime(request.getPickupTime())
                .specialInstructions(request.getSpecialInstructions())
                .build();

        int totalAmount    = 0;
        int originalAmount = 0;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            // Acquire a pessimistic write-lock so no other transaction can modify
            // availableQuantity between our stock check and the decrement below.
            FoodItem foodItem = foodItemRepository.findByIdWithLock(itemRequest.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Food item not found with ID: " + itemRequest.getFoodItemId()));

            validateOrderItem(foodItem, store, itemRequest.getQuantity());

            // Atomic decrement — returns 0 rows if stock is insufficient
            int decremented = foodItemRepository.decrementQuantity(
                    foodItem.getId(), itemRequest.getQuantity());
            if (decremented == 0) {
                throw new InsufficientStockException(
                        "Failed to reserve stock for '" + foodItem.getName() + "'. "
                        + "Available: " + foodItem.getAvailableQuantity()
                        + ", Requested: " + itemRequest.getQuantity());
            }

            // Conditionally mark the item as SOLD_OUT when all stock is now depleted.
            // We know the pre-lock quantity from the locked entity; no flush/re-fetch needed.
            if (foodItem.getAvailableQuantity() - itemRequest.getQuantity() == 0) {
                foodItemRepository.markSoldOutIfEmpty(foodItem.getId(), FoodItemStatus.SOLD_OUT);
            }

            int itemFlashTotal    = foodItem.getFlashPrice()    * itemRequest.getQuantity();
            int itemOriginalTotal = foodItem.getOriginalPrice() * itemRequest.getQuantity();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .foodItem(foodItem)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(foodItem.getFlashPrice())
                    .totalPrice(itemFlashTotal)
                    .build();

            order.getOrderItems().add(orderItem);
            totalAmount    += itemFlashTotal;
            originalAmount += itemOriginalTotal;
        }

        order.setTotalAmount(totalAmount);
        order.setOriginalAmount(originalAmount);

        // CascadeType.ALL on Order.orderItems persists OrderItem rows automatically
        Order savedOrder = orderRepository.save(order);

        // Create the associated payment record
        paymentRepository.save(Payment.builder()
                .order(savedOrder)
                .amount(totalAmount)
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.PENDING)
                .build());

        // Notify the store owner of the new incoming order
        notifyUsers(
                List.of(store.getOwner().getId()),
                "New Order Received",
                "Order #" + savedOrder.getOrderNumber() + " — "
                        + request.getItems().size() + " item(s), total: " + totalAmount,
                NotificationType.ORDER_CONFIRMED,
                savedOrder.getId());

        log.info("Order created: id={}, number={}", savedOrder.getId(), savedOrder.getOrderNumber());
        return entityMapper.toOrderResponse(savedOrder);
    }

    // =========================================================================
    // Cancellation
    // =========================================================================

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = findOrderOrThrow(orderId);
        User currentUser = authenticationService.getCurrentUser();

        boolean isOrderOwner       = order.getUser().getId().equals(currentUser.getId());
        boolean isStoreOwnerOrAdmin = authenticationService.isStoreOwnerOrAdmin(order.getStore());

        if (!isOrderOwner && !isStoreOwnerOrAdmin) {
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        // Customers may only cancel PENDING orders (store has not confirmed yet)
        // Store owners / admins may cancel PENDING or CONFIRMED orders
        boolean canCancel = order.getStatus() == OrderStatus.PENDING
                || (isStoreOwnerOrAdmin && order.getStatus() == OrderStatus.CONFIRMED);

        if (!canCancel) {
            throw new InvalidOperationException(
                    "Order cannot be cancelled in its current state: "
                    + order.getStatus().getDisplayName());
        }

        // Restore stock for every line item atomically
        for (OrderItem item : order.getOrderItems()) {
            Long foodItemId = item.getFoodItem().getId();
            foodItemRepository.incrementQuantity(foodItemId, item.getQuantity());
            // If the item was SOLD_OUT (stock just returned), restore it to AVAILABLE
            foodItemRepository.restoreAvailableIfSoldOut(
                    foodItemId, FoodItemStatus.AVAILABLE, FoodItemStatus.SOLD_OUT);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        Order cancelled = orderRepository.save(order);

        // Sync the associated payment record
        paymentRepository.findByOrder(order).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
        });

        // Notify the opposite party about the cancellation
        Long notifyUserId = isOrderOwner
                ? order.getStore().getOwner().getId()
                : order.getUser().getId();
        String cancelMsg = "Order #" + order.getOrderNumber() + " has been cancelled"
                + (reason != null && !reason.isBlank() ? ": " + reason : ".");
        notifyUsers(List.of(notifyUserId), "Order Cancelled", cancelMsg,
                NotificationType.ORDER_CANCELLED, order.getId());

        log.info("Order cancelled: {}", order.getOrderNumber());
        return entityMapper.toOrderResponse(cancelled);
    }

    // =========================================================================
    // Store-side status transitions  (PENDING → CONFIRMED → PREPARING → READY → COMPLETED)
    // =========================================================================

    @Override
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        verifyStoreOwnerOrAdmin(order);
        requireStatus(order, OrderStatus.PENDING, "confirmed");

        order.setStatus(OrderStatus.CONFIRMED);
        Order updated = orderRepository.save(order);

        notifyUsers(
                List.of(order.getUser().getId()),
                "Order Confirmed",
                "Your order #" + order.getOrderNumber() + " has been confirmed by the store.",
                NotificationType.ORDER_CONFIRMED,
                order.getId());

        log.info("Order confirmed: {}", order.getOrderNumber());
        return entityMapper.toOrderResponse(updated);
    }

    @Override
    @Transactional
    public OrderResponse startPreparingOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        verifyStoreOwnerOrAdmin(order);
        requireStatus(order, OrderStatus.CONFIRMED, "set to preparing");

        order.setStatus(OrderStatus.PREPARING);
        Order updated = orderRepository.save(order);

        log.info("Order preparation started: {}", order.getOrderNumber());
        return entityMapper.toOrderResponse(updated);
    }

    @Override
    @Transactional
    public OrderResponse markOrderReady(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        verifyStoreOwnerOrAdmin(order);
        requireStatus(order, OrderStatus.PREPARING, "marked as ready");

        order.setStatus(OrderStatus.READY);
        Order updated = orderRepository.save(order);

        notifyUsers(
                List.of(order.getUser().getId()),
                "Order Ready for Pickup",
                "Your order #" + order.getOrderNumber()
                        + " is ready! Please come to " + order.getStore().getName() + ".",
                NotificationType.ORDER_READY,
                order.getId());

        log.info("Order marked ready: {}", order.getOrderNumber());
        return entityMapper.toOrderResponse(updated);
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        verifyStoreOwnerOrAdmin(order);
        requireStatus(order, OrderStatus.READY, "completed");

        order.setStatus(OrderStatus.COMPLETED);
        Order updated = orderRepository.save(order);

        log.info("Order completed: {}", order.getOrderNumber());
        return entityMapper.toOrderResponse(updated);
    }

    // =========================================================================
    // Payment
    // =========================================================================

    @Override
    @Transactional
    public OrderResponse processPayment(Long orderId) {
        log.info("Processing payment for order id={}", orderId);

        Order order = findOrderOrThrow(orderId);
        User currentUser = authenticationService.getCurrentUser();

        // Only the customer who placed the order or an admin may trigger payment
        if (!order.getUser().getId().equals(currentUser.getId())
                && !authenticationService.isAdmin()) {
            throw new AccessDeniedException(
                    "You do not have permission to process payment for this order");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvalidOperationException(
                    "Payment has already been processed for order: " + order.getOrderNumber());
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
            throw new InvalidOperationException(
                    "Cannot process payment for an order with status: "
                    + order.getStatus().getDisplayName());
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment record not found for order: " + order.getOrderNumber()));

        // In production, integrate with a payment gateway here and only mark PAID
        // upon a successful gateway callback. This simulates an immediate success.
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // Keep the denormalised paymentStatus on the order in sync
        order.setPaymentStatus(PaymentStatus.PAID);
        Order updated = orderRepository.save(order);

        log.info("Payment processed for order: {}", order.getOrderNumber());
        return entityMapper.toOrderResponse(updated);
    }

    // =========================================================================
    // Query operations
    // =========================================================================

    @Override
    public OrderResponse findById(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        User currentUser = authenticationService.getCurrentUser();

        boolean isOrderOwner       = order.getUser().getId().equals(currentUser.getId());
        boolean isStoreOwnerOrAdmin = authenticationService.isStoreOwnerOrAdmin(order.getStore());

        if (!isOrderOwner && !isStoreOwnerOrAdmin) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return entityMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponse findByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with number: " + orderNumber));

        User currentUser = authenticationService.getCurrentUser();
        boolean isOrderOwner       = order.getUser().getId().equals(currentUser.getId());
        boolean isStoreOwnerOrAdmin = authenticationService.isStoreOwnerOrAdmin(order.getStore());

        if (!isOrderOwner && !isStoreOwnerOrAdmin) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return entityMapper.toOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> findMyOrders(Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();
        return orderRepository.findByUser(currentUser, pageable)
                .map(entityMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> findMyOrdersByStatus(String status, Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();
        OrderStatus orderStatus = parseOrderStatus(status);
        return orderRepository.findByUserIdAndStatus(currentUser.getId(), orderStatus, pageable)
                .map(entityMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> findStoreOrders(Long storeId, Pageable pageable) {
        Store store = findStoreOrThrow(storeId);
        verifyStoreOwnerOrAdmin(store);
        return orderRepository.findByStoreId(storeId, pageable)
                .map(entityMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> findStoreOrdersByStatus(Long storeId, String status, Pageable pageable) {
        Store store = findStoreOrThrow(storeId);
        verifyStoreOwnerOrAdmin(store);
        OrderStatus orderStatus = parseOrderStatus(status);
        return orderRepository.findByStoreIdAndStatus(storeId, orderStatus, pageable)
                .map(entityMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(entityMapper::toOrderResponse);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Generates a human-readable, unique order number.
     * Format: {@code ORD-YYYYMMDD-XXXXXX} (e.g. {@code ORD-20260228-A3F9C1}).
     */
    private String generateOrderNumber() {
        String date   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + date + "-" + suffix;
    }

    /**
     * Validates that a food item can be added to the given order/store:
     * <ul>
     *   <li>belongs to the target store,</li>
     *   <li>has {@link FoodItemStatus#AVAILABLE} status,</li>
     *   <li>is within its active flash-sale window,</li>
     *   <li>has sufficient available stock.</li>
     * </ul>
     */
    private void validateOrderItem(FoodItem foodItem, Store store, int requestedQty) {
        if (!foodItem.getStore().getId().equals(store.getId())) {
            throw new InvalidOperationException(
                    "Food item '" + foodItem.getName() + "' does not belong to store '"
                    + store.getName() + "'. All items in one order must be from the same store.");
        }

        if (foodItem.getStatus() != FoodItemStatus.AVAILABLE) {
            throw new InvalidOperationException(
                    "Food item '" + foodItem.getName() + "' is not available "
                    + "(status: " + foodItem.getStatus().getDisplayName() + ")");
        }

        LocalDateTime now = LocalDateTime.now();
        if (foodItem.getSaleStartTime() == null || foodItem.getSaleEndTime() == null
                || now.isBefore(foodItem.getSaleStartTime())
                || !now.isBefore(foodItem.getSaleEndTime())) {
            throw new InvalidOperationException(
                    "Flash sale for '" + foodItem.getName() + "' is not currently active");
        }

        if (foodItem.getAvailableQuantity() < requestedQty) {
            throw new InsufficientStockException(
                    "Insufficient stock for '" + foodItem.getName() + "'. "
                    + "Available: " + foodItem.getAvailableQuantity()
                    + ", Requested: " + requestedQty);
        }
    }

    /**
     * Asserts that the order is in the {@code expected} status before performing a state transition.
     */
    private void requireStatus(Order order, OrderStatus expected, String transitionVerb) {
        if (order.getStatus() != expected) {
            throw new InvalidOperationException(
                    "Order #" + order.getOrderNumber() + " cannot be " + transitionVerb
                    + ". Current status: " + order.getStatus().getDisplayName()
                    + ", required: " + expected.getDisplayName());
        }
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId));
    }

    private Store findStoreOrThrow(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found with ID: " + storeId));
    }

    /**
     * Verifies that the current principal is the owner of the store that this order belongs to,
     * or holds the ADMIN role.
     */
    private void verifyStoreOwnerOrAdmin(Order order) {
        verifyStoreOwnerOrAdmin(order.getStore());
    }

    private void verifyStoreOwnerOrAdmin(Store store) {
        if (!authenticationService.isStoreOwnerOrAdmin(store)) {
            throw new AccessDeniedException(
                    "You do not have permission to manage orders for store '"
                    + store.getName() + "'");
        }
    }

    private OrderStatus parseOrderStatus(String status) {
        try {
            return OrderStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid order status: '" + status + "'");
        }
    }

    /**
     * Publishes a notification message to RabbitMQ.
     * Failures are swallowed and logged — notification delivery is best-effort and must
     * not roll back the enclosing order transaction.
     */
    private void notifyUsers(List<Long> userIds, String title, String message,
                             NotificationType type, Long referenceId) {
        try {
            messagePublisher.publishNotification(NotificationMessage.builder()
                    .userIds(userIds)
                    .title(title)
                    .message(message)
                    .type(type)
                    .referenceId(referenceId)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to publish order notification (orderId={}, type={}): {}",
                    referenceId, type, e.getMessage());
        }
    }
}
