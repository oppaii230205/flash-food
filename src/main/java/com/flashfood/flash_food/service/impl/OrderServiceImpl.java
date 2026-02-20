package com.flashfood.flash_food.service.impl;

import com.flashfood.flash_food.dto.request.CreateOrderRequest;
import com.flashfood.flash_food.dto.response.OrderResponse;
import com.flashfood.flash_food.entity.*;
import com.flashfood.flash_food.exception.*;
import com.flashfood.flash_food.util.EntityMapper;
import com.flashfood.flash_food.repository.*;
import com.flashfood.flash_food.service.AuthenticationService;
import com.flashfood.flash_food.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService
 * Handles order lifecycle with inventory management and payment processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final StoreRepository storeRepository;
    private final PaymentRepository paymentRepository;
    private final AuthenticationService authenticationService;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for store ID: {}", request.getStoreId());

        User currentUser = authenticationService.getCurrentUser();

        // Validate store
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + request.getStoreId()));

        // Validate store is active
        if (store.getStatus() != StoreStatus.ACTIVE) {
            throw new InvalidOperationException("Store is not currently accepting orders");
        }

        // Parse payment method
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.fromDisplayName(request.getPaymentMethod());
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid payment method: " + request.getPaymentMethod());
        }

        // Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(currentUser);
        order.setStore(store);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod);
        order.setPickupTime(request.getPickupTime());
        order.setSpecialInstructions(request.getSpecialInstructions());

        // Process order items with pessimistic locking
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            // Lock food item to prevent overselling
            FoodItem foodItem = foodItemRepository.findByIdWithLock(itemRequest.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + itemRequest.getFoodItemId()));

            // Validate food item belongs to the same store
            if (!foodItem.getStore().getId().equals(store.getId())) {
                throw new InvalidOperationException("All items must be from the same store");
            }

            // Validate food item is available
            if (foodItem.getStatus() != FoodItemStatus.AVAILABLE) {
                throw new InvalidOperationException("Food item '" + foodItem.getName() + "' is not available");
            }

            // Check if flash sale is active
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(foodItem.getSaleStartTime()) || now.isAfter(foodItem.getSaleEndTime())) {
                throw new InvalidOperationException("Flash sale for '" + foodItem.getName() + "' is not active");
            }

            // Check stock availability
            if (foodItem.getAvailableQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for " + foodItem.getName() + 
                        ". Available: " + foodItem.getAvailableQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            // Decrement quantity using atomic update
            int updated = foodItemRepository.decrementQuantity(foodItem.getId(), itemRequest.getQuantity());
            if (updated == 0) {
                throw new InsufficientStockException("Failed to reserve stock for " + foodItem.getName());
            }

            // Refresh entity to get updated quantity
            foodItemRepository.flush();
            foodItem = foodItemRepository.findById(foodItem.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food item not found"));

            // Update food item status if out of stock
            if (foodItem.getAvailableQuantity() == 0) {
                foodItem.setStatus(FoodItemStatus.OUT_OF_STOCK);
                foodItemRepository.save(foodItem);
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFoodItem(foodItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(foodItem.getFlashPrice());
            
            BigDecimal itemTotal = foodItem.getFlashPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setSubtotal(itemTotal);
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        log.info("Order created successfully with order number: {}", savedOrder.getOrderNumber());

        return entityMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Can only cancel pending orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOperationException("Only pending orders can be cancelled");
        }

        // Restore stock for each item
        for (OrderItem item : order.getOrderItems()) {
            FoodItem foodItem = item.getFoodItem();
            int newQuantity = foodItem.getAvailableQuantity() + item.getQuantity();
            foodItem.setAvailableQuantity(newQuantity);
            
            // Update status back to available if was out of stock
            if (foodItem.getStatus() == FoodItemStatus.OUT_OF_STOCK && newQuantity > 0) {
                foodItem.setStatus(FoodItemStatus.AVAILABLE);
            }
            
            foodItemRepository.save(foodItem);
        }

        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        // Update payment status
        Payment payment = paymentRepository.findByOrder(order)
                .orElse(null);
        if (payment != null) {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
        }

        log.info("Order cancelled successfully: {}", order.getOrderNumber());

        return entityMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        log.info("Confirming order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Can only confirm pending orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOperationException("Only pending orders can be confirmed");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order confirmed successfully: {}", order.getOrderNumber());

        return entityMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse startPreparingOrder(Long orderId) {
        log.info("Starting to prepare order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Can only prepare confirmed orders
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOperationException("Only confirmed orders can be prepared");
        }

        order.setStatus(OrderStatus.PREPARING);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order preparation started: {}", order.getOrderNumber());

        return entityMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse markOrderReady(Long orderId) {
        log.info("Marking order as ready with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Can only mark preparing orders as ready
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new InvalidOperationException("Only preparing orders can be marked as ready");
        }

        order.setStatus(OrderStatus.READY);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order marked as ready: {}", order.getOrderNumber());

        return entityMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        log.info("Completing order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Can only complete ready orders
        if (order.getStatus() != OrderStatus.READY) {
            throw new InvalidOperationException("Only ready orders can be completed");
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order completed successfully: {}", order.getOrderNumber());

        return entityMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse processPayment(Long orderId) {
        log.info("Processing payment for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        // In real scenario, integrate with payment gateway here
        // For now, just mark as completed
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Payment processed successfully for order: {}", order.getOrderNumber());

        return entityMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponse findById(Long orderId) {
        log.debug("Finding order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        return entityMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponse findByOrderNumber(String orderNumber) {
        log.debug("Finding order with order number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));

        return entityMapper.toOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> findMyOrders(Pageable pageable) {
        log.debug("Finding orders for current user");

        User currentUser = authenticationService.getCurrentUser();
        Page<Order> orders = orderRepository.findByUser(currentUser, pageable);

        return orders.map(entityMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> findMyOrdersByStatus(String status, Pageable pageable) {
        log.debug("Finding orders for current user with status: {}", status);

        User currentUser = authenticationService.getCurrentUser();

        // Parse status
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid order status: " + status);
        }

        List<Order> orders = orderRepository.findByUserIdAndStatus(currentUser.getId(), orderStatus);
        
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), orders.size());
        List<Order> pageContent = orders.subList(start, end);
        
        Page<Order> page = new PageImpl<>(pageContent, pageable, orders.size());
        return page.map(entityMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> findStoreOrders(Long storeId, Pageable pageable) {
        log.debug("Finding orders for store ID: {}", storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

        // Filter by store
        Page<Order> orders = orderRepository.findAll(pageable)
                .map(order -> order.getStore().equals(store) ? order : null);

        return orders.map(entityMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> findStoreOrdersByStatus(Long storeId, String status, Pageable pageable) {
        log.debug("Finding orders for store ID: {} with status: {}", storeId, status);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

        // Parse status
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid order status: " + status);
        }

        List<Order> orders = orderRepository.findByStoreIdAndStatus(storeId, orderStatus);
        
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), orders.size());
        List<Order> pageContent = orders.subList(start, end);
        
        Page<Order> page = new PageImpl<>(pageContent, pageable, orders.size());
        return page.map(entityMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> findAllOrders(Pageable pageable) {
        log.debug("Finding all orders (admin)");

        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(entityMapper::toOrderResponse);
    }

    // ===== Helper Methods =====

    /**
     * Generate unique order number
     * Format: ORD-YYYYMMDD-XXXXXX
     */
    private String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + dateStr + "-" + randomStr;
    }
}
