package kz.itzhiti.orderservice.service;

import kz.itzhiti.orderservice.event.DeliveryCompletedEvent;
import kz.itzhiti.orderservice.model.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "delivery-completed", groupId = "order-service-group")
    public void handleDeliveryCompleted(DeliveryCompletedEvent event) {
        log.info("Received DeliveryCompletedEvent for order ID: {}", event.getOrderId());
        try {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.DELIVERED);
            log.info("Order {} marked as DELIVERED", event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing DeliveryCompletedEvent for order {}: {}",
                    event.getOrderId(), e.getMessage());
        }
    }
}
