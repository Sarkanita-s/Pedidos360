package com.pedidos360.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String DIRECT_EXCHANGE = "pedidos.direct.exchange";
    public static final String TOPIC_EXCHANGE = "pedidos.topic.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "pedidos.dlx.exchange";

    // Queues Principales
    public static final String QUEUE_ORDER_CREATED = "pedidos.created.queue";
    public static final String QUEUE_ORDER_STATUS = "pedidos.status.queue";
    public static final String QUEUE_NOTIFICATION = "pedidos.notification.queue";

    // Queues DLQ (Dead Letter Queues)
    public static final String DLQ_ORDER_CREATED = "pedidos.created.dlq";
    public static final String DLQ_ORDER_STATUS = "pedidos.status.dlq";
    public static final String DLQ_NOTIFICATION = "pedidos.notification.dlq";

    // Routing Keys
    public static final String ROUTING_KEY_ORDER_CREATED = "pedido.creado";
    public static final String ROUTING_KEY_ORDER_STATUS = "pedido.estado";
    public static final String ROUTING_KEY_NOTIF_PATTERN = "pedidos.notif.#";
    public static final String ROUTING_KEY_NOTIF_PUBLISH = "pedidos.notif.email";

    // DLQ Routing Keys
    public static final String DLQ_ROUTING_KEY_ORDER_CREATED = "dlq.pedido.creado";
    public static final String DLQ_ROUTING_KEY_ORDER_STATUS = "dlq.pedido.estado";
    public static final String DLQ_ROUTING_KEY_NOTIFICATION = "dlq.pedido.notif";

    /* ========================================================
       1. EXCHANGES (Direct, Topic, DLX)
       ======================================================== */

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    /* ========================================================
       2. DEAD LETTER QUEUES (DLQ)
       ======================================================== */

    @Bean
    public Queue dlqOrderCreatedQueue() {
        return QueueBuilder.durable(DLQ_ORDER_CREATED).build();
    }

    @Bean
    public Queue dlqOrderStatusQueue() {
        return QueueBuilder.durable(DLQ_ORDER_STATUS).build();
    }

    @Bean
    public Queue dlqNotificationQueue() {
        return QueueBuilder.durable(DLQ_NOTIFICATION).build();
    }

    @Bean
    public Binding bindingDlqOrderCreated() {
        return BindingBuilder.bind(dlqOrderCreatedQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    public Binding bindingDlqOrderStatus() {
        return BindingBuilder.bind(dlqOrderStatusQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY_ORDER_STATUS);
    }

    @Bean
    public Binding bindingDlqNotification() {
        return BindingBuilder.bind(dlqNotificationQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY_NOTIFICATION);
    }

    /* ========================================================
       3. QUEUES PRINCIPALES (Con DLQ configurado)
       ======================================================== */

    @Bean
    public Queue orderCreatedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY_ORDER_CREATED);
        return new Queue(QUEUE_ORDER_CREATED, true, false, false, args);
    }

    @Bean
    public Queue orderStatusQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY_ORDER_STATUS);
        return new Queue(QUEUE_ORDER_STATUS, true, false, false, args);
    }

    @Bean
    public Queue notificationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY_NOTIFICATION);
        return new Queue(QUEUE_NOTIFICATION, true, false, false, args);
    }

    /* ========================================================
       4. BINDINGS PRINCIPALES (Direct y Topic)
       ======================================================== */

    @Bean
    public Binding bindingOrderCreated() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(directExchange())
                .with(ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    public Binding bindingOrderStatus() {
        return BindingBuilder.bind(orderStatusQueue())
                .to(directExchange())
                .with(ROUTING_KEY_ORDER_STATUS);
    }

    @Bean
    public Binding bindingNotification() {
        return BindingBuilder.bind(notificationQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_NOTIF_PATTERN);
    }

    /* ========================================================
       5. SERIALIZADOR JSON PARA MENSAJES
       ======================================================== */

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
