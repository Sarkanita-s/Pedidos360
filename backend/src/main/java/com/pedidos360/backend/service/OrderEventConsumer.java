package com.pedidos360.backend.service;

import com.pedidos360.backend.config.RabbitMQConfig;
import com.pedidos360.backend.dto.OrderEventMessage;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    /**
     * Consumidor de la cola 'pedidos.created.queue'.
     * Procesa con ACK manual o NACK según validación.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CREATED)
    public void receiveOrderCreated(
            OrderEventMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[CONSUMER - CREATED] Mensaje recibido: {}", message);

        try {
            // Validación de integridad del payload
            if (message.getOrderId() == null || message.getOrderId() <= 0) {
                throw new IllegalArgumentException("ID de pedido inválido en payload recibido");
            }

            // Simular error si los detalles indican 'TRIGGER_ERROR' para evidenciar DLQ
            if (message.getDetails() != null && message.getDetails().contains("TRIGGER_ERROR")) {
                throw new RuntimeException("Falla forzada para prueba de DLQ");
            }

            log.info("[CONSUMER - CREATED] Procesamiento exitoso para pedido ID: {}", message.getOrderId());
            // ACK manual exitoso
            channel.basicAck(deliveryTag, false);

        } catch (Exception ex) {
            log.error("[CONSUMER - CREATED] Error procesando pedido ID: {}. Enviando a DLQ. Causa: {}",
                    message.getOrderId(), ex.getMessage());
            // basicNack con requeue=false -> Se redirige automáticamente al Dead Letter Exchange
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Consumidor de la cola 'pedidos.status.queue'.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_STATUS)
    public void receiveOrderStatus(
            OrderEventMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[CONSUMER - STATUS] Mensaje recibido: {}", message);

        try {
            if (message.getStatus() == null || message.getStatus().isBlank()) {
                throw new IllegalArgumentException("Estado vacío o no especificado");
            }

            if (message.getDetails() != null && message.getDetails().contains("TRIGGER_ERROR")) {
                throw new RuntimeException("Falla forzada de actualización de estado para prueba de DLQ");
            }

            log.info("[CONSUMER - STATUS] Estado de pedido {} actualizado a: {}", message.getOrderId(), message.getStatus());
            channel.basicAck(deliveryTag, false);

        } catch (Exception ex) {
            log.error("[CONSUMER - STATUS] Error procesando actualización de estado. Enviando a DLQ: {}", ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Consumidor de la cola 'pedidos.notification.queue'.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION)
    public void receiveNotification(
            OrderEventMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[CONSUMER - NOTIF] Notificación recibida para usuario: {}", message.getOwnerOid());

        try {
            if (message.getDetails() != null && message.getDetails().contains("TRIGGER_ERROR")) {
                throw new RuntimeException("Falla simulada en despacho de notificación para DLQ");
            }

            log.info("[CONSUMER - NOTIF] Notificación despachada con éxito a {}", message.getOwnerOid());
            channel.basicAck(deliveryTag, false);

        } catch (Exception ex) {
            log.error("[CONSUMER - NOTIF] Error despachando notificación. Enviando a DLQ: {}", ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Consumidor auditor de Dead Letter Queues (DLQ) para logging y trazabilidad
     */
    @RabbitListener(queues = {
            RabbitMQConfig.DLQ_ORDER_CREATED,
            RabbitMQConfig.DLQ_ORDER_STATUS,
            RabbitMQConfig.DLQ_NOTIFICATION
    })
    public void processDlqMessages(
            OrderEventMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(AmqpHeaders.CONSUMER_QUEUE) String queueName) throws IOException {

        log.warn("[DLQ LISTENER] ¡Mensaje recibido en cola de fallos {}!: {}", queueName, message);
        // Marcamos ACK en el DLQ una vez auditado y logueado
        channel.basicAck(deliveryTag, false);
    }
}
