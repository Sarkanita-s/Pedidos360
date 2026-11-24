package com.pedidos360.backend.service;

import com.pedidos360.backend.config.KafkaConfig;
import com.pedidos360.backend.dto.OrderKafkaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaProducer.class);

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public OrderKafkaProducer(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publica un evento de pedido en el tópico principal 'pedidos.order.events'
     * y replica la traza en el tópico de auditoría 'pedidos.audit.events'.
     * Utiliza orderId como clave de partición para garantizar orden de eventos.
     */
    public CompletableFuture<SendResult<Object, Object>> sendOrderEvent(OrderKafkaEvent event) {
        String partitionKey = (event.getOrderId() != null) ? event.getOrderId().toString() : event.getEventId();

        log.info("[KAFKA PRODUCER] Enviando evento de pedido a tópico '{}' [Key: {}] -> {}",
                KafkaConfig.TOPIC_ORDER_EVENTS, partitionKey, event);

        CompletableFuture<SendResult<Object, Object>> future = kafkaTemplate.send(
                KafkaConfig.TOPIC_ORDER_EVENTS,
                partitionKey,
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[KAFKA PRODUCER] Evento de pedido entregado con éxito a partición {} con offset {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("[KAFKA PRODUCER] Error enviando evento de pedido ID: {}. Causa: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });

        // También registrar en el tópico de auditoría (Topic 2)
        sendAuditEvent(event);

        return future;
    }

    /**
     * Publica evento en el tópico de auditoría 'pedidos.audit.events'
     */
    public CompletableFuture<SendResult<Object, Object>> sendAuditEvent(OrderKafkaEvent event) {
        String partitionKey = (event.getOrderId() != null) ? event.getOrderId().toString() : event.getEventId();

        log.info("[KAFKA PRODUCER] Registrando traza de auditoría en '{}' [Key: {}]",
                KafkaConfig.TOPIC_AUDIT_EVENTS, partitionKey);

        return kafkaTemplate.send(
                KafkaConfig.TOPIC_AUDIT_EVENTS,
                partitionKey,
                event
        );
    }
}
