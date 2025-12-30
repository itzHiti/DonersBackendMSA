package kz.itzhiti.deliveryservice.service;

import kz.itzhiti.deliveryservice.dto.DeliveryDTO;
import kz.itzhiti.deliveryservice.dto.UpdateDeliveryStatusRequest;
import kz.itzhiti.deliveryservice.exception.ResourceNotFoundException;
import kz.itzhiti.deliveryservice.model.Courier;
import kz.itzhiti.deliveryservice.model.Delivery;
import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import kz.itzhiti.deliveryservice.model.enums.DeliveryStatus;
import kz.itzhiti.deliveryservice.repository.CourierRepository;
import kz.itzhiti.deliveryservice.repository.DeliveryRepository;
import kz.itzhiti.deliveryservice.service.DeliveryEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeliveryServiceTest {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private CourierRepository courierRepository;

    @BeforeEach
    void setUp() {
        deliveryRepository.deleteAll();
        courierRepository.deleteAll();
    }

    @Test
    void getAllDeliveries_shouldReturnAllDeliveries() {
        Courier courier = Courier.builder()
                .name("Иван Иванов")
                .phone("+77771234567")
                .status(CourierStatus.AVAILABLE)
                .build();
        courier = courierRepository.save(courier);

        Delivery delivery1 = Delivery.builder()
                .orderId(101L)
                .address("Адрес 1")
                .phone("+77771234567")
                .status(DeliveryStatus.ASSIGNED)
                .courier(courier)
                .build();
        deliveryRepository.save(delivery1);

        Delivery delivery2 = Delivery.builder()
                .orderId(102L)
                .address("Адрес 2")
                .phone("+77779876543")
                .status(DeliveryStatus.IN_TRANSIT)
                .courier(courier)
                .build();
        deliveryRepository.save(delivery2);

        List<DeliveryDTO> result = deliveryService.getAllDeliveries();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getDeliveryById_existingDelivery_shouldReturnDelivery() {
        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Тестовый адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.ASSIGNED)
                .build();
        delivery = deliveryRepository.save(delivery);

        DeliveryDTO result = deliveryService.getDeliveryById(delivery.getId());

        assertNotNull(result);
        assertEquals(101L, result.getOrderId());
        assertEquals("Тестовый адрес", result.getAddress());
    }

    @Test
    void getDeliveryById_nonExistingDelivery_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () ->
                deliveryService.getDeliveryById(999L)
        );
    }

    @Test
    void getDeliveryByOrderId_existingOrder_shouldReturnDelivery() {
        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Тестовый адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.PENDING)
                .build();
        delivery = deliveryRepository.save(delivery);

        DeliveryDTO result = deliveryService.getDeliveryByOrderId(101L);

        assertNotNull(result);
        assertEquals(101L, result.getOrderId());
        assertEquals("Тестовый адрес", result.getAddress());
    }

    @Test
    void getDeliveryByOrderId_nonExistingOrder_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () ->
                deliveryService.getDeliveryByOrderId(999L)
        );
    }

    @Test
    void getDeliveriesByStatus_shouldReturnFilteredDeliveries() {
        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.IN_TRANSIT)
                .build();
        deliveryRepository.save(delivery);

        List<DeliveryDTO> result = deliveryService.getDeliveriesByStatus(DeliveryStatus.IN_TRANSIT);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DeliveryStatus.IN_TRANSIT, result.get(0).getStatus());
    }

    @Test
    void getDeliveriesByCourier_shouldReturnCourierDeliveries() {
        Courier courier = Courier.builder()
                .name("Иван Иванов")
                .phone("+77771234567")
                .status(CourierStatus.AVAILABLE)
                .build();
        courier = courierRepository.save(courier);

        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.IN_TRANSIT)
                .courier(courier)
                .build();
        deliveryRepository.save(delivery);

        List<DeliveryDTO> result = deliveryService.getDeliveriesByCourier(courier.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(courier.getId(), result.get(0).getCourier().getId());
    }

    @Test
    void updateDeliveryStatus_toInTransit_shouldUpdateSuccessfully() {
        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.ASSIGNED)
                .build();
        delivery = deliveryRepository.save(delivery);

        UpdateDeliveryStatusRequest request = UpdateDeliveryStatusRequest.builder()
                .status(DeliveryStatus.IN_TRANSIT)
                .build();

        DeliveryDTO result = deliveryService.updateDeliveryStatus(delivery.getId(), request.getStatus());

        assertNotNull(result);
        assertEquals(DeliveryStatus.IN_TRANSIT, result.getStatus());

        Delivery updated = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertEquals(DeliveryStatus.IN_TRANSIT, updated.getStatus());
    }

    @Test
    void updateDeliveryStatus_toDelivered_shouldUpdateSuccessfully() {
        Courier courier = Courier.builder()
                .name("Иван Иванов")
                .phone("+77771234567")
                .status(CourierStatus.AVAILABLE)
                .build();
        courier = courierRepository.save(courier);

        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.IN_TRANSIT)
                .courier(courier)
                .build();
        delivery = deliveryRepository.save(delivery);

        UpdateDeliveryStatusRequest request = UpdateDeliveryStatusRequest.builder()
                .status(DeliveryStatus.DELIVERED)
                .build();

        DeliveryDTO result = deliveryService.updateDeliveryStatus(delivery.getId(), request.getStatus());

        assertNotNull(result);
        assertEquals(DeliveryStatus.DELIVERED, result.getStatus());

        Delivery updated = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertEquals(DeliveryStatus.DELIVERED, updated.getStatus());
    }

    @Test
    void assignCourier_availableCourier_shouldAssignSuccessfully() {
        Courier courier = Courier.builder()
                .name("Иван Иванов")
                .phone("+77771234567")
                .status(CourierStatus.AVAILABLE)
                .currentOrdersCount(0)
                .build();
        courier = courierRepository.save(courier);

        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.PENDING)
                .build();
        delivery = deliveryRepository.save(delivery);

        DeliveryDTO result = deliveryService.assignCourier(delivery.getId(), courier.getId());

        assertNotNull(result);
        assertEquals(DeliveryStatus.ASSIGNED, result.getStatus());
        assertEquals(courier.getId(), result.getCourier().getId());

        Delivery updated = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertEquals(DeliveryStatus.ASSIGNED, updated.getStatus());
        assertEquals(courier.getId(), updated.getCourier().getId());

        Courier updatedCourier = courierRepository.findById(courier.getId()).orElseThrow();
        assertEquals(1, updatedCourier.getCurrentOrdersCount());
    }

    @Test
    void assignCourier_nonExistingCourier_shouldThrowException() {
        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.PENDING)
                .build();
        delivery = deliveryRepository.save(delivery);

        Delivery finalDelivery = delivery;
        assertThrows(ResourceNotFoundException.class, () ->
                deliveryService.assignCourier(finalDelivery.getId(), 999L)
        );
    }

    @Test
    void assignCourier_unavailableCourier_shouldThrowException() {
        Courier courier = Courier.builder()
                .name("Иван Иванов")
                .phone("+77771234567")
                .status(CourierStatus.BUSY)
                .currentOrdersCount(3)
                .build();
        courier = courierRepository.save(courier);

        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.PENDING)
                .build();
        delivery = deliveryRepository.save(delivery);

        Courier finalCourier = courier;
        Delivery finalDelivery = delivery;
        assertThrows(IllegalStateException.class, () ->
                deliveryService.assignCourier(finalDelivery.getId(), finalCourier.getId())
        );
    }

    @Test
    void cancelDelivery_shouldUpdateStatusToCancelled() {
        Delivery delivery = Delivery.builder()
                .orderId(101L)
                .address("Адрес")
                .phone("+77771234567")
                .status(DeliveryStatus.ASSIGNED)
                .build();
        delivery = deliveryRepository.save(delivery);

        deliveryService.cancelDelivery(delivery.getId());

        Delivery updated = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertEquals(DeliveryStatus.CANCELLED, updated.getStatus());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DeliveryEventProducer deliveryEventProducer() {
            return Mockito.mock(DeliveryEventProducer.class);
        }
    }
}
