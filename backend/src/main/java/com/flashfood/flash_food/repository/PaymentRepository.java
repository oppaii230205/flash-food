package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.Order;
import com.flashfood.flash_food.entity.Payment;
import com.flashfood.flash_food.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find the payment record associated with a given order.
     */
    Optional<Payment> findByOrder(Order order);

    /**
     * Find payment by order ID.
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Find payment by external transaction ID.
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Check whether a payment exists for an order.
     */
    boolean existsByOrder(Order order);

    /**
     * Find all payments with a given status.
     */
    java.util.List<Payment> findByStatus(PaymentStatus status);
}
