package com.flashfood.flash_food.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology and infrastructure configuration.
 *
 * <h3>Message flow</h3>
 * <pre>
 *  Producer               Exchange                    Queue
 *  ────────────────────────────────────────────────────────────────────────
 *  OrderService     ──►  order.exchange      ──►  order.queue
 *  FoodItemService  ──►  flash-sale.exchange  ──►  flash-sale.queue
 *  OrderEventConsumer──► notification.exchange──►  notification.queue
 * </pre>
 *
 * <h3>Dead-letter path</h3>
 * <p>Each main queue declares {@code x-dead-letter-exchange} and
 * {@code x-dead-letter-routing-key}.  When the Spring Retry policy
 * (configured via {@code spring.rabbitmq.listener.simple.retry.*} in
 * {@code application.properties}) exhausts all attempts, it raises
 * {@link org.springframework.amqp.rabbit.support.ListenerExecutionFailedException},
 * the container rejects the message without requeue, and the broker routes it
 * to the matching DLQ through the {@link DirectExchange} below.
 *
 * <h3>Routing key conventions</h3>
 * <ul>
 *   <li>Order events:      {@code order.<status>}  (e.g. {@code order.created})</li>
 *   <li>Notifications:     {@code notification.send}</li>
 *   <li>Flash-sale events: {@code flash-sale.notify}</li>
 * </ul>
 */
@Configuration
public class RabbitMQConfig {

    // -------------------------------------------------------------------------
    // Queue names
    // -------------------------------------------------------------------------
    public static final String NOTIFICATION_QUEUE = "flash-food.notification.queue";
    public static final String ORDER_QUEUE        = "flash-food.order.queue";
    public static final String FLASH_SALE_QUEUE   = "flash-food.flash-sale.queue";

    // Dead-letter queues
    public static final String NOTIFICATION_DLQ = "flash-food.notification.dlq";
    public static final String ORDER_DLQ        = "flash-food.order.dlq";
    public static final String FLASH_SALE_DLQ   = "flash-food.flash-sale.dlq";

    // -------------------------------------------------------------------------
    // Exchange names
    // -------------------------------------------------------------------------
    public static final String NOTIFICATION_EXCHANGE = "flash-food.notification.exchange";
    public static final String ORDER_EXCHANGE        = "flash-food.order.exchange";
    public static final String FLASH_SALE_EXCHANGE   = "flash-food.flash-sale.exchange";

    /** Single dead-letter exchange shared by all three main queues. */
    public static final String DEAD_LETTER_EXCHANGE = "flash-food.dead.letter.exchange";

    // -------------------------------------------------------------------------
    // Routing-key constants
    //
    // Binding keys (consumer side) use wildcards; publisher keys are specific.
    // -------------------------------------------------------------------------

    /** Consumer binding pattern for the notification queue. */
    public static final String NOTIFICATION_BINDING_KEY = "notification.#";
    /** Consumer binding pattern for the order queue. */
    public static final String ORDER_BINDING_KEY        = "order.#";
    /** Consumer binding pattern for the flash-sale queue. */
    public static final String FLASH_SALE_BINDING_KEY   = "flash-sale.#";

    /** Publisher key → notification queue. */
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";

    /** Publisher keys → order queue (one per lifecycle state). */
    public static final String ORDER_CREATED_ROUTING_KEY   = "order.created";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String ORDER_PREPARING_ROUTING_KEY = "order.preparing";
    public static final String ORDER_READY_ROUTING_KEY     = "order.ready";
    public static final String ORDER_COMPLETED_ROUTING_KEY = "order.completed";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";

    /** Publisher key → flash-sale queue. */
    public static final String FLASH_SALE_ROUTING_KEY = "flash-sale.notify";

    // =========================================================================
    // Dead-letter infrastructure
    // =========================================================================

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public Queue orderDlq() {
        return QueueBuilder.durable(ORDER_DLQ).build();
    }

    @Bean
    public Queue flashSaleDlq() {
        return QueueBuilder.durable(FLASH_SALE_DLQ).build();
    }

    /**
     * Binds each DLQ to the dead-letter exchange using the DLQ name as the routing key.
     * The main-queue {@code x-dead-letter-routing-key} must match these keys exactly.
     */
    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlq()).to(deadLetterExchange()).with(NOTIFICATION_DLQ);
    }

    @Bean
    public Binding orderDlqBinding() {
        return BindingBuilder.bind(orderDlq()).to(deadLetterExchange()).with(ORDER_DLQ);
    }

    @Bean
    public Binding flashSaleDlqBinding() {
        return BindingBuilder.bind(flashSaleDlq()).to(deadLetterExchange()).with(FLASH_SALE_DLQ);
    }

    // =========================================================================
    // Main queues
    // =========================================================================

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange",    DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange",    DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_DLQ)
                .build();
    }

    @Bean
    public Queue flashSaleQueue() {
        return QueueBuilder.durable(FLASH_SALE_QUEUE)
                .withArgument("x-dead-letter-exchange",    DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", FLASH_SALE_DLQ)
                .build();
    }

    // =========================================================================
    // Exchanges
    // =========================================================================

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange flashSaleExchange() {
        return new TopicExchange(FLASH_SALE_EXCHANGE, true, false);
    }

    // =========================================================================
    // Bindings
    // =========================================================================

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange()).with(NOTIFICATION_BINDING_KEY);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange()).with(ORDER_BINDING_KEY);
    }

    @Bean
    public Binding flashSaleBinding() {
        return BindingBuilder.bind(flashSaleQueue())
                .to(flashSaleExchange()).with(FLASH_SALE_BINDING_KEY);
    }

    // =========================================================================
    // Message converter — JSON with Java 8 date/time (LocalDateTime) support
    //
    // Spring Boot's AMQP auto-configuration picks up the single MessageConverter
    // bean and applies it to both RabbitTemplate and the listener container
    // factory automatically.
    // =========================================================================

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    // =========================================================================
    // RabbitTemplate
    // =========================================================================

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        // Return undeliverable messages to sender rather than silently dropping them
        template.setMandatory(true);
        return template;
    }
}
