package com.pedidos360.backend.service;

import com.pedidos360.backend.config.RabbitMQConfig;
import com.pedidos360.backend.dto.OrderEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderEventMessage message) {
        log.info("[PRODUCER] Publicando evento de pedido creado a Direct Exchange -> {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_ORDER_CREATED,
                message
        );
    }

    public void publishOrderStatusChanged(OrderEventMessage message) {
        log.info("[PRODUCER] Publicando cambio de estado a Direct Exchange -> {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_ORDER_STATUS,
                message
        );
    }

    public void publishNotification(OrderEventMessage message, String routingKey) {
        String key = (routingKey != null && !routingKey.isBlank())
                ? routingKey
                : RabbitMQConfig.ROUTING_KEY_NOTIF_PUBLISH;

        log.info("[PRODUCER] Publicando notificación a Topic Exchange ({}) -> {}", key, message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TOPIC_EXCHANGE,
                key,
                message
        );
    }
}
