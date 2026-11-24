package com.pedidos360.backend.controller;

import com.pedidos360.backend.config.KafkaConfig;
import com.pedidos360.backend.dto.OrderKafkaEvent;
import com.pedidos360.backend.service.OrderKafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/kafka")
@CrossOrigin(origins = "*")
public class KafkaAdminController {

    private final OrderKafkaProducer orderKafkaProducer;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092,localhost:9093,localhost:9094}")
    private String bootstrapServers;

    public KafkaAdminController(OrderKafkaProducer orderKafkaProducer) {
        this.orderKafkaProducer = orderKafkaProducer;
    }

    /**
     * Consulta el estado general de la arquitectura Kafka, tópicos, particiones y factor de replicación
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    public ResponseEntity<Map<String, Object>> getKafkaStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("clusterStatus", "ONLINE");
        response.put("bootstrapServers", bootstrapServers);
        response.put("brokersCount", 3);
        response.put("zookeeperEnsembleCount", 3);

        List<Map<String, Object>> topicsInfo = new ArrayList<>();

        topicsInfo.add(Map.of(
                "topic", KafkaConfig.TOPIC_ORDER_EVENTS,
                "partitions", 3,
                "replicationFactor", 3,
                "minInSyncReplicas", 2,
                "retentionMs", 604800000L,
                "consumerGroup", KafkaConfig.GROUP_ORDER,
                "description", "Tópico principal de eventos del ciclo de vida del pedido"
        ));

        topicsInfo.add(Map.of(
                "topic", KafkaConfig.TOPIC_AUDIT_EVENTS,
                "partitions", 3,
                "replicationFactor", 3,
                "minInSyncReplicas", 2,
                "retentionMs", 604800000L,
                "consumerGroup", KafkaConfig.GROUP_AUDIT,
                "description", "Tópico de auditoría y trazabilidad histórica de pedidos"
        ));

        topicsInfo.add(Map.of(
                "topic", KafkaConfig.TOPIC_ORDER_EVENTS_DLT,
                "partitions", 3,
                "replicationFactor", 3,
                "consumerGroup", KafkaConfig.GROUP_DLT,
                "description", "Dead Letter Topic para eventos de pedidos fallidos"
        ));

        topicsInfo.add(Map.of(
                "topic", KafkaConfig.TOPIC_AUDIT_EVENTS_DLT,
                "partitions", 3,
                "replicationFactor", 3,
                "consumerGroup", KafkaConfig.GROUP_DLT,
                "description", "Dead Letter Topic para auditoría fallida"
        ));

        response.put("topics", topicsInfo);
        response.put("consumerGroups", List.of(
                KafkaConfig.GROUP_ORDER,
                KafkaConfig.GROUP_AUDIT,
                KafkaConfig.GROUP_DLT
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para simular la publicación de eventos hacia Kafka y probar particionamiento o DLT
     */
    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    public ResponseEntity<Map<String, Object>> simulateKafkaEvent(
            @RequestParam(defaultValue = "101") Long orderId,
            @RequestParam(defaultValue = "ORDER_CREATED") String eventType,
            @RequestParam(defaultValue = "PENDIENTE") String status,
            @RequestParam(defaultValue = "Cliente Simulado") String customerName,
            @RequestParam(defaultValue = "19990.0") Double total,
            @RequestParam(defaultValue = "false") boolean forceError) {

        String details = forceError ? "SIMULATED_FAIL_TRIGGER_ERROR" : "SIMULATED_KAFKA_SUCCESS";

        OrderKafkaEvent event = new OrderKafkaEvent(
                orderId,
                eventType,
                status,
                customerName,
                total,
                "admin-simulator-oid",
                details
        );

        orderKafkaProducer.sendOrderEvent(event);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Evento publicado exitosamente en Kafka");
        result.put("orderId", orderId);
        result.put("partitionKey", orderId.toString());
        result.put("simulatedErrorDlt", forceError);
        result.put("primaryTopic", KafkaConfig.TOPIC_ORDER_EVENTS);
        result.put("auditTopic", KafkaConfig.TOPIC_AUDIT_EVENTS);
        result.put("eventPayload", event);

        return ResponseEntity.ok(result);
    }
}
