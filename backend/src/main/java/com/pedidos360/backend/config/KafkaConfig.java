package com.pedidos360.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092,localhost:9093,localhost:9094}")
    private String bootstrapServers;

    // ========================================================
    // TÓPICOS EVALUADOS (EP5 / EP6)
    // 2 tópicos con 3 particiones y factor de replicación 3
    // ========================================================
    public static final String TOPIC_ORDER_EVENTS = "pedidos.order.events";
    public static final String TOPIC_AUDIT_EVENTS = "pedidos.audit.events";

    // Tópicos Dead Letter Topic (DLT)
    public static final String TOPIC_ORDER_EVENTS_DLT = "pedidos.order.events.DLT";
    public static final String TOPIC_AUDIT_EVENTS_DLT = "pedidos.audit.events.DLT";

    // Consumer Groups
    public static final String GROUP_ORDER = "pedidos360-order-group";
    public static final String GROUP_AUDIT = "pedidos360-audit-group";
    public static final String GROUP_DLT = "pedidos360-dlt-group";

    /**
     * Tópico 1: pedidos.order.events
     * 3 particiones, factor de replicación 3, retención 7 días
     */
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(TOPIC_ORDER_EVENTS)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .config("retention.ms", "604800000") // 7 días en milisegundos
                .build();
    }

    /**
     * Tópico 2: pedidos.audit.events
     * 3 particiones, factor de replicación 3, retención 7 días
     */
    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(TOPIC_AUDIT_EVENTS)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .config("retention.ms", "604800000")
                .build();
    }

    /**
     * Dead Letter Topic para order events con réplica 3
     */
    @Bean
    public NewTopic orderEventsDltTopic() {
        return TopicBuilder.name(TOPIC_ORDER_EVENTS_DLT)
                .partitions(3)
                .replicas(3)
                .build();
    }

    /**
     * Dead Letter Topic para audit events con réplica 3
     */
    @Bean
    public NewTopic auditEventsDltTopic() {
        return TopicBuilder.name(TOPIC_AUDIT_EVENTS_DLT)
                .partitions(3)
                .replicas(3)
                .build();
    }

    /**
     * ProducerFactory configurado con StringSerializer y JsonSerializer
     */
    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate para publicación de eventos a tópicos y Dead Letter Topic (DLT)
     */
    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * ConsumerFactory configurado con deserializador JSON y paquetes de confianza
     */
    @Bean
    public ConsumerFactory<Object, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ORDER);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Manejador de errores para Kafka con reintentos y desvío automático a Dead Letter Topic (DLT)
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        // 2 intentos con 1 segundo de intervalo antes de enviar a DLT
        FixedBackOff backOff = new FixedBackOff(1000L, 2L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    /**
     * Container Factory configurado con el Error Handler y DLT
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
