package kz.itzhiti.deliveryservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.itzhiti.deliveryservice.dto.DeliveryDTO;
import kz.itzhiti.deliveryservice.dto.UpdateDeliveryStatusRequest;
import kz.itzhiti.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Deliveries", description = "Delivery management API")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/{id}")
    @Operation(summary = "Get delivery by ID", description = "Retrieve delivery details by ID")
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Long id) {
        DeliveryDTO delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(delivery);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get delivery by order ID", description = "Retrieve delivery details for a specific order")
    public ResponseEntity<DeliveryDTO> getDeliveryByOrderId(@PathVariable Long orderId) {
        DeliveryDTO delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(delivery);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all deliveries", description = "Retrieve all deliveries (ADMIN only)")
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveries() {
        List<DeliveryDTO> deliveries = deliveryService.getAllDeliveries();
        return ResponseEntity.ok(deliveries);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update delivery status", description = "Update delivery status (ADMIN/COURIER)")
    public ResponseEntity<DeliveryDTO> updateDeliveryStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {

        DeliveryDTO updatedDelivery = deliveryService.updateDeliveryStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedDelivery);
    }

    @PostMapping("/{deliveryId}/assign/{courierId}")
    @Operation(summary = "Assign courier to delivery", description = "Assign a courier to a delivery (ADMIN only)")
    public ResponseEntity<DeliveryDTO> assignCourier(
            @PathVariable Long deliveryId,
            @PathVariable Long courierId) {

        DeliveryDTO updatedDelivery = deliveryService.assignCourier(deliveryId, courierId);
        return ResponseEntity.ok(updatedDelivery);
    }
}

