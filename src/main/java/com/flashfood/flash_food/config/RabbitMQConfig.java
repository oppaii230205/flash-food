package com.flashfood.flash_food.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for messaging and notifications
 */
@Configuration
public class RabbitMQConfig {
    
    // Queue names
    public static final String NOTIFICATION_QUEUE = "flash-food.notification.queue";
    public static final String ORDER_QUEUE = "flash-food.order.queue";
    public static final String FLASH_SALE_QUEUE = "flash-food.flash-sale.queue";
    
    // Exchange names
    public static final String NOTIFICATION_EXCHANGE = "flash-food.notification.exchange";
    public static final String ORDER_EXCHANGE = "flash-food.order.exchange";
    public static final String FLASH_SALE_EXCHANGE = "flash-food.flash-sale.exchange";
    
    // Routing keys
    public static final String NOTIFICATION_ROUTING_KEY = "notification.#";
    public static final String ORDER_ROUTING_KEY = "order.#";
    public static final String FLASH_SALE_ROUTING_KEY = "flash-sale.#";
    
    // Notification Queue
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .build();
    }
    
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }
    
    // Order Queue
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .build();
    }
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    @Bean
    public Binding orderBinding() {
        return BindingBuilder
                .bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }
    
    // Flash Sale Queue (for mass notifications)
    @Bean
    public Queue flashSaleQueue() {
        return QueueBuilder.durable(FLASH_SALE_QUEUE)
                .build();
    }
    
    @Bean
    public TopicExchange flashSaleExchange() {
        return new TopicExchange(FLASH_SALE_EXCHANGE);
    }
    
    @Bean
    public Binding flashSaleBinding() {
        return BindingBuilder
                .bind(flashSaleQueue())
                .to(flashSaleExchange())
                .with(FLASH_SALE_ROUTING_KEY);
    }
    
    // Message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
