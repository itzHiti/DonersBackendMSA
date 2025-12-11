package kz.itzhiti.deliveryservice.dto;

import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierDTO {
    private Long id;
    private String name;
    private String phone;
    private CourierStatus status;
    private Integer currentOrdersCount;
}

