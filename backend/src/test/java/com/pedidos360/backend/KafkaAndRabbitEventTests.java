package com.pedidos360.backend;

import com.pedidos360.backend.config.KafkaConfig;
import com.pedidos360.backend.config.RabbitMQConfig;
import com.pedidos360.backend.dto.OrderEventMessage;
import com.pedidos360.backend.dto.OrderKafkaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KafkaAndRabbitEventTests {

    @Test
    @DisplayName("Validar construcción y campos de OrderKafkaEvent (EP5/EP6)")
    void testOrderKafkaEventStructure() {
        OrderKafkaEvent event = new OrderKafkaEvent(
                100L,
                "ORDER_CREATED",
                "PENDIENTE",
                "Juan Pérez",
                15990.0,
                "user-oid-12345",
                "Evento inicial en Kafka"
        );

        assertNotNull(event.getEventId(), "EventId debe generarse automáticamente");
        assertEquals(100L, event.getOrderId());
        assertEquals("ORDER_CREATED", event.getEventType());
        assertEquals("PENDIENTE", event.getStatus());
        assertEquals("Juan Pérez", event.getCustomerName());
        assertEquals(15990.0, event.getTotal());
        assertEquals("user-oid-12345", event.getOwnerOid());
        assertNotNull(event.getTimestamp());
        assertEquals("Evento inicial en Kafka", event.getDetails());
    }

    @Test
    @DisplayName("Validar configuración de tópicos y réplicas de Kafka")
    void testKafkaConfigConstants() {
        assertEquals("pedidos.order.events", KafkaConfig.TOPIC_ORDER_EVENTS);
        assertEquals("pedidos.audit.events", KafkaConfig.TOPIC_AUDIT_EVENTS);
        assertEquals("pedidos.order.events.DLT", KafkaConfig.TOPIC_ORDER_EVENTS_DLT);
        assertEquals("pedidos.audit.events.DLT", KafkaConfig.TOPIC_AUDIT_EVENTS_DLT);

        assertEquals("pedidos360-order-group", KafkaConfig.GROUP_ORDER);
        assertEquals("pedidos360-audit-group", KafkaConfig.GROUP_AUDIT);
        assertEquals("pedidos360-dlt-group", KafkaConfig.GROUP_DLT);
    }

    @Test
    @DisplayName("Validar configuración y nombres de colas RabbitMQ (EP3/EP4)")
    void testRabbitMQConfigConstants() {
        assertEquals("pedidos.direct.exchange", RabbitMQConfig.DIRECT_EXCHANGE);
        assertEquals("pedidos.topic.exchange", RabbitMQConfig.TOPIC_EXCHANGE);
        assertEquals("pedidos.dlx.exchange", RabbitMQConfig.DEAD_LETTER_EXCHANGE);

        assertEquals("pedidos.created.queue", RabbitMQConfig.QUEUE_ORDER_CREATED);
        assertEquals("pedidos.status.queue", RabbitMQConfig.QUEUE_ORDER_STATUS);
        assertEquals("pedidos.notification.queue", RabbitMQConfig.QUEUE_NOTIFICATION);
    }

    @Test
    @DisplayName("Validar construcción de OrderEventMessage para RabbitMQ")
    void testOrderEventMessage() {
        OrderEventMessage message = new OrderEventMessage(
                200L,
                "ORDER_STATUS_CHANGED",
                "ACEPTADO",
                "user-oid-9999",
                "Actualización de estado"
        );

        assertEquals(200L, message.getOrderId());
        assertEquals("ORDER_STATUS_CHANGED", message.getEventType());
        assertEquals("ACEPTADO", message.getStatus());
        assertEquals("user-oid-9999", message.getOwnerOid());
        assertNotNull(message.getTimestamp());
    }
}
