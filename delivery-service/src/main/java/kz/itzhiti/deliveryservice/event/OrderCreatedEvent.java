package kz.itzhiti.deliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String customerId;
    private String deliveryAddress;
    private String phone;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}

