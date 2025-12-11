package kz.itzhiti.orderservice.service;

import kz.itzhiti.orderservice.dto.*;
import kz.itzhiti.orderservice.event.OrderCreatedEvent;
import kz.itzhiti.orderservice.event.OrderStatusChangedEvent;
import kz.itzhiti.orderservice.exception.ResourceNotFoundException;
import kz.itzhiti.orderservice.exception.UnauthorizedException;
import kz.itzhiti.orderservice.model.Order;
import kz.itzhiti.orderservice.model.OrderItem;
import kz.itzhiti.orderservice.model.enums.OrderStatus;
import kz.itzhiti.orderservice.model.Product;
import kz.itzhiti.orderservice.repository.OrderRepository;
import kz.itzhiti.orderservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderEventProducer eventProducer;

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request, String customerId) {
        log.info("Creating order for customer: {}", customerId);

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.PENDING)
                .deliveryAddress(request.getDeliveryAddress())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .totalPrice(BigDecimal.ZERO)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + itemRequest.getProductId()));

            if (!product.getAvailable()) {
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }

            BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalPrice = totalPrice.add(itemPrice);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addItem(orderItem);
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        // Publish Kafka event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .deliveryAddress(savedOrder.getDeliveryAddress())
                .phone(savedOrder.getPhone())
                .totalPrice(savedOrder.getTotalPrice())
                .createdAt(savedOrder.getCreatedAt())
                .build();

        eventProducer.sendOrderCreatedEvent(event);

        log.info("Order created with ID: {}", savedOrder.getId());
        return convertToDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id, String customerId, boolean isAdmin) {
        log.debug("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        if (!isAdmin && !order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }

        return convertToDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getCustomerOrders(String customerId) {
        log.debug("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        log.debug("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", id, newStatus);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // Publish status change event
        OrderStatusChangedEvent event = OrderStatusChangedEvent.builder()
                .orderId(updatedOrder.getId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .build();

        eventProducer.sendOrderStatusChangedEvent(event);

        log.info("Order status updated: {}", updatedOrder.getId());
        return convertToDTO(updatedOrder);
    }

    @Transactional
    public void cancelOrder(Long id, String customerId, boolean isAdmin) {
        log.info("Cancelling order: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        if (!isAdmin && !order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered order");
        }

        updateOrderStatus(id, OrderStatus.CANCELLED);
        log.info("Order cancelled: {}", id);
    }

    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .deliveryAddress(order.getDeliveryAddress())
                .phone(order.getPhone())
                .notes(order.getNotes())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
