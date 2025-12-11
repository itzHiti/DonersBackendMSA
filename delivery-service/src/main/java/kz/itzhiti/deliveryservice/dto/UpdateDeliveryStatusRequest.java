package kz.itzhiti.deliveryservice.dto;

import jakarta.validation.constraints.NotNull;
import kz.itzhiti.deliveryservice.model.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequest {
    @NotNull(message = "Status is required")
    private DeliveryStatus status;
}

