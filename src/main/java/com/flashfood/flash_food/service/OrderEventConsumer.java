package com.flashfood.flash_food.service;

import com.flashfood.flash_food.dto.message.NotificationMessage;
import com.flashfood.flash_food.dto.message.OrderEventMessage;
import com.flashfood.flash_food.config.RabbitMQConfig;
import com.flashfood.flash_food.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Consumes order lifecycle events from {@link RabbitMQConfig#ORDER_QUEUE} and
 * translates them into user-facing {@link NotificationMessage}s that are
 * forwarded to the notification exchange.
 *
 * <h3>Responsibility</h3>
 * <p>Decouples {@code OrderService} from notification logic: the service emits
 * a single domain event; this consumer decides <em>who</em> to notify and
 * <em>what</em> to say based on the transition.
 *
 * <h3>Error handling</h3>
 * <p>Exceptions are <strong>not</strong> caught here.  If an unhandled exception
 * escapes the method, the AMQP container retries up to 3 times (with
 * exponential back-off configured in {@link RabbitMQConfig}).  After all
 * retries are exhausted the message is forwarded to
 * {@link RabbitMQConfig#ORDER_DLQ} via the dead-letter exchange.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final MessagePublisher messagePublisher;

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderEvent(OrderEventMessage event) {
        log.info("Order event received: orderNumber={}, status={}",
                event.getOrderNumber(), event.getStatus());

        if (event.getStatus() == null) {
            log.warn("Received order event with null status — orderNumber={}, skipping",
                    event.getOrderNumber());
            return;
        }

        switch (event.getStatus()) {
            case PENDING    -> notifyNewOrder(event);
            case CONFIRMED  -> notifyOrderConfirmed(event);
            case PREPARING  -> notifyOrderPreparing(event);
            case READY      -> notifyOrderReady(event);
            case COMPLETED  -> notifyOrderCompleted(event);
            case CANCELLED  -> notifyOrderCancelled(event);
            default -> log.debug("No notification action for order status: {}",
                    event.getStatus().getDisplayName());
        }
    }

    // =========================================================================
    // Per-status notification builders
    // =========================================================================

    /**
     * PENDING — the customer has just placed the order; notify the store owner.
     */
    private void notifyNewOrder(OrderEventMessage event) {
        String body = "Order #" + event.getOrderNumber()
                + " — " + event.getItemCount() + " item(s), total: " + event.getTotalAmount();

        messagePublisher.publishNotification(NotificationMessage.builder()
                .userIds(List.of(event.getStoreOwnerId()))
                .title("New Order Received")
                .message(body)
                .type(NotificationType.ORDER_CONFIRMED)
                .referenceId(event.getOrderId())
                .build());

        log.debug("New-order notification published to storeOwnerId={}", event.getStoreOwnerId());
    }

    /**
     * CONFIRMED — the store confirmed the order; notify the customer.
     */
    private void notifyOrderConfirmed(OrderEventMessage event) {
        messagePublisher.publishNotification(NotificationMessage.builder()
                .userIds(List.of(event.getCustomerId()))
                .title("Order Confirmed")
                .message("Your order #" + event.getOrderNumber()
                        + " has been confirmed by the store.")
                .type(NotificationType.ORDER_CONFIRMED)
                .referenceId(event.getOrderId())
                .build());
    }

    /**
     * PREPARING — the store started preparing the order; notify the customer.
     */
    private void notifyOrderPreparing(OrderEventMessage event) {
        messagePublisher.publishNotification(NotificationMessage.builder()
                .userIds(List.of(event.getCustomerId()))
                .title("Order Being Prepared")
                .message("Your order #" + event.getOrderNumber()
                        + " is now being prepared. It will be ready shortly!")
                .type(NotificationType.ORDER_CONFIRMED)
                .referenceId(event.getOrderId())
                .build());
    }

    /**
     * READY — the order is ready for pickup; notify the customer.
     */
    private void notifyOrderReady(OrderEventMessage event) {
        messagePublisher.publishNotification(NotificationMessage.builder()
                .userIds(List.of(event.getCustomerId()))
                .title("Order Ready for Pickup")
                .message("Your order #" + event.getOrderNumber()
                        + " is ready! Please come to the store to pick it up.")
                .type(NotificationType.ORDER_READY)
                .referenceId(event.getOrderId())
                .build());
    }

    /**
     * COMPLETED — the order was picked up; no further notification needed by
     * default (can be extended, e.g. to ask for a review).
     */
    private void notifyOrderCompleted(OrderEventMessage event) {
        log.info("Order completed: orderNumber={} — no notification sent", event.getOrderNumber());
    }

    /**
     * CANCELLED — notify the party that did <em>not</em> cancel.
     * {@link OrderEventMessage#getCancelledByCustomer()} drives the logic:
     * <ul>
     *   <li>{@code true}  → customer cancelled → notify the store owner.</li>
     *   <li>{@code false} → store/admin cancelled → notify the customer.</li>
     * </ul>
     */
    private void notifyOrderCancelled(OrderEventMessage event) {
        boolean customerCancelled = Boolean.TRUE.equals(event.getCancelledByCustomer());
        Long recipientId = customerCancelled ? event.getStoreOwnerId() : event.getCustomerId();

        if (recipientId == null) {
            log.warn("Cannot determine cancellation notification recipient for orderNumber={}",
                    event.getOrderNumber());
            return;
        }

        String reason = (event.getCancellationReason() != null
                && !event.getCancellationReason().isBlank())
                ? ": " + event.getCancellationReason()
                : ".";
        String body = "Order #" + event.getOrderNumber() + " has been cancelled" + reason;

        messagePublisher.publishNotification(NotificationMessage.builder()
                .userIds(List.of(recipientId))
                .title("Order Cancelled")
                .message(body)
                .type(NotificationType.ORDER_CANCELLED)
                .referenceId(event.getOrderId())
                .build());
    }
}
