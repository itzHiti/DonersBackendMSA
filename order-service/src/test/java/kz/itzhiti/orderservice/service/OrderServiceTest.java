package kz.itzhiti.orderservice.service;

import kz.itzhiti.orderservice.dto.OrderDTO;
import kz.itzhiti.orderservice.exception.ResourceNotFoundException;
import kz.itzhiti.orderservice.exception.UnauthorizedException;
import kz.itzhiti.orderservice.model.Order;
import kz.itzhiti.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    // владелец заказа
    @Test
    void getOrderById_owner_shouldReturnOrderDTO() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("cust123");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderById(
                1L,
                "cust123",   // customerId
                false        // не админ
        );

        assertNotNull(result);
        verify(orderRepository).findById(1L);
    }

    // админ может смотреть любой заказ
    @Test
    void getOrderById_admin_shouldReturnOrderDTO() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("anotherCustomer");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderById(
                1L,
                "admin",
                true         // админ
        );

        assertNotNull(result);
    }

    // ни то ни другое
    @Test
    void getOrderById_notOwner_shouldThrowUnauthorized() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("owner123");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(UnauthorizedException.class, () ->
                orderService.getOrderById(
                        1L,
                        "hacker123", // другой customerId
                        false
                )
        );
    }

    // заказ не найден
    @Test
    void getOrderById_notFound_shouldThrowException() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderById(
                        1L,
                        "any",
                        true
                )
        );
    }
}
