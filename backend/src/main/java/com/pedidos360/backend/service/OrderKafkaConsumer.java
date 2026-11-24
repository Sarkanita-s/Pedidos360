package com.pedidos360.backend.service;

import com.pedidos360.backend.config.KafkaConfig;
import com.pedidos360.backend.dto.OrderKafkaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaConsumer.class);

    /**
     * Consumidor para 'pedidos.order.events' perteneciendo al Consumer Group 'pedidos360-order-group'.
     * Observa particiones asignadas y simula fallas forzadas para DLT si se solicita.
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_ORDER_EVENTS,
            groupId = KafkaConfig.GROUP_ORDER,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(
            @Payload OrderKafkaEvent event,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[KAFKA CONSUMER - ORDER GROUP] Evento recibido en tópico '{}' [Partición: {}, Offset: {}, Clave: {}] -> Evento: {}",
                KafkaConfig.TOPIC_ORDER_EVENTS, partition, offset, key, event);

        // Validación o simulación de error forzado para evidenciar Dead Letter Topic (DLT)
        if (event.getDetails() != null && event.getDetails().contains("TRIGGER_ERROR")) {
            log.error("[KAFKA CONSUMER - ORDER GROUP] Detectado disparador de falla 'TRIGGER_ERROR' en pedido ID: {}. Lanzando excepción hacia DLT...",
                    event.getOrderId());
            throw new RuntimeException("Falla forzada en consumidor Kafka para verificación de Dead Letter Topic (DLT)");
        }

        log.info("[KAFKA CONSUMER - ORDER GROUP] Procesamiento exitoso para pedido ID: {} ({}) en partición {}",
                event.getOrderId(), event.getStatus(), partition);
    }

    /**
     * Consumidor de auditoría independiente para 'pedidos.audit.events'
     * con Consumer Group 'pedidos360-audit-group'.
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_AUDIT_EVENTS,
            groupId = KafkaConfig.GROUP_AUDIT,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAuditEvent(
            @Payload OrderKafkaEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[KAFKA AUDIT CONSUMER] Traza de auditoría registrada. Pedido: {}, Estado: {}, Usuario: {} [Partición: {}, Offset: {}]",
                event.getOrderId(), event.getStatus(), event.getOwnerOid(), partition, offset);
    }

    /**
     * Consumidor auditor de Dead Letter Topic (DLT) para trazabilidad de mensajes fallidos
     */
    @KafkaListener(
            topics = {KafkaConfig.TOPIC_ORDER_EVENTS_DLT, KafkaConfig.TOPIC_AUDIT_EVENTS_DLT},
            groupId = KafkaConfig.GROUP_DLT,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDeadLetterEvent(
            @Payload Object payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.warn("[KAFKA DLT AUDITOR] ¡Alerta! Mensaje recibido en Dead Letter Topic '{}' [Partición: {}, Offset: {}]: {}",
                topic, partition, offset, payload);
    }
}
