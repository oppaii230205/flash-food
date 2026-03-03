package com.flashfood.flash_food.dto.message;

import com.flashfood.flash_food.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Message published to the order exchange for every order lifecycle transition.
 *
 * <p>The routing key follows the pattern {@code order.<status>} (e.g.
 * {@code order.created}, {@code order.confirmed}).  The
 * {@link com.flashfood.flash_food.service.OrderEventConsumer} listens on
 * {@code order.#} and decides which parties to notify based on the
 * {@link #status} field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventMessage {

    private Long        orderId;
    private String      orderNumber;
    private OrderStatus status;

    /** ID of the customer who placed the order. */
    private Long customerId;
    /** ID of the store the order belongs to. */
    private Long storeId;
    /** ID of the store's owner — used to notify the right person. */
    private Long storeOwnerId;

    /** Total flash-price of all items (smallest currency unit). */
    private Integer totalAmount;
    /** Number of distinct items in the order. */
    private Integer itemCount;

    /** Populated only for CANCELLED events. */
    private String cancellationReason;

    /**
     * Populated only for CANCELLED events.
     * {@code true}  → the customer initiated the cancellation (notify the store owner).
     * {@code false} → the store / admin initiated the cancellation (notify the customer).
     */
    private Boolean cancelledByCustomer;

    /** Wall-clock time at which the status transition occurred. */
    private LocalDateTime eventTime;
}
