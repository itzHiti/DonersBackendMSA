package kz.itzhiti.orderservice.service;

import kz.itzhiti.orderservice.event.OrderCreatedEvent;
import kz.itzhiti.orderservice.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Sending OrderCreatedEvent for order ID: {}", event.getOrderId());
        kafkaTemplate.send("order-created", event.getOrderId().toString(), event);
    }

    public void sendOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        log.info("Sending OrderStatusChangedEvent for order ID: {}, new status: {}",
                event.getOrderId(), event.getNewStatus());
        kafkaTemplate.send("order-status-changed", event.getOrderId().toString(), event);
    }
}
