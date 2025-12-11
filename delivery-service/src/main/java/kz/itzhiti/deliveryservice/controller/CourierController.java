package kz.itzhiti.deliveryservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.itzhiti.deliveryservice.dto.CourierDTO;
import kz.itzhiti.deliveryservice.dto.CreateCourierRequest;
import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import kz.itzhiti.deliveryservice.service.CourierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Couriers", description = "Courier management API")
public class CourierController {

    private final CourierService courierService;

    @GetMapping
    @Operation(summary = "Get all couriers", description = "Retrieve list of all couriers")
    public ResponseEntity<List<CourierDTO>> getAllCouriers(
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {

        if (availableOnly) {
            return ResponseEntity.ok(courierService.getAvailableCouriers());
        }
        return ResponseEntity.ok(courierService.getAllCouriers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get courier by ID", description = "Retrieve a specific courier by ID")
    public ResponseEntity<CourierDTO> getCourierById(@PathVariable Long id) {
        CourierDTO courier = courierService.getCourierById(id);
        return ResponseEntity.ok(courier);
    }

    @PostMapping
    @Operation(summary = "Create new courier", description = "Create a new courier (ADMIN only)")
    public ResponseEntity<CourierDTO> createCourier(@Valid @RequestBody CreateCourierRequest request) {
        CourierDTO createdCourier = courierService.createCourier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourier);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update courier status", description = "Update courier status (ADMIN only)")
    public ResponseEntity<CourierDTO> updateCourierStatus(
            @PathVariable Long id,
            @RequestParam CourierStatus status) {

        CourierDTO updatedCourier = courierService.updateCourierStatus(id, status);
        return ResponseEntity.ok(updatedCourier);
    }
}

