package kz.itzhiti.deliveryservice.dto;

import kz.itzhiti.deliveryservice.dto.CourierDTO;
import kz.itzhiti.deliveryservice.model.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {

    private Long id;
    private Long orderId;
    private CourierDTO courier;
    private String address;
    private String phone;
    private DeliveryStatus status;
    private Integer estimatedTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}