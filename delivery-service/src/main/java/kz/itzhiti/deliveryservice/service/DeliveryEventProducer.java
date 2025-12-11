package kz.itzhiti.deliveryservice.service;

import kz.itzhiti.deliveryservice.config.KafkaConfig;
import kz.itzhiti.deliveryservice.event.DeliveryCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDeliveryCompletedEvent(DeliveryCompletedEvent event) {
        log.info("Sending DeliveryCompletedEvent for order ID: {}", event.getOrderId());
        kafkaTemplate.send(KafkaConfig.DELIVERY_COMPLETED_TOPIC, event.getOrderId().toString(), event);
    }
}

