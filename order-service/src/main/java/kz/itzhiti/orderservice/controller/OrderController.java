package kz.itzhiti.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.itzhiti.orderservice.dto.CreateOrderRequest;
import kz.itzhiti.orderservice.dto.OrderDTO;
import kz.itzhiti.orderservice.dto.UpdateOrderStatusRequest;
import kz.itzhiti.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
@SecurityRequirement(name = "bearer-jwt")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create new order", description = "Create a new doner order (USER)")
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        String customerId = getCustomerId(authentication);
        OrderDTO createdOrder = orderService.createOrder(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by ID")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        String customerId = getCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        OrderDTO order = orderService.getOrderById(id, customerId, isAdmin);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get my orders", description = "Retrieve all orders for the authenticated customer")
    public ResponseEntity<List<OrderDTO>> getMyOrders(Authentication authentication) {
        String customerId = getCustomerId(authentication);
        List<OrderDTO> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all orders", description = "Retrieve all orders (ADMIN only)")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Update order status (ADMIN only)")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel order", description = "Cancel an order (USER can cancel own orders)")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        String customerId = getCustomerId(authentication);
        boolean isAdmin = isAdmin(authentication);
        orderService.cancelOrder(id, customerId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    private String getCustomerId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        }
        return authentication.getName();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}
