package kz.itzhiti.deliveryservice.service;

import kz.itzhiti.deliveryservice.dto.CourierDTO;
import kz.itzhiti.deliveryservice.dto.CreateCourierRequest;
import kz.itzhiti.deliveryservice.exception.ResourceNotFoundException;
import kz.itzhiti.deliveryservice.model.Courier;
import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import kz.itzhiti.deliveryservice.repository.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierService courierService;

    @Test
    void getAllCouriers_shouldReturnAllCouriers() {
        Courier courier1 = Courier.builder()
                .id(1L)
                .name("Иван Иванов")
                .phone("+77771234567")
                .status(CourierStatus.AVAILABLE)
                .build();

        Courier courier2 = Courier.builder()
                .id(2L)
                .name("Петр Петров")
                .phone("+77779876543")
                .status(CourierStatus.BUSY)
                .build();

        when(courierRepository.findAll()).thenReturn(Arrays.asList(courier1, courier2));

        List<CourierDTO> result = courierService.getAllCouriers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Иван Иванов", result.get(0).getName());
        verify(courierRepository, times(1)).findAll();
    }

    @Test
    void getAvailableCouriers_shouldReturnOnlyAvailable() {
        Courier courier = Courier.builder()
                .id(1L)
                .name("Иван Иванов")
                .status(CourierStatus.AVAILABLE)
                .build();

        when(courierRepository.findByStatus(CourierStatus.AVAILABLE))
                .thenReturn(List.of(courier));

        List<CourierDTO> result = courierService.getAvailableCouriers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CourierStatus.AVAILABLE, result.get(0).getStatus());
        verify(courierRepository).findByStatus(CourierStatus.AVAILABLE);
    }

    @Test
    void getCourierById_existingCourier_shouldReturnCourier() {
        Courier courier = Courier.builder()
                .id(1L)
                .name("Иван Иванов")
                .phone("+77771234567")
                .build();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        CourierDTO result = courierService.getCourierById(1L);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.getName());
        verify(courierRepository).findById(1L);
    }

    @Test
    void getCourierById_nonExistingCourier_shouldThrowException() {
        when(courierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                courierService.getCourierById(999L)
        );
        verify(courierRepository).findById(999L);
    }

    @Test
    void createCourier_shouldReturnCreatedCourier() {
        CreateCourierRequest request = CreateCourierRequest.builder()
                .name("Новый курьер")
                .phone("+77771234567")
                .build();

        Courier savedCourier = Courier.builder()
                .id(1L)
                .name("Новый курьер")
                .phone("+77771234567")
                .status(CourierStatus.AVAILABLE)
                .build();

        when(courierRepository.save(any(Courier.class))).thenReturn(savedCourier);

        CourierDTO result = courierService.createCourier(request);

        assertNotNull(result);
        assertEquals("Новый курьер", result.getName());
        assertEquals(CourierStatus.AVAILABLE, result.getStatus());
        verify(courierRepository).save(any(Courier.class));
    }

    @Test
    void updateCourierStatus_existingCourier_shouldUpdateStatus() {
        Courier courier = Courier.builder()
                .id(1L)
                .name("Иван Иванов")
                .status(CourierStatus.AVAILABLE)
                .build();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierRepository.save(any(Courier.class))).thenReturn(courier);

        CourierDTO result = courierService.updateCourierStatus(1L, CourierStatus.BUSY);

        assertNotNull(result);
        assertEquals(CourierStatus.BUSY, result.getStatus());
        verify(courierRepository).findById(1L);
        verify(courierRepository).save(any(Courier.class));
    }

    @Test
    void deleteCourier_existingCourier_shouldDeleteSuccessfully() {
        when(courierRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courierRepository).deleteById(1L);

        courierService.deleteCourier(1L);

        verify(courierRepository).existsById(1L);
        verify(courierRepository).deleteById(1L);
    }

    @Test
    void assignCourierToDelivery_shouldMarkAsBusy() {
        Courier courier = Courier.builder()
                .id(1L)
                .name("Иван Иванов")
                .status(CourierStatus.AVAILABLE)
                .currentOrdersCount(0)
                .build();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierRepository.save(any(Courier.class))).thenReturn(courier);

        courierService.updateCourierStatus(courier.getId(), CourierStatus.BUSY);

        verify(courierRepository).findById(1L);
        verify(courierRepository).save(argThat(c ->
                c.getStatus() == CourierStatus.BUSY && c.getCurrentOrdersCount() == 0
        ));
    }

    @Test
    void releaseCourierFromDelivery_shouldMarkAsAvailable() {
        Courier courier = Courier.builder()
                .id(1L)
                .name("Иван Иванов")
                .status(CourierStatus.BUSY)
                .currentOrdersCount(1)
                .build();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierRepository.save(any(Courier.class))).thenReturn(courier);

        courierService.updateCourierStatus(courier.getId(), CourierStatus.AVAILABLE);

        verify(courierRepository).findById(1L);
        verify(courierRepository).save(argThat(c ->
                c.getStatus() == CourierStatus.AVAILABLE && c.getCurrentOrdersCount() == 1
        ));
    }
}
