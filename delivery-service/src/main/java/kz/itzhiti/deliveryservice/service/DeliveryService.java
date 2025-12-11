package kz.itzhiti.deliveryservice.service;

import kz.itzhiti.deliveryservice.dto.CourierDTO;
import kz.itzhiti.deliveryservice.dto.DeliveryDTO;
import kz.itzhiti.deliveryservice.event.DeliveryCompletedEvent;
import kz.itzhiti.deliveryservice.event.OrderCreatedEvent;
import kz.itzhiti.deliveryservice.exception.ResourceNotFoundException;
import kz.itzhiti.deliveryservice.model.Courier;
import kz.itzhiti.deliveryservice.model.Delivery;
import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import kz.itzhiti.deliveryservice.model.enums.DeliveryStatus;
import kz.itzhiti.deliveryservice.repository.CourierRepository;
import kz.itzhiti.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;
    private final DeliveryEventProducer eventProducer;

    @Transactional
    public DeliveryDTO createDeliveryFromOrder(OrderCreatedEvent orderEvent) {
        log.info("Creating delivery for order: {}", orderEvent.getOrderId());

        Delivery delivery = Delivery.builder()
                .orderId(orderEvent.getOrderId())
                .address(orderEvent.getDeliveryAddress())
                .phone(orderEvent.getPhone())
                .status(DeliveryStatus.PENDING)
                .estimatedTime(30) // Default 30 minutes
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.info("Delivery created with ID: {}", savedDelivery.getId());

        return convertToDTO(savedDelivery);
    }

    @Transactional(readOnly = true)
    public DeliveryDTO getDeliveryById(Long id) {
        log.debug("Fetching delivery by ID: {}", id);
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with ID: " + id));
        return convertToDTO(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryDTO getDeliveryByOrderId(Long orderId) {
        log.debug("Fetching delivery for order ID: {}", orderId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order ID: " + orderId));
        return convertToDTO(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryDTO> getAllDeliveries() {
        log.debug("Fetching all deliveries");
        return deliveryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryDTO updateDeliveryStatus(Long id, DeliveryStatus newStatus) {
        log.info("Updating delivery {} status to {}", id, newStatus);
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with ID: " + id));

        delivery.setStatus(newStatus);
        Delivery updatedDelivery = deliveryRepository.save(delivery);

        // If delivery is completed, send Kafka event
        if (newStatus == DeliveryStatus.DELIVERED) {
            DeliveryCompletedEvent event = DeliveryCompletedEvent.builder()
                    .orderId(delivery.getOrderId())
                    .deliveryId(delivery.getId())
                    .completedAt(LocalDateTime.now())
                    .build();
            eventProducer.sendDeliveryCompletedEvent(event);
        }

        log.info("Delivery status updated: {}", updatedDelivery.getId());
        return convertToDTO(updatedDelivery);
    }

    @Transactional
    public DeliveryDTO assignCourier(Long deliveryId, Long courierId) {
        log.info("Assigning courier {} to delivery {}", courierId, deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with ID: " + deliveryId));

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found with ID: " + courierId));

        if (courier.getStatus() != CourierStatus.AVAILABLE) {
            throw new IllegalStateException("Courier is not available");
        }

        delivery.setCourier(courier);
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        courier.setCurrentOrdersCount(courier.getCurrentOrdersCount() + 1);
        if (courier.getCurrentOrdersCount() >= 3) {
            courier.setStatus(CourierStatus.BUSY);
        }

        courierRepository.save(courier);
        Delivery updatedDelivery = deliveryRepository.save(delivery);

        log.info("Courier assigned to delivery: {}", updatedDelivery.getId());
        return convertToDTO(updatedDelivery);
    }

    private DeliveryDTO convertToDTO(Delivery delivery) {
        CourierDTO courierDTO = null;
        if (delivery.getCourier() != null) {
            courierDTO = CourierDTO.builder()
                    .id(delivery.getCourier().getId())
                    .name(delivery.getCourier().getName())
                    .phone(delivery.getCourier().getPhone())
                    .status(delivery.getCourier().getStatus())
                    .currentOrdersCount(delivery.getCourier().getCurrentOrdersCount())
                    .build();
        }

        return DeliveryDTO.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .courier(courierDTO)
                .address(delivery.getAddress())
                .phone(delivery.getPhone())
                .status(delivery.getStatus())
                .estimatedTime(delivery.getEstimatedTime())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}

