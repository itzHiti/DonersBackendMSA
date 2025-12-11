package kz.itzhiti.deliveryservice.service;

import kz.itzhiti.deliveryservice.dto.CourierDTO;
import kz.itzhiti.deliveryservice.dto.CreateCourierRequest;
import kz.itzhiti.deliveryservice.exception.ResourceNotFoundException;
import kz.itzhiti.deliveryservice.model.Courier;
import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import kz.itzhiti.deliveryservice.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourierService {

    private final CourierRepository courierRepository;

    @Transactional(readOnly = true)
    public List<CourierDTO> getAllCouriers() {
        log.debug("Fetching all couriers");
        return courierRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourierDTO> getAvailableCouriers() {
        log.debug("Fetching available couriers");
        return courierRepository.findByStatus(CourierStatus.AVAILABLE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourierDTO getCourierById(Long id) {
        log.debug("Fetching courier by ID: {}", id);
        Courier courier = courierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found with ID: " + id));
        return convertToDTO(courier);
    }

    @Transactional
    public CourierDTO createCourier(CreateCourierRequest request) {
        log.info("Creating new courier: {}", request.getName());

        Courier courier = Courier.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .status(request.getStatus())
                .currentOrdersCount(0)
                .build();

        Courier savedCourier = courierRepository.save(courier);
        log.info("Courier created with ID: {}", savedCourier.getId());
        return convertToDTO(savedCourier);
    }

    @Transactional
    public CourierDTO updateCourierStatus(Long id, CourierStatus status) {
        log.info("Updating courier {} status to {}", id, status);

        Courier courier = courierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found with ID: " + id));

        courier.setStatus(status);
        Courier updatedCourier = courierRepository.save(courier);

        log.info("Courier status updated: {}", updatedCourier.getId());
        return convertToDTO(updatedCourier);
    }

    private CourierDTO convertToDTO(Courier courier) {
        return CourierDTO.builder()
                .id(courier.getId())
                .name(courier.getName())
                .phone(courier.getPhone())
                .status(courier.getStatus())
                .currentOrdersCount(courier.getCurrentOrdersCount())
                .build();
    }
}

