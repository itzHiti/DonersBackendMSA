package kz.itzhiti.deliveryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String ORDER_CREATED_TOPIC = "order-created";
    public static final String DELIVERY_COMPLETED_TOPIC = "delivery-completed";

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deliveryCompletedTopic() {
        return TopicBuilder.name(DELIVERY_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

