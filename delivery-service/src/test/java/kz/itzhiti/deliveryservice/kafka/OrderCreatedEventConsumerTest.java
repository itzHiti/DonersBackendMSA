package kz.itzhiti.deliveryservice.kafka;

import kz.itzhiti.deliveryservice.event.OrderCreatedEvent;
import kz.itzhiti.deliveryservice.model.Delivery;
import kz.itzhiti.deliveryservice.model.enums.DeliveryStatus;
import kz.itzhiti.deliveryservice.repository.DeliveryRepository;
import kz.itzhiti.deliveryservice.service.DeliveryEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderCreatedEventConsumerTest {

    @Autowired
    private DeliveryEventConsumer deliveryEventConsumer;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @BeforeEach
    void setUp() {
        deliveryRepository.deleteAll();
    }

    @Test
    void handleOrderCreated_shouldCreateDelivery() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(101L)
                .customerId("user123")
                .deliveryAddress("Алматы, ул. Абая 10")
                .phone("+77771234567")
                .totalPrice(BigDecimal.valueOf(5000))
                .createdAt(LocalDateTime.now())
                .build();

        deliveryEventConsumer.handleOrderCreated(event);

        List<Delivery> deliveries = deliveryRepository.findAll();
        assertEquals(1, deliveries.size());

        Delivery delivery = deliveries.get(0);
        assertEquals(101L, delivery.getOrderId());
        assertEquals("Алматы, ул. Абая 10", delivery.getAddress());
        assertEquals("+77771234567", delivery.getPhone());
        assertEquals(DeliveryStatus.PENDING, delivery.getStatus());
        assertNotNull(delivery.getCreatedAt());
    }

    @Test
    void handleOrderCreated_multipleOrders_shouldCreateMultipleDeliveries() {
        OrderCreatedEvent event1 = OrderCreatedEvent.builder()
                .orderId(101L)
                .customerId("user123")
                .deliveryAddress("Address 1")
                .phone("+77771234567")
                .totalPrice(BigDecimal.valueOf(5000))
                .createdAt(LocalDateTime.now())
                .build();

        OrderCreatedEvent event2 = OrderCreatedEvent.builder()
                .orderId(102L)
                .customerId("user456")
                .deliveryAddress("Address 2")
                .phone("+77779876543")
                .totalPrice(BigDecimal.valueOf(3000))
                .createdAt(LocalDateTime.now())
                .build();

        deliveryEventConsumer.handleOrderCreated(event1);
        deliveryEventConsumer.handleOrderCreated(event2);

        List<Delivery> deliveries = deliveryRepository.findAll();
        assertEquals(2, deliveries.size());

        assertTrue(deliveries.stream()
                .anyMatch(d -> d.getOrderId().equals(101L)));
        assertTrue(deliveries.stream()
                .anyMatch(d -> d.getOrderId().equals(102L)));
    }

    @Test
    void handleOrderCreated_withNullPhone_shouldStillCreateDelivery() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(101L)
                .customerId("user123")
                .deliveryAddress("Алматы, ул. Абая 10")
                .phone(null)
                .totalPrice(BigDecimal.valueOf(5000))
                .createdAt(LocalDateTime.now())
                .build();

        deliveryEventConsumer.handleOrderCreated(event);

        List<Delivery> deliveries = deliveryRepository.findAll();
        assertEquals(1, deliveries.size());

        Delivery delivery = deliveries.get(0);
        assertEquals(101L, delivery.getOrderId());
        assertNull(delivery.getPhone());
    }

    @Test
    void handleOrderCreated_shouldSetPendingStatus() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(101L)
                .customerId("user123")
                .deliveryAddress("Алматы, ул. Абая 10")
                .phone("+77771234567")
                .totalPrice(BigDecimal.valueOf(5000))
                .createdAt(LocalDateTime.now())
                .build();

        deliveryEventConsumer.handleOrderCreated(event);

        Delivery delivery = deliveryRepository.findAll().get(0);
        assertEquals(DeliveryStatus.PENDING, delivery.getStatus());
        assertNull(delivery.getCourier());
    }
}
