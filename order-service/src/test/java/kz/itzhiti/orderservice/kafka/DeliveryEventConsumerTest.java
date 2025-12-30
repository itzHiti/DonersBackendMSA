package kz.itzhiti.orderservice.kafka;

import kz.itzhiti.orderservice.event.DeliveryCompletedEvent;
import kz.itzhiti.orderservice.model.Order;
import kz.itzhiti.orderservice.model.enums.OrderStatus;
import kz.itzhiti.orderservice.repository.OrderRepository;
import kz.itzhiti.orderservice.service.OrderEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@ActiveProfiles("test")
@Transactional
class DeliveryEventConsumerTest {

    @Autowired
    private OrderEventConsumer orderEventConsumer;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        testOrder = Order.builder()
                .customerId("user123")
                .status(OrderStatus.IN_DELIVERY)
                .deliveryAddress("Test address")
                .phone("+77771234567")
                .totalPrice(BigDecimal.valueOf(10000))
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void handleDeliveryCompleted_shouldUpdateOrderStatusToDelivered() {
        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder()
                .deliveryId(1L)
                .orderId(testOrder.getId())
                .completedAt(LocalDateTime.now())
                .build();

        orderEventConsumer.handleDeliveryCompleted(event);

        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }

    @Test
    void handleDeliveryCompleted_nonExistingOrder_shouldNotThrowException() {
        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder()
                .deliveryId(1L)
                .orderId(9999L)
                .completedAt(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> orderEventConsumer.handleDeliveryCompleted(event));
    }

    @Test
    void handleDeliveryCompleted_shouldNotChangeAlreadyDeliveredOrder() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(testOrder);

        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder()
                .deliveryId(1L)
                .orderId(testOrder.getId())
                .completedAt(LocalDateTime.now())
                .build();

        orderEventConsumer.handleDeliveryCompleted(event);

        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }
}
