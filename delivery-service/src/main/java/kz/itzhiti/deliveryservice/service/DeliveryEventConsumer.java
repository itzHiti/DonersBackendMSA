package kz.itzhiti.deliveryservice.service;

import kz.itzhiti.deliveryservice.config.KafkaConfig;
import kz.itzhiti.deliveryservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(topics = KafkaConfig.ORDER_CREATED_TOPIC, groupId = "delivery-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order ID: {}", event.getOrderId());
        try {
            deliveryService.createDeliveryFromOrder(event);
            log.info("Delivery created for order {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Error creating delivery for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}

