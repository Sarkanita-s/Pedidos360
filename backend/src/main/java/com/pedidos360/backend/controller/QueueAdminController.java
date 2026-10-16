package com.pedidos360.backend.controller;

import com.pedidos360.backend.config.RabbitMQConfig;
import com.pedidos360.backend.dto.OrderEventMessage;
import com.pedidos360.backend.service.OrderEventProducer;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/queues")
@CrossOrigin(origins = "*")
public class QueueAdminController {

    private final AmqpAdmin amqpAdmin;
    private final OrderEventProducer orderEventProducer;

    public QueueAdminController(AmqpAdmin amqpAdmin, OrderEventProducer orderEventProducer) {
        this.amqpAdmin = amqpAdmin;
        this.orderEventProducer = orderEventProducer;
    }

    /**
     * Obtiene el estado y conteo de mensajes de todas las colas y DLQs del clúster
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    public ResponseEntity<Map<String, Object>> getQueuesStatus() {
        List<String> monitoredQueues = List.of(
                RabbitMQConfig.QUEUE_ORDER_CREATED,
                RabbitMQConfig.QUEUE_ORDER_STATUS,
                RabbitMQConfig.QUEUE_NOTIFICATION,
                RabbitMQConfig.DLQ_ORDER_CREATED,
                RabbitMQConfig.DLQ_ORDER_STATUS,
                RabbitMQConfig.DLQ_NOTIFICATION
        );

        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> queuesDetails = new ArrayList<>();

        for (String queueName : monitoredQueues) {
            QueueInformation info = amqpAdmin.getQueueInfo(queueName);
            Map<String, Object> queueData = new HashMap<>();
            queueData.put("name", queueName);
            queueData.put("messageCount", info != null ? info.getMessageCount() : 0);
            queueData.put("consumerCount", info != null ? info.getConsumerCount() : 0);
            queueData.put("isDlq", queueName.endsWith(".dlq"));
            queuesDetails.add(queueData);
        }

        response.put("clusterStatus", "ONLINE");
        response.put("queues", queuesDetails);

        return ResponseEntity.ok(response);
    }

    /**
     * Purga los mensajes acumulados en una cola
     */
    @PostMapping("/purge/{queueName}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Map<String, Object>> purgeQueue(@PathVariable String queueName) {
        amqpAdmin.purgeQueue(queueName, false);
        return ResponseEntity.ok(Map.of(
                "message", "Cola purgada exitosamente",
                "queue", queueName
        ));
    }

    /**
     * Endpoint de prueba y validación para inyectar mensajes directos o forzar error hacia DLQ
     */
    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    public ResponseEntity<Map<String, Object>> simulateMessage(
            @RequestParam(defaultValue = "created") String type,
            @RequestParam(defaultValue = "false") boolean forceError) {

        String details = forceError ? "SIMULATED_FAIL_TRIGGER_ERROR" : "SIMULATED_SUCCESS";

        OrderEventMessage message = new OrderEventMessage(
                999L,
                "SIMULATION_" + type.toUpperCase(),
                "TESTING",
                "admin-simulator-user",
                details
        );

        switch (type.toLowerCase()) {
            case "status" -> orderEventProducer.publishOrderStatusChanged(message);
            case "notif" -> orderEventProducer.publishNotification(message, "pedidos.notif.simulation");
            default -> orderEventProducer.publishOrderCreated(message);
        }

        return ResponseEntity.ok(Map.of(
                "status", "Mensaje enviado a RabbitMQ",
                "simulatedError", forceError,
                "target", type,
                "messagePayload", message
        ));
    }
}
